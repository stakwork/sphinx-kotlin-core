package chat.sphinx.concepts.network.query.save_profile

import chat.sphinx.concepts.network.query.save_profile.model.DeletePeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.GetExternalRequestDto
import chat.sphinx.concepts.network.query.save_profile.model.PeopleProfileDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQuerySaveProfile {

    abstract fun getExternalRequestByKey(
        host: String,
        key: String
    ): Flow<LoadResponse<GetExternalRequestDto, ResponseError>>

    abstract fun savePeopleProfile(
        profile: PeopleProfileDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>

    abstract fun deletePeopleProfile(
        deletePeopleProfileDto: DeletePeopleProfileDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>
}