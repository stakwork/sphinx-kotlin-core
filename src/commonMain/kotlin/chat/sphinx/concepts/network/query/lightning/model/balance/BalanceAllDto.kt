package chat.sphinx.concepts.network.query.lightning.model.balance

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BalanceAllDto(
    val local_balance: Long,
    val remote_balance: Long,
)
