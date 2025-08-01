package chat.sphinx.features.network.query.save_profile

import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.save_profile.model.*
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.message.MessagePerson
import chat.sphinx.wrapper.message.host
import chat.sphinx.wrapper.message.uuid
import kotlinx.coroutines.flow.Flow

class NetworkQuerySaveProfileImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQuerySaveProfile() {

    companion object {
        private const val ENDPOINT_SAVE_KEY = "https://%s/save/%s"
        private const val ENDPOINT_PROFILE = "/profile"
        private const val ENDPOINT_TRIBE_MEMBER_PROFILE = "https://%s/person/uuid/%s"

        private const val GIPHY_BASE_URL = "https://api.giphy.com/v1/gifs"
        private const val SEARCH_ENDPOINT = "$GIPHY_BASE_URL/search"
        private const val TRENDING_ENDPOINT = "$GIPHY_BASE_URL/trending"
    }

    override fun getExternalRequestByKey(
        host: String,
        key: String
    ): Flow<LoadResponse<GetExternalRequestDto, ResponseError>> =
        networkRelayCall.get(
            url = String.format(
                ENDPOINT_SAVE_KEY,
                host,
                key
            ),
            responseJsonSerializer = GetExternalRequestDto.serializer(),
        )


    override fun getTribeMemberProfile(person: MessagePerson
    ): Flow<LoadResponse<TribeMemberProfileDto, ResponseError>> =
        networkRelayCall.get(
            url = String.format(
                ENDPOINT_TRIBE_MEMBER_PROFILE,
                person.host(),
                person.uuid()
            ),
            responseJsonSerializer = TribeMemberProfileDto.serializer()
        )

    override fun searchGifs(
        query: String,
        offset: Int,
        limit: Int,
        giphyApiKey : String,
        ): Flow<LoadResponse<GiphyResponse, ResponseError>> {
        val url = "$SEARCH_ENDPOINT?api_key=${giphyApiKey}&q=$query&limit=$limit&offset=$offset"
        return networkRelayCall.get(
            url = url,
            responseJsonSerializer = GiphyResponse.serializer(),
            useExtendedNetworkCallClient = true
        )
    }

    override fun getTrendingGifs(
        offset: Int,
        limit: Int,
        giphyApiKey: String
    ): Flow<LoadResponse<GiphyResponse, ResponseError>> {
        val url = "$TRENDING_ENDPOINT?api_key=${giphyApiKey}&limit=$limit&offset=$offset"
        return networkRelayCall.get(
            url = url,
            responseJsonSerializer = GiphyResponse.serializer(),
            useExtendedNetworkCallClient = true
        )
    }

}
