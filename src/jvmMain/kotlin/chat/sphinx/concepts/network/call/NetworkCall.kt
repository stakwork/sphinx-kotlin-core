package chat.sphinx.concepts.network.call

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import kotlinx.coroutines.flow.Flow
import kotlinx.io.errors.IOException
import kotlinx.serialization.KSerializer
import okhttp3.Request

@Suppress("NOTHING_TO_INLINE")
@Throws(IllegalArgumentException::class)
inline fun NetworkCall.buildRequest(
    url: String,
    headers: Map<String, String>?
): Request.Builder {
    val builder = Request.Builder()

    builder.url(url)

    headers?.let {
        for (header in it) {
            builder.addHeader(header.key, header.value)
        }
    }

    return builder
}

/**
 * Methods for GET/PUT/POST/DELETE for general, non-Relay specific network queries.
 * */
abstract class NetworkCall {

    /**
     * GET
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [url] the url
     * @param [headers] any headers that need to be added to the request
     * */
    abstract fun <T: Any> get(
        url: String,
        responseJsonSerializer: KSerializer<T>,
        headers: Map<String, String>? = null,
        useExtendedNetworkCallClient: Boolean = false,
    ): Flow<LoadResponse<T, ResponseError>>

    /**
     * GET
     *
     * @param [responseJsonClass] the class to serialize the response json as a List of
     * @param [url] the url
     * @param [headers] any headers that need to be added to the request
     * */
    abstract fun <T: Any> getList(
        url: String,
        responseJsonClass: Class<T>,
        headers: Map<String, String>? = null,
        useExtendedNetworkCallClient: Boolean = false,
    ): Flow<LoadResponse<List<T>, ResponseError>>

    /**
     * PUT
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [url] the url
     * @param [requestBodyJsonClass] the class to serialize the request body to json
     * @param [requestBody] the request body to be converted to json
     * @param [mediaType] the media type for the request body, defaults to "application/json"
     * @param [headers] any headers that need to be added to the request
     * */
    abstract fun <Result: Any, Input: Any> put(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>?,
        mediaType: String? = "application/json",
        headers: Map<String, String>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

    /**
     * POST
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [url] the url
     * @param [requestBodyJsonClass] the class to serialize the request body to json
     * @param [requestBody] the request body to be converted to json
     * @param [mediaType] the media type for the request body, defaults to "application/json"
     * @param [headers] any headers that need to be added to the request
     * */
    abstract fun <Result: Any, Input: Any> post(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String? = "application/json",
        headers: Map<String, String>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

    /**
     * DELETE
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [url] the url
     * @param [requestBodyJsonClass] OPTIONAL: the class to serialize the request body to json
     * @param [requestBody] OPTIONAL: the request body to be converted to json
     * @param [mediaType] OPTIONAL: the media type for the request body
     * @param [headers] any headers that need to be added to the request
     * */
    abstract fun <Result: Any, Input: Any> delete(
        url: String,
        responseJsonSerializer: KSerializer<Result>,
        requestBodyPair: Pair<Input, KSerializer<Input>>? = null,
        mediaType: String? = null,
        headers: Map<String, String>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

    @Throws(NullPointerException::class, IOException::class)
    abstract suspend fun <T: Any> call(
        responseJsonSerializer: KSerializer<T>,
        request: Request,
        useExtendedNetworkCallClient: Boolean = false
    ): T

    @Throws(NullPointerException::class, IOException::class)
    abstract suspend fun <T: Any> callList(
        responseJsonClass: Class<T>,
        request: Request,
        useExtendedNetworkCallClient: Boolean = false
    ): List<T>

}
