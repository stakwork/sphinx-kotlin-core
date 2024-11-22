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
    val description: String? = null,
    val tags: Array<String> = arrayOf(),
    val img: String? = null,
    val owner_alias: String? = null,
    val price_per_message: Long = 0,
    val price_to_join: Long = 0,
    val escrow_amount: Long = 0,
    val escrow_millis: Long = 0,
    val unlisted: Boolean? = null,
    val private: Boolean? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val member_count: Int? = null,
    val last_active: Int? = null,
    val unique_name: String? = null,
    val pin: String? = null,
    val app_url: String? = null,
    val second_brain_url: String? = null,
    val feed_url: String? = null,
    val feed_type: Int? = null
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
