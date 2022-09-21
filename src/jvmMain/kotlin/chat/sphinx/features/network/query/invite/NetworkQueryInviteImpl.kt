package chat.sphinx.features.network.query.invite

import chat.sphinx.concepts.network.query.invite.NetworkQueryInvite
import chat.sphinx.concepts.network.query.invite.model.*
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.invite.model.FinishInviteRelayResponse
import chat.sphinx.features.network.query.invite.model.PayInviteResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.invite.InviteString
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class NetworkQueryInviteImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryInvite() {

    companion object {
        private const val ENDPOINT_SIGNUP = "/api/v1/signup"
        private const val ENDPOINT_SIGNUP_FINISH = "/invites/finish"
        private const val ENDPOINT_LOWEST_PRICE = "/api/v1/nodes/pricing"
        private const val ENDPOINT_INVITE_PAY = "/invites/%s/pay"

        private const val HUB_URL = "https://hub.sphinx.chat"
    }

    override fun getLowestNodePrice(): Flow<LoadResponse<HubLowestNodePriceResponse, ResponseError>> {
        return networkRelayCall.get(
            url = HUB_URL + ENDPOINT_LOWEST_PRICE,
            responseJsonSerializer = HubLowestNodePriceResponse.serializer(),
        )
    }


    override fun redeemInvite(
        inviteString: String
    ): Flow<LoadResponse<HubRedeemInviteResponse, ResponseError>> {
        return networkRelayCall.post(
            url = HUB_URL + ENDPOINT_SIGNUP,
            responseJsonSerializer = HubRedeemInviteResponse.serializer(),
            requestBodyPair = Pair(
                InvitePostDto(inviteString),
                InvitePostDto.serializer()
            )
        )
    }

    override fun finishInvite(
        inviteString: String
    ): Flow<LoadResponse<FinishInviteResponseDto, ResponseError>> {
        return networkRelayCall.relayPost(
            responseJsonSerializer = FinishInviteRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_SIGNUP_FINISH,
            requestBodyPair = Pair(
                InvitePostDto(inviteString),
                InvitePostDto.serializer()
            )
        )
    }

    override fun payInvite(
        inviteString: InviteString
    ): Flow<LoadResponse<PayInviteDto, ResponseError>> {
        return networkRelayCall.relayPost(
            responseJsonSerializer = PayInviteResponse.serializer(),
            relayEndpoint = "/invites/${inviteString.value}/pay" ,
            requestBodyPair = Pair(
                mapOf(Pair("", "")),
                Json.serializersModule.serializer()
            )
        )
    }
}
