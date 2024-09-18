package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MsgsCounts(
    val total: Long?,
    val ok_key: Long?,
    val first_for_each_scid: Long?,
    val total_highest_index: Long?,
    val ok_key_highest_index: Long?,
    val first_for_each_scid_highest_index: Long?
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
