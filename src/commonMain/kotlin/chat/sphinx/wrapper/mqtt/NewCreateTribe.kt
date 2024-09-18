package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class NewCreateTribe(
    val pubkey: String?,
    val route_hint: String?,
    val name: String,
    val description: String,
    val tags: List<String>,
    val img: String?,
    val price_per_message: Long?,
    val price_to_join: Long?,
    val escrow_amount: Long?,
    val escrow_millis: Long?,
    val unlisted: Boolean?,
    val private: Boolean?,
    val app_url: String?,
    val feed_url: String?,
    val feed_type: Int?,
    val created: Long?,
    val updated: Long?,
    val member_count: Int?,
    val last_active: Long?,
    val owner_alias: String
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun getPricePerMessageInSats(): Long {
        return if (price_per_message == 0L || price_per_message == null) 0L else price_per_message / 1000
    }

    fun getPriceToJoinInSats(): Long {
        return if (price_to_join == 0L || price_to_join == null) 0L else price_to_join / 1000
    }

    fun getEscrowAmountInSats(): Long {
        return if (escrow_amount == 0L || escrow_amount == null) 0L else escrow_amount / 1000
    }

    companion object {
        @Throws(Exception::class, IllegalArgumentException::class)
        fun String.toNewCreateTribe(): NewCreateTribe {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for NewCreateTribe")
        }
    }
}
