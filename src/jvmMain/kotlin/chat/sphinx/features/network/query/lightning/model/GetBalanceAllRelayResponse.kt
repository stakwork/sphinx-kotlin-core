package chat.sphinx.features.network.query.lightning.model

import chat.sphinx.concepts.network.query.lightning.model.balance.BalanceAllDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetBalanceAllRelayResponse(
    override val success: Boolean,
    override val response: BalanceAllDto? = null,
    override val error: String? = null
): RelayResponse<BalanceAllDto>()
