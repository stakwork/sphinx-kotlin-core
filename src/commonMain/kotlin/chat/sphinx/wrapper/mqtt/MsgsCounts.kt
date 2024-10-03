package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MsgsCounts(
    val total: Long? = null,
    val ok_key: Long? = null,
    val first_for_each_scid: Long? = null,
    val total_highest_index: Long? = null,
    val ok_key_highest_index: Long? = null,
    val first_for_each_scid_highest_index: Long? = null
) {
    companion object {
        fun String.toMsgsCounts(): MsgsCounts? {
            return try {
                Json.decodeFromString<MsgsCounts>(this)
            } catch (e: Exception) {
                null
            }
        }
    }
}
