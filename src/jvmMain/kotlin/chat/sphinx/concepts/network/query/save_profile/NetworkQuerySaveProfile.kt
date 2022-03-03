package chat.sphinx.concepts.network.query.save_profile

import chat.sphinx.concepts.network.query.save_profile.model.DeletePeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.GetPeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.PeopleProfileDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow

abstract class NetworkQuerySaveProfile {

    abstract fun getPeopleProfileByKey(
        host: String,
        key: String
    ): Flow<LoadResponse<GetPeopleProfileDto, ResponseError>>

    abstract fun savePeopleProfile(
        profile: PeopleProfileDto,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>

    abstract fun deletePeopleProfile(
        deletePeopleProfileDto: DeletePeopleProfileDto,
        relayData: Pair<AuthorizationToken, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>
}