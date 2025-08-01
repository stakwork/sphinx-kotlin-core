package chat.sphinx.concepts.network.query.save_profile

import chat.sphinx.concepts.network.query.save_profile.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.message.MessagePerson
import kotlinx.coroutines.flow.Flow

abstract class NetworkQuerySaveProfile {

    abstract fun getExternalRequestByKey(
        host: String,
        key: String
    ): Flow<LoadResponse<GetExternalRequestDto, ResponseError>>

    abstract fun getTribeMemberProfile(
        person: MessagePerson
    ): Flow<LoadResponse<TribeMemberProfileDto, ResponseError>>

    abstract fun searchGifs(
        query: String,
        offset: Int = 0,
        limit: Int = 25,
        giphyApiKey : String,
        ): Flow<LoadResponse<GiphyResponse, ResponseError>>

    abstract fun getTrendingGifs(
        offset: Int = 0,
        limit: Int = 25,
        giphyApiKey : String,
    ): Flow<LoadResponse<GiphyResponse, ResponseError>>
}
