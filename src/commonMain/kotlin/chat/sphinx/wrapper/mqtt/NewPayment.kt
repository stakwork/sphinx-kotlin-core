package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Payment(
    val scid: Long?,
    val amt_msat: Long?,
    val rhash: String?,
    val ts: Long?,
    val remote: Boolean?,
    val msg_idx: Long?
) {
    companion object {
        fun String.toPaymentsList(): List<Payment>? {
            return try {
                Json.decodeFromString<List<Payment>>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
