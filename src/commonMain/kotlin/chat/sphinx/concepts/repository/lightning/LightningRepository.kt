package chat.sphinx.concepts.repository.lightning

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.lightning.NodeBalance
import chat.sphinx.wrapper.lightning.NodeBalanceAll
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LightningRepository {
    val networkRefreshBalance: Flow<LoadResponse<Boolean, ResponseError>>
    suspend fun getAccountBalance(): StateFlow<NodeBalance?>
    suspend fun getAccountBalanceAll(
        relayData: Pair<AuthorizationToken, RelayUrl>? = null
    ): Flow<LoadResponse<NodeBalanceAll, ResponseError>>
}
