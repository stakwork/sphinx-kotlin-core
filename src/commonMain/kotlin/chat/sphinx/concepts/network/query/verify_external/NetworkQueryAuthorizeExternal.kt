package chat.sphinx.concepts.network.query.verify_external

import chat.sphinx.concepts.network.query.verify_external.model.PersonInfoDto
import chat.sphinx.concepts.network.query.verify_external.model.SignBase64Dto
import chat.sphinx.concepts.network.query.verify_external.model.VerifyExternalDto
import chat.sphinx.concepts.network.query.verify_external.model.VerifyExternalInfoDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryAuthorizeExternal {

    abstract fun verifyExternal(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<VerifyExternalDto, ResponseError>>

    abstract fun signBase64(
        base64: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<SignBase64Dto, ResponseError>>

    abstract fun authorizeExternal(
        host: String,
        challenge: String,
        token: String,
        info: VerifyExternalInfoDto,
    ): Flow<LoadResponse<Any, ResponseError>>

    abstract fun getPersonInfo(
        host: String,
        publicKey: String
    ): Flow<LoadResponse<PersonInfoDto, ResponseError>>
}