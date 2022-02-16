package chat.sphinx.concepts.network.query.invite

import chat.sphinx.concepts.network.query.invite.model.HubLowestNodePriceResponse
import chat.sphinx.concepts.network.query.invite.model.HubRedeemInviteResponse
import chat.sphinx.concepts.network.query.invite.model.PayInviteDto
import chat.sphinx.concepts.network.query.invite.model.RedeemInviteResponseDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.invite.InviteString
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryInvite {

    ///////////
    /// GET ///
    ///////////

    ///////////
    /// PUT ///
    ///////////

    ////////////
    /// POST ///
    ////////////
//    app.post('/invites', invites.createInvite)
//    app.post('/invites/:invite_string/pay', invites.payInvite)
//    app.post('/invites/finish', invites.finishInvite)

    abstract fun getLowestNodePrice(): Flow<LoadResponse<HubLowestNodePriceResponse, ResponseError>>

    // TODO: Return RedeemInviteResponse
    abstract fun redeemInvite(
        inviteString: InviteString
    ): Flow<LoadResponse<HubRedeemInviteResponse, ResponseError>>

    abstract fun finishInvite(
        inviteString: String
    ): Flow<LoadResponse<RedeemInviteResponseDto, ResponseError>>

    abstract fun payInvite(
        inviteString: InviteString
    ): Flow<LoadResponse<PayInviteDto, ResponseError>>

    //////////////
    /// DELETE ///
    //////////////
}
