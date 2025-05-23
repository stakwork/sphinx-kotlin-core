package chat.sphinx.features.network.relay_call

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.network.call.buildRequest
import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.client.NetworkClientClearedListener
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.d
import chat.sphinx.logger.e
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.response.message
import chat.sphinx.utils.SphinxJson
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.errors.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.util.concurrent.TimeUnit

@Suppress("NOTHING_TO_INLINE")
private inline fun NetworkRelayCallImpl.mapRelayHeaders(
    relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>,
    additionalHeaders: Map<String, String>?,
    LOG: SphinxLogger,
): Map<String, String> {

    val map: MutableMap<String, String> = mutableMapOf(
        relayData.first.second?.let { transportToken ->
            Pair(TransportToken.TRANSPORT_TOKEN_HEADER, transportToken.value)
        } ?: Pair(AuthorizationToken.AUTHORIZATION_HEADER, relayData.first.first.value)
    )

    relayData.second?.let { signedRequest ->
        map.put(
            RequestSignature.REQUEST_SIGNATURE_HEADER,
            signedRequest.value
        )
    }

    additionalHeaders?.let {
        map.putAll(it)
    }

    LOG.d("SPHINX KEY", map.toString())
    LOG.d("SPHINX KEY", map.toString())
    LOG.d("SPHINX KEY", map.toString())

    return map
}

@Suppress("NOTHING_TO_INLINE")
private inline fun NetworkRelayCallImpl.handleException(
    LOG: SphinxLogger,
    callMethod: String,
    url: String,
    e: Exception
): Response.Error<ResponseError> {
    val msg = "$callMethod Request failure for: $url"
    LOG.e(NetworkRelayCallImpl.TAG, msg, e)
    return Response.Error(ResponseError(msg, e))
}


@Suppress("BlockingMethodInNonBlockingContext")
class NetworkRelayCallImpl(
    private val dispatchers: CoroutineDispatchers,
    private val networkClient: NetworkClient,
    private val relayDataHandler: RelayDataHandler,
    private val LOG: SphinxLogger,
) : NetworkRelayCall(),
    NetworkClientClearedListener,
    CoroutineDispatchers by dispatchers
{
    companion object {
        const val TAG = "NetworkRelayCallImpl"

        private const val GET = "GET"
        private const val PUT = "PUT"
        private const val POST = "POST"
        private const val DELETE = "DELETE"
    }

    ///////////////////
    /// NetworkCall ///
    ///////////////////
    override fun <T: Any> get(
        url: String,
        responseJsonSerializer: KSerializer<T>,
        headers: Map<String, String>?,
        useExtendedNetworkCallClient: Boolean
    ): Flow<LoadResponse<T, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val response = call(responseJsonSerializer, requestBuilder.build(), useExtendedNetworkCallClient)

            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(handleException(LOG, GET, url, e))
        }
    }

    override fun <T: Any> getList(
        url: String,
        responseJsonSerializer: KSerializer<T>,
        headers: Map<String, String>?,
        useExtendedNetworkCallClient: Boolean,
    ): Flow<LoadResponse<List<T>, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val response = callList(responseJsonSerializer, requestBuilder.build(), useExtendedNetworkCallClient)

            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(handleException(LOG, GET, url, e))
        }
    }

    override fun <Result: Any, Input: Any> put(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>?,
        mediaType: String?,
        headers: Map<String, String>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val requestBodyJson: String? = requestBodyPair?.let { (requestBody, requestBodySerializer) ->
                Json.encodeToString(requestBodySerializer, requestBody)
            }

            val reqBody = requestBodyJson?.toRequestBody(mediaType?.toMediaType())

            val response = call(responseJsonSerializer, requestBuilder.put(reqBody ?: EMPTY_REQUEST).build())

            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(handleException(LOG, PUT, url, e))
        }

    }

    override fun <Result: Any, Input: Any> post(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String?,
        headers: Map<String, String>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val (requestBody, requestBodySerializer) = requestBodyPair
            val requestBodyJson: String = Json.encodeToString(requestBodySerializer, requestBody)

            val reqBody = requestBodyJson.toRequestBody(mediaType?.toMediaType())

            val response = call(responseJsonSerializer, requestBuilder.post(reqBody).build())

            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(handleException(LOG, POST, url, e))
        }

    }

    override fun <Result: Any, Input: Any> delete(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>?,
        mediaType: String?,
        headers: Map<String, String>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val requestBodyJson: String? = requestBodyPair?.let { (requestBody, requestBodySerializer) ->
                Json.encodeToString(requestBodySerializer, requestBody)
            }

            val reqBody: RequestBody? = requestBodyJson?.toRequestBody(mediaType?.toMediaType())

            val response = call(responseJsonSerializer, requestBuilder.delete(reqBody ?: EMPTY_REQUEST).build())

            emit(Response.Success(response))
        } catch (e: Exception) {
            emit(handleException(LOG, DELETE, url, e))
        }

    }

    @Volatile
    private var extendedNetworkCallClient: OkHttpClient? = null
    private val extendedClientLock = Mutex()

    override fun networkClientCleared() {
        extendedNetworkCallClient = null
    }

    init {
        networkClient.addListener(this)
    }

    @Throws(NullPointerException::class, java.io.IOException::class)
    override suspend fun <T: Any> call(
        responseJsonSerializer: KSerializer<T>,
        request: Request,
        useExtendedNetworkCallClient: Boolean
    ): T {

        // TODO: Make less horrible. Needed for the `/contacts` endpoint for users who
        //  have a large number of contacts as Relay needs more time than the default
        //  client's settings. Replace once the `aa/contacts` endpoint gets pagination.
        val client = if (useExtendedNetworkCallClient) {
            extendedClientLock.withLock {
                extendedNetworkCallClient ?: networkClient.getClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(45, TimeUnit.SECONDS)
                    .build()
                    .also { extendedNetworkCallClient = it }
            }
        } else {
            networkClient.getClient()
        }

        val networkResponse = withContext(io) {
            client.newCall(request).execute()
        }

        if (!networkResponse.isSuccessful) {
            networkResponse.body?.close()
            throw java.io.IOException(networkResponse.toString())
        }

        val body = networkResponse.body ?: throw NullPointerException(
            """
                NetworkResponse.body returned null
                NetworkResponse: $networkResponse
            """.trimIndent()
        )

        return withContext(default) {
            SphinxJson.decodeFromString(responseJsonSerializer, body.string())
        } ?: throw IOException(
            """
                Failed to convert Json to ${responseJsonSerializer.descriptor}
                NetworkResponse: $networkResponse
            """.trimIndent()
        )
    }

    @Throws(NullPointerException::class, java.io.IOException::class)
    override suspend fun <T: Any> callList(
        responseJsonSerializer: KSerializer<T>,
        request: Request,
        useExtendedNetworkCallClient: Boolean
    ): List<T> {
        // TODO: Make less horrible. Needed for the `/contacts` endpoint for users who
        //  have a large number of contacts as Relay needs more time than the default
        //  client's settings. Replace once the `aa/contacts` endpoint gets pagination.
        val client = if (useExtendedNetworkCallClient) {
            extendedClientLock.withLock {
                extendedNetworkCallClient ?: networkClient.getClient().newBuilder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(45, TimeUnit.SECONDS)
                    .build()
                    .also { extendedNetworkCallClient = it }
            }
        } else {
            networkClient.getClient()
        }

        val networkResponse = withContext(io) {
            client.newCall(request).execute()
        }

        if (!networkResponse.isSuccessful) {
            networkResponse.body?.close()
            throw java.io.IOException(networkResponse.toString())
        }

        val body = networkResponse.body ?: throw NullPointerException(
            """
                NetworkResponse.body returned null
                NetworkResponse: $networkResponse
            """.trimIndent()
        )

        return withContext(default) {
            SphinxJson.decodeFromString(
                ListSerializer(responseJsonSerializer),
                body.string()
            )
        } ?: throw IOException(
            """
                Failed to convert Json to ${responseJsonSerializer::class.simpleName}
                NetworkResponse: $networkResponse
            """.trimIndent()
        )
    }

    override fun getRawJson(
        url: String,
        headers: Map<String, String>?,
        useExtendedNetworkCallClient: Boolean
    ): Flow<LoadResponse<String, ResponseError>> = flow {

        emit(LoadResponse.Loading)

        try {
            val requestBuilder = buildRequest(url, headers)

            val client = if (useExtendedNetworkCallClient) {
                extendedClientLock.withLock {
                    extendedNetworkCallClient ?: networkClient.getClient().newBuilder()
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(45, TimeUnit.SECONDS)
                        .writeTimeout(45, TimeUnit.SECONDS)
                        .build()
                        .also { extendedNetworkCallClient = it }
                }
            } else {
                networkClient.getClient()
            }

            val networkResponse = withContext(io) {
                client.newCall(requestBuilder.build()).execute()
            }

            val body = networkResponse.body ?: throw NullPointerException(
                """
            NetworkResponse.body returned null
            NetworkResponse: $networkResponse
        """.trimIndent()
            )

            if (!networkResponse.isSuccessful) {
                val responseError = body.string()
                body.close()
                throw IOException(responseError)
            }

            val rawJson = withContext(default) {
                body.string()
            }

            emit(Response.Success(rawJson))
        } catch (e: Exception) {
            emit(handleException(LOG, GET, url, e))
        }
    }


}
