package chat.sphinx.features.network.query.version

import chat.sphinx.concepts.network.query.version.NetworkQueryVersion
import chat.sphinx.concepts.network.query.version.model.AppVersionsDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.version.model.GetAppVersionsRelayResponse
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow

class NetworkQueryVersionImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryVersion() {

    companion object {
        private const val ENDPOINT_APP_VERSIONS = "/app_versions"
    }

    override fun getAppVersions(
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<AppVersionsDto, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonClass = GetAppVersionsRelayResponse::class.java,
            relayEndpoint = ENDPOINT_APP_VERSIONS,
            relayData = relayData
        )
}