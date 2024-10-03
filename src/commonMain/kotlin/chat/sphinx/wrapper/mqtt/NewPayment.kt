package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Payment(
    val scid: Long? = null,
    val amt_msat: Long? = null,
    val rhash: String? = null,
    val ts: Long? = null,
    val remote: Boolean? = null,
    val msg_idx: Long? = null
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
