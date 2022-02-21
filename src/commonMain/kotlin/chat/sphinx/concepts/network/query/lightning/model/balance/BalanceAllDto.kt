package chat.sphinx.concepts.network.query.lightning.model.balance

import kotlinx.serialization.Serializable

@Serializable
data class BalanceAllDto(
    val local_balance: Long,
    val remote_balance: Long,
)
