package chat.sphinx.features.network.relay_call

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.network.call.buildRequest
import chat.sphinx.concepts.network.client.NetworkClient
import chat.sphinx.concepts.network.client.NetworkClientClearedListener
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.concepts.network.relay_call.RelayResponse
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.concepts.relay.retrieveRelayUrlAndAuthorizationToken
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.logger.e
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.response.message
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.errors.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

@Suppress("NOTHING_TO_INLINE")
private inline fun NetworkRelayCallImpl.mapRelayHeaders(
    relayData: Pair<AuthorizationToken, RelayUrl>,
    additionalHeaders: Map<String, String>?
): Map<String, String> {
    val map: MutableMap<String, String> = mutableMapOf(
        Pair(AuthorizationToken.AUTHORIZATION_HEADER, relayData.first.value)
    )

    additionalHeaders?.let {
        map.putAll(it)
    }

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

@Throws(Exception::class)
private suspend inline fun RelayDataHandler.retrieveRelayData(): Pair<AuthorizationToken, RelayUrl> {
    val response = retrieveRelayUrlAndAuthorizationToken()

    Exhaustive@
    when(response) {
        is Response.Error -> {
            throw Exception(response.message)
        }
        is Response.Success -> {
            return response.value
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
@Throws(AssertionError::class, NullPointerException::class, CancellationException::class)
private suspend inline fun RequestBody.requestBodyToJson(
    dispatchers: CoroutineDispatchers
): String =
    withContext(dispatchers.default) {
        Json.encodeToString(RequestBodySerializer, this@requestBodyToJson)
    } ?: throw NullPointerException(
        "Failed to convert RequestBody ${this::class.simpleName} to Json"
    )



@Serializer(forClass = RequestBody::class)
object RequestBodySerializer

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

    @Throws(NullPointerException::class, IOException::class)
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
            throw IOException(networkResponse.toString())
        }

        val body = networkResponse.body ?: throw NullPointerException(
            """
                NetworkResponse.body returned null
                NetworkResponse: $networkResponse
            """.trimIndent()
        )

        return withContext(default) {
            Json { ignoreUnknownKeys = true }.decodeFromString(responseJsonSerializer, body.string())
        } ?: throw IOException(
            """
                Failed to convert Json to ${responseJsonSerializer.descriptor}
                NetworkResponse: $networkResponse
            """.trimIndent()
        )
    }

    @Throws(NullPointerException::class, IOException::class)
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
            throw IOException(networkResponse.toString())
        }

        val body = networkResponse.body ?: throw NullPointerException(
            """
                NetworkResponse.body returned null
                NetworkResponse: $networkResponse
            """.trimIndent()
        )

        return withContext(default) {
            Json { ignoreUnknownKeys = true }.decodeFromString(
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

    ////////////////////////
    /// NetworkRelayCall ///
    ////////////////////////
    override fun <T: Any, V: RelayResponse<T>> relayGet(
        responseJsonSerializer: KSerializer<V>,
        relayEndpoint: String,
        additionalHeaders: Map<String, String>?,
        relayData: Pair<AuthorizationToken, RelayUrl>?,
        useExtendedNetworkCallClient: Boolean,
    ): Flow<LoadResponse<T, ResponseError>> = flow {

        val responseFlow: Flow<LoadResponse<V, ResponseError>>? = try {
            val nnRelayData: Pair<AuthorizationToken, RelayUrl> = relayData
                ?: relayDataHandler.retrieveRelayData()

            get(
                nnRelayData.second.value + relayEndpoint,
                responseJsonSerializer,
                mapRelayHeaders(nnRelayData, additionalHeaders),
                useExtendedNetworkCallClient
            )
        } catch (e: Exception) {
            emit(handleException(LOG, GET, relayEndpoint, e))
            null
        }

        responseFlow?.let {
            emitAll(validateRelayResponse(it, GET, relayEndpoint))
        }

    }

    override fun <Result: Any, Input: Any, Output: RelayResponse<Result>> relayPut(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>?,
        mediaType: String?,
        additionalHeaders: Map<String, String>?,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        val responseFlow: Flow<LoadResponse<Output, ResponseError>>? = try {
            val nnRelayData: Pair<AuthorizationToken, RelayUrl> = relayData
                ?: relayDataHandler.retrieveRelayData()

            put(
                nnRelayData.second.value + relayEndpoint,
                responseJsonSerializer,
                requestBodyPair,
                mediaType,
                mapRelayHeaders(nnRelayData, additionalHeaders)
            )
        } catch (e: Exception) {
            emit(handleException(LOG, PUT, relayEndpoint, e))
            null
        }

        responseFlow?.let {
            emitAll(validateRelayResponse(it, PUT, relayEndpoint))
        }

    }

    // TODO: Remove and replace all uses with post (DO NOT USE THIS METHOD FOR NEW CODE)
    override fun <Result: Any, Input: Any, Output: RelayResponse<Result>> relayUnauthenticatedPost(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String?,
        relayUrl: RelayUrl
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        val responseFlow: Flow<LoadResponse<Output, ResponseError>>? = try {
            post(
                relayUrl.value + relayEndpoint,
                responseJsonSerializer,
                requestBodyPair,
                mediaType
            )
        } catch (e: Exception) {
            emit(handleException(LOG, POST, relayEndpoint, e))
            null
        }

        responseFlow?.let {
            emitAll(validateRelayResponse(it, POST, relayEndpoint))
        }

    }

    override fun <Result: Any, Input: Any, Output: RelayResponse<Result>> relayPost(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String?,
        additionalHeaders: Map<String, String>?,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        val responseFlow: Flow<LoadResponse<Output, ResponseError>>? = try {
            val nnRelayData: Pair<AuthorizationToken, RelayUrl> = relayData
                ?: relayDataHandler.retrieveRelayData()

            post(
                nnRelayData.second.value + relayEndpoint,
                responseJsonSerializer,
                requestBodyPair,
                mediaType,
                mapRelayHeaders(nnRelayData, additionalHeaders)
            )
        } catch (e: Exception) {
            emit(handleException(LOG, POST, relayEndpoint, e))
            null
        }

        responseFlow?.let {
            emitAll(validateRelayResponse(it, POST, relayEndpoint))
        }

    }

    override fun <Result: Any, Input: Any, Output: RelayResponse<Result>> relayDelete(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>?,
        mediaType: String?,
        additionalHeaders: Map<String, String>?,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<Result, ResponseError>> = flow {

        val responseFlow: Flow<LoadResponse<Output, ResponseError>>? = try {
            val nnRelayData: Pair<AuthorizationToken, RelayUrl> = relayData
                ?: relayDataHandler.retrieveRelayData()

            delete(
                nnRelayData.second.value + relayEndpoint,
                responseJsonSerializer,
                requestBodyPair,
                mediaType,
                mapRelayHeaders(nnRelayData, additionalHeaders)
            )
        } catch (e: Exception) {
            emit(handleException(LOG, DELETE, relayEndpoint, e))
            null
        }

        responseFlow?.let {
            emitAll(validateRelayResponse(it, DELETE, relayEndpoint))
        }

    }

    @OptIn(InternalCoroutinesApi::class)
    @Throws(NullPointerException::class, AssertionError::class)
    private fun <T: Any, V: RelayResponse<T>> validateRelayResponse(
        flow: Flow<LoadResponse<V, ResponseError>>,
        callMethod: String,
        endpoint: String,
    ): Flow<LoadResponse<T, ResponseError>> = flow {
        flow.collect { loadResponse ->
            when(loadResponse) {
                is LoadResponse.Loading -> {
                    emit(loadResponse)
                }
                is Response.Error -> {
                    emit(loadResponse)
                }
                is Response.Success -> {
                    if (loadResponse.value.success) {

                        loadResponse.value.response?.let { nnResponse ->

                            emit(Response.Success(nnResponse))

                        } ?: let {

                            val msg = """
                                        RelayResponse.success: true
                                        RelayResponse.response: >>> null <<<
                                        RelayResponse.error: ${loadResponse.value.error}
                                    """.trimIndent()

                            emit(handleException(LOG, callMethod, endpoint, NullPointerException(msg)))

                        }
                    } else {
                        val msg = """
                                    RelayResponse.success: false
                                    RelayResponse.error: ${loadResponse.value.error}
                                """.trimIndent()

                        emit(handleException(LOG, callMethod, endpoint, Exception(msg)))
                    }
                }
            }
        }
    }

}
