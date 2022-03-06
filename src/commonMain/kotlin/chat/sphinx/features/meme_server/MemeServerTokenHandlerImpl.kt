package chat.sphinx.features.meme_server

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
import chat.sphinx.concepts.network.query.meme_server.NetworkQueryMemeServer
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.logger.e
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.utils.platform.getCurrentTimeInMillis
import chat.sphinx.wrapper.contact.Contact
import chat.sphinx.wrapper.meme_server.*
import chat.sphinx.wrapper.message.media.token.MediaHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private suspend inline fun AuthenticationStorage.removeToken(mediaHost: MediaHost) {
    removeString(
        "MEME_SERVER_TOKEN_${mediaHost.value}"
    )
}

/**
 * Stores the [AuthenticationToken] along with its expiration time appended to it
 * */
private suspend inline fun AuthenticationStorage.persistToken(
    mediaHost: MediaHost,
    token: AuthenticationToken,
) {
    val nowPlus7Days = getCurrentTimeInMillis() + MemeServerTokenHandlerImpl._7_DAYS
    val tokenString = token.value + MemeServerTokenHandlerImpl.DELIMITER + nowPlus7Days.toString()
    putString(
        "MEME_SERVER_TOKEN_${mediaHost.value}",
        tokenString
    )
}

class MemeServerTokenHandlerImpl(
    private val accountOwner: StateFlow<Contact?>,
    applicationScope: CoroutineScope,
    private val authenticationStorage: AuthenticationStorage,
    dispatchers: CoroutineDispatchers,
    private val networkQueryMemeServer: NetworkQueryMemeServer,
    private val LOG: SphinxLogger,
) : MemeServerTokenHandler(),
    CoroutineDispatchers by dispatchers
{
    companion object {
        const val TAG = "MemeServerTokenHandlerImpl"

        const val DELIMITER = "|--|"

        @Suppress("ObjectPropertyName")
        const val _7_DAYS = 7 * 24 * 60 * 60 * 1000
    }

    private open inner class SynchronizedMap<K, V> {
        private val hashMap: MutableMap<K, V> = LinkedHashMap(1)
        private val lock = Mutex()
        open suspend fun <T> withLock(action: suspend (MutableMap<K, V>) -> T): T =
            lock.withLock {
                action(hashMap)
            }
    }

    /**
     * Generates & returns a [Mutex] for the given [MediaHost] in a synchronous manner
     * */
    private inner class SynchronizedLockMap: SynchronizedMap<MediaHost, Mutex>() {
        suspend fun getOrCreateLock(mediaHost: MediaHost): Mutex =
            super.withLock {
                it[mediaHost] ?: Mutex().also { mutex ->
                    it[mediaHost] = mutex
                }
            }

        override suspend fun <T> withLock(action: suspend (MutableMap<MediaHost, Mutex>) -> T): T {
            throw IllegalStateException("Use method getOrCreateLock instead")
        }
    }

    private val tokenCache = SynchronizedMap<MediaHost, AuthenticationToken?>()
    private val tokenLock = SynchronizedLockMap()

    override suspend fun retrieveAuthenticationToken(mediaHost: MediaHost): AuthenticationToken? {
        return tokenCache.withLock { it[mediaHost] } ?: tokenLock.getOrCreateLock(mediaHost).withLock {
            tokenCache.withLock { it[mediaHost] } ?: retrieveAuthenticationTokenImpl(mediaHost)
                .also { token ->
                    tokenCache.withLock { it[mediaHost] = token }
                }
        }
    }

    private suspend fun retrieveAuthenticationTokenImpl(mediaHost: MediaHost): AuthenticationToken? {
        authenticationStorage.getString(
            "MEME_SERVER_TOKEN_${mediaHost.value}",
            null
        ).let { tokenString ->
            return if (tokenString == null) {
                authenticateToHost(mediaHost)?.let { nnToken ->
                    authenticationStorage.persistToken(mediaHost, nnToken)

                    nnToken
                }
            } else {
                val data: Pair<AuthenticationToken, Long>? = tokenString.split(DELIMITER).let { splits ->
                    splits.elementAtOrNull(0)?.toAuthenticationToken()?.let { token ->
                        splits.elementAtOrNull(1)?.toLongOrNull()?.let { expiration ->
                            Pair(token, expiration)
                        }
                    }
                }

                data?.let { nnData ->
                    val now = getCurrentTimeInMillis()

                    if (now > nnData.second) {
                        authenticateToHost(mediaHost).let { token ->
                            if (token != null) {
                                authenticationStorage.persistToken(mediaHost, token)

                                token
                            } else {
                                authenticationStorage.removeToken(mediaHost)

                                null
                            }
                        }
                    } else {
                        LOG.d(
                            TAG,
                            """
                                MemeServerAuthenticationToken retrieved from persistent storage!
                                host: $mediaHost
                                token: ${nnData.first}
                                expiration (ms): ${nnData.second - now}
                            """.trimIndent()
                        )

                        nnData.first
                    }
                } ?: authenticateToHost(mediaHost).let { token ->
                    if (token != null) {
                        authenticationStorage.persistToken(mediaHost, token)

                        token
                    } else {
                        authenticationStorage.removeToken(mediaHost)

                        null
                    }
                }
            }
        }
    }

    private suspend fun authenticateToHost(mediaHost: MediaHost): AuthenticationToken? {
        var owner = accountOwner.value

        if (owner?.nodePubKey == null) {
            try {
                accountOwner.collect { contact ->
                    // suspend until account owner is available (either
                    // because we're awaiting a contact network refresh
                    // for the first time, or the DB has yet to be decrypted
                    if (contact != null) {
                        owner = contact
                        throw Exception()
                    }
                }
            } catch (e: Exception) {}

            delay(25L)
        }

        var token: AuthenticationToken? = null

        owner?.nodePubKey?.let { nodePubKey ->
            var id: AuthenticationId? = null
            var challenge: AuthenticationChallenge? = null

            networkQueryMemeServer.askAuthentication(mediaHost).collect { loadResponse ->
                Exhaustive@
                when (loadResponse) {
                    is LoadResponse.Loading -> {}
                    is Response.Error -> {
                        LOG.e(TAG, loadResponse.message, loadResponse.exception)
                    }
                    is Response.Success -> {
                        id = loadResponse.value.id.toAuthenticationId()
                        challenge = loadResponse.value.challenge.toAuthenticationChallenge()
                    }
                }
            }

            id?.let { nnId ->
                challenge?.let { nnChallenge ->

                    var sig: AuthenticationSig? = null

                    networkQueryMemeServer.signChallenge(nnChallenge).collect { loadResponse ->
                        Exhaustive@
                        when (loadResponse) {
                            is LoadResponse.Loading -> {}
                            is Response.Error -> {
                                LOG.e(TAG, loadResponse.message, loadResponse.exception)
                            }
                            is Response.Success -> {
                                sig = loadResponse.value.sig.toAuthenticationSig()
                            }
                        }
                    }

                    sig?.let { nnSig ->

                        networkQueryMemeServer.verifyAuthentication(
                            nnId,
                            nnSig,
                            nodePubKey,
                            mediaHost,
                        ).collect { loadResponse ->
                            Exhaustive@
                            when (loadResponse) {
                                is LoadResponse.Loading -> {}
                                is Response.Error -> {
                                    LOG.e(TAG, loadResponse.message, loadResponse.exception)
                                }
                                is Response.Success -> {
                                    loadResponse.value.token.toAuthenticationToken()?.let { nnToken ->
                                        token = nnToken
                                        LOG.d(
                                            TAG,
                                            """
                                                                        MemeServerAuthenticationToken acquired from server!
                                                                        host: $mediaHost
                                                                        token: $nnToken
                                                                    """.trimIndent()
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        return token
    }

    init {
        // Primes the default meme server token. If it's not persisted or
        // invalid (timed out), will suspend until the Account Owner is,
        // is available from the DB (meaning user has successfully authenticated).
        applicationScope.launch(mainImmediate) {
            retrieveAuthenticationToken(MediaHost.DEFAULT)
        }
    }
}
