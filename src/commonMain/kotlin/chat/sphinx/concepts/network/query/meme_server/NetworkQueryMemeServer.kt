package chat.sphinx.concepts.network.query.meme_server

import chat.sphinx.concepts.network.query.meme_server.model.*
import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.meme_server.AuthenticationChallenge
import chat.sphinx.wrapper.meme_server.AuthenticationId
import chat.sphinx.wrapper.meme_server.AuthenticationSig
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.MediaType
import chat.sphinx.wrapper.message.media.token.MediaHost
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import okio.Path
import okio.Source

abstract class NetworkQueryMemeServer {

    abstract fun askAuthentication(
        memeServerHost: MediaHost = MediaHost.DEFAULT,
    ): Flow<LoadResponse<MemeServerAuthenticationDto, ResponseError>>

    abstract fun signChallenge(
        challenge: AuthenticationChallenge,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Flow<LoadResponse<MemeServerChallengeSigDto, ResponseError>>

    abstract fun verifyAuthentication(
        id: AuthenticationId,
        sig: AuthenticationSig,
        ownerPubKey: LightningNodePubKey,
        memeServerHost: MediaHost = MediaHost.DEFAULT,
    ): Flow<LoadResponse<MemeServerAuthenticationTokenDto, ResponseError>>

    abstract suspend fun getPaymentTemplates(
        authenticationToken: AuthenticationToken,
        memeServerHost: MediaHost = MediaHost.DEFAULT,
    ): Flow<LoadResponse<List<PaymentTemplateDto>, ResponseError>>

    abstract suspend fun uploadAttachmentEncrypted(
        authenticationToken: AuthenticationToken,
        mediaType: MediaType,
        file: Path,
        password: Password,
        memeServerHost: MediaHost = MediaHost.DEFAULT,
    ): Response<PostMemeServerUploadDto, ResponseError>

    abstract suspend fun uploadAttachment(
        authenticationToken: AuthenticationToken,
        mediaType: MediaType,
        source: Source,
        fileName: String,
        contentLength: Long?,
        memeServerHost: MediaHost = MediaHost.DEFAULT,
    ): Response<PostMemeServerUploadDto, ResponseError>
}
