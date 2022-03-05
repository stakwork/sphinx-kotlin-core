package chat.sphinx.concepts.network.relay_call

import chat.sphinx.concepts.network.call.NetworkCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

/**
 * Methods for GET/PUT/POST/DELETE that are specific to interacting with Relay.
 *
 * It automatically:
 *  - Retrieves from persistent storage the [RelayUrl] and [AuthorizationToken]
 *  for all queries if `null` is passed for that argument.
 *  - Adds the Authorization RequestHeader.
 *  - Handles [RelayResponse.success] when `false` by returning a [ResponseError]
 *  instead.
 *  - Json serialization/deserialization
 * */
abstract class NetworkRelayCall: NetworkCall() {

    /**
     * GET
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [relayEndpoint] the endpoint to append to the [RelayUrl], ex: /contacts
     * @param [additionalHeaders] any additional headers to add to the call
     * @param [relayData] if not `null`, will override the auto-fetching of persisted user data
     * */
    abstract fun <
            T: Any,
            V: RelayResponse<T>
            > relayGet(
        responseJsonSerializer: KSerializer<V>,
        relayEndpoint: String,
        additionalHeaders: Map<String, String>? = null,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null,
        useExtendedNetworkCallClient: Boolean = false,
    ): Flow<LoadResponse<T, ResponseError>>

    /**
     * PUT
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [relayEndpoint] the endpoint to append to the [RelayUrl], ex: /contacts
     * @param [requestBodyJsonClass] the class to serialize the request body to json
     * @param [requestBody] the request body to be converted to json
     * @param [mediaType] the media type for the request body, defaults to "application/json"
     * @param [additionalHeaders] any additional headers to add to the call
     * @param [relayData] if not `null`, will override the auto-fetching of persisted user data
     * */
    abstract fun <
            Result: Any,
            Input: Any,
            Output: RelayResponse<Result>
            > relayPut(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>? = null,
        mediaType: String? = "application/json",
        additionalHeaders: Map<String, String>? = null,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

    // TODO: Remove and replace all uses with post (DO NOT USE THIS METHOD FOR NEW CODE)
    /**
     * POST
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [relayEndpoint] the endpoint to append to the [RelayUrl], ex: /contacts
     * @param [requestBodyJsonClass] the class to serialize the request body to json
     * @param [requestBody] the request body to be converted to json
     * @param [mediaType] the media type for the request body, defaults to "application/json"
     * @param [relayUrl] unauthenticated relay URL
     * */
    @Deprecated(message = "do not use")
    abstract fun <
            Result: Any, Input: Any, Output: RelayResponse<Result>
            > relayUnauthenticatedPost(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String? = "application/json",
        relayUrl: RelayUrl,
    ): Flow<LoadResponse<Result, ResponseError>>

    /**
     * POST
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [relayEndpoint] the endpoint to append to the [RelayUrl], ex: /contacts
     * @param [requestBodyJsonClass] the class to serialize the request body to json
     * @param [requestBody] the request body to be converted to json
     * @param [mediaType] the media type for the request body, defaults to "application/json"
     * @param [additionalHeaders] any additional headers to add to the call
     * @param [relayData] if not `null`, will override the auto-fetching of persisted user data
     * */
    abstract fun <
            Result: Any, Input: Any, Output: RelayResponse<Result>
            > relayPost(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>,
        mediaType: String? = "application/json",
        additionalHeaders: Map<String, String>? = null,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

    /**
     * DELETE
     *
     * @param [responseJsonClass] the class to serialize the response json into
     * @param [relayEndpoint] the endpoint to append to the [RelayUrl], ex: /contacts
     * @param [requestBodyJsonClass] OPTIONAL: the class to serialize the request body to json
     * @param [requestBody] OPTIONAL: the request body to be converted to json
     * @param [mediaType] OPTIONAL: the media type for the request body
     * @param [additionalHeaders] any additional headers to add to the call
     * @param [relayData] if not `null`, will override the auto-fetching of persisted user data
     * */
    abstract fun <
            Result: Any, Input: Any, Output: RelayResponse<Result>
            > relayDelete(
        responseJsonSerializer: KSerializer<Output>,
        relayEndpoint: String,
        requestBodyPair: Pair<Input, KSerializer<Input>>? = null,
        mediaType: String? = null,
        additionalHeaders: Map<String, String>? = null,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null,
    ): Flow<LoadResponse<Result, ResponseError>>

}
