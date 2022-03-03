package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.balance.BalanceDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetBalanceRelayResponse(
    override val success: Boolean,
    override val response: BalanceDto?,
    override val error: String?
): RelayResponse<BalanceDto>()
