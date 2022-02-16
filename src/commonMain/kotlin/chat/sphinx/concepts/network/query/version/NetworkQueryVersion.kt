package chat.sphinx.concepts.network.query.version

import chat.sphinx.concepts.network.query.version.model.AppVersionsDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryVersion {

    ///////////
    /// GET ///
    ///////////
    abstract fun getAppVersions(
        relayData: Pair<AuthorizationToken, RelayUrl>? = null
    ): Flow<LoadResponse<AppVersionsDto, ResponseError>>

}