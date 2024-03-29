package chat.sphinx.features.network.query.save_profile

import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.save_profile.model.DeletePeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.GetExternalRequestDto
import chat.sphinx.concepts.network.query.save_profile.model.PeopleProfileDto
import chat.sphinx.concepts.network.query.save_profile.model.TribeMemberProfileDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.save_profile.model.SaveProfileResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.message.MessagePerson
import chat.sphinx.wrapper.message.host
import chat.sphinx.wrapper.message.uuid
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

class NetworkQuerySaveProfileImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQuerySaveProfile() {

    companion object {
        private const val ENDPOINT_SAVE_KEY = "https://%s/save/%s"
        private const val ENDPOINT_PROFILE = "/profile"
        private const val ENDPOINT_TRIBE_MEMBER_PROFILE = "https://%s/person/uuid/%s"
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


    override fun savePeopleProfile(
        profile: PeopleProfileDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<Any, ResponseError>> =
        networkRelayCall.relayPost(
            relayEndpoint = ENDPOINT_PROFILE,
            requestBodyPair = Pair(
                profile,
                PeopleProfileDto.serializer()
            ),
            responseJsonSerializer = SaveProfileResponse.serializer(),
            relayData = relayData
        )

    override fun deletePeopleProfile(
        deletePeopleProfileDto: DeletePeopleProfileDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>?
    ): Flow<LoadResponse<Any, ResponseError>> =
        networkRelayCall.relayDelete(
            relayEndpoint = ENDPOINT_PROFILE,
            requestBodyPair = Pair(
                deletePeopleProfileDto,
                DeletePeopleProfileDto.serializer()
            ),
            responseJsonSerializer = SaveProfileResponse.serializer(),
            relayData = relayData,
            additionalHeaders = mapOf("Content-Type" to "application/json;charset=utf-8")
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

}
