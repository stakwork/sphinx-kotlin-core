package chat.sphinx.concepts.network.query.chat.model

import chat.sphinx.serialization.SphinxBoolean
import chat.sphinx.utils.platform.getFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
import okio.Path

@Serializable
data class TribeDto(
    val name: String,
    val description: String,
    val img: String? = null,
    val tags: Array<String> = arrayOf(),
    val group_key: String,
    val owner_pubkey: String,
    val owner_route_hint: String? = null,
    val owner_alias: String? = null,
    val price_to_join: Long = 0,
    val price_per_message: Long = 0,
    val escrow_amount: Long = 0,
    val escrow_millis: Long = 0,
    val unlisted: SphinxBoolean,
    val private: SphinxBoolean,
    val deleted: SphinxBoolean,
    val app_url: String? = null,
    val feed_url: String? = null,
    val feed_type: Int? = null,
) {

    var amount: Long? = null
    var host: String? = null
    var uuid: String? = null

    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("my_alias")
    var myAlias: String? = null

    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("my_photo_url")
    var myPhotoUrl: String? = null

    @Transient
    var profileImgFile: Path? = null

    fun setProfileImageFile(imgPath: Path?) {
        this.profileImgFile?.let {
            try {
                getFileSystem().delete(it)
            } catch (e: Exception) {}
        }
        this.profileImgFile = imgPath
    }

    val hourToStake: Long
        get() = (escrow_millis) / 60 / 60 / 1000

    fun set(host: String?, uuid: String) {
        this.host = host
        this.uuid = uuid
    }
}
