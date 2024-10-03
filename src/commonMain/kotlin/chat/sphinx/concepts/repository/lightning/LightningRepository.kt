package chat.sphinx.concepts.repository.lightning

import chat.sphinx.concepts.network.query.lightning.model.lightning.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.lightning.NodeBalanceAll
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface LightningRepository {
    val networkRefreshBalance: MutableStateFlow<Long?>
    suspend fun getAccountBalanceAll(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<NodeBalanceAll, ResponseError>>

    suspend fun getActiveLSat(
        issuer: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ActiveLsatDto, ResponseError>>

    suspend fun signChallenge(
        challenge: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<SignChallengeDto, ResponseError>>

    suspend fun payLSat(
        payLSatDto: PayLsatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<PayLsatResponseDto, ResponseError>>

    suspend fun updateLSat(
        identifier: String,
        updateLSatDto: UpdateLsatDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<String, ResponseError>>
}
