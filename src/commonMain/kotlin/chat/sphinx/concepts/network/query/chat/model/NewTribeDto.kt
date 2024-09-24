package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class NewTribeDto(
    val pubkey: String,
    val route_hint: String,
    val name: String,
    val description: String?,
    val tags: Array<String> = arrayOf(),
    val img: String?,
    val owner_alias: String?,
    val price_per_message: Long = 0,
    val price_to_join: Long = 0,
    val escrow_amount: Long = 0,
    val escrow_millis: Long = 0,
    val unlisted: Boolean?,
    val private: Boolean?,
    val created: Long?,
    val updated: Long?,
    val member_count: Int?,
    val last_active: Int?,
    val unique_name: String?,
    val pin: String?,
    val app_url: String?,
    val feed_url: String?,
    val feed_type: Int?
) {

    var amount: Long? = null
    var host: String? = null
    var uuid: String? = null

    var joined: Boolean? = null

    @SerialName("my_alias")
    var myAlias: String? = null

    @Transient
    var profileImgFile: File? = null

    fun setProfileImageFile(img: File?) {
        this.profileImgFile?.let {
            try {
                it.delete()
            } catch (e: Exception) {
            }
        }
        this.profileImgFile = img
    }

    fun set(
        host: String?,
        tribePubKey: String,
    ) {
        this.host = host
        this.uuid = tribePubKey
    }

    fun toJsonString(): String {
        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
        return json.encodeToString(NewTribeDto.serializer(), this)
    }

    fun getPricePerMessageInSats(): Long {
        return if (price_per_message == 0L) 0L else price_per_message / 1000
    }

    fun getPriceToJoinInSats(): Long {
        return if (price_to_join == 0L) 0L else price_to_join / 1000
    }

    fun getEscrowAmountInSats(): Long {
        return if (escrow_amount == 0L) 0L else escrow_amount / 1000
    }

}

fun Long.escrowMillisToHours(): Long {
    if (this == 0L) {
        return 0L
    }
    return this / (1000 * 60 * 60)
}
