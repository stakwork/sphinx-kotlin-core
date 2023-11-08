package chat.sphinx.concepts.network.query.message.model

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageDto(
    val chat_id: Long? = null,
    val contact_id: Long? = null,
    val amount: Long,
    val message_price: Long,
    val reply_uuid: String? = null,
    val text: String? = null,
    val remote_text_map: Map<String, String>? = null,
    val media_key_map: Map<String, String>? = null,
    val media_type: String? = null,
    val muid: String? = null,
    val price: Long? = null,
    val boost: Boolean = false,
    val call: Boolean = false,
    val pay: Boolean = false,
    val thread_uuid: String? = null
) {
    init {
        require(!(chat_id == null && contact_id == null)) {
            "A chat_id and/or contact_id is required"
        }

        require(
            (remote_text_map == null && text == null) ||
            (remote_text_map != null && text != null)
        ) {
            "remote_text_map && text must both be either null, or not null"
        }

        require(remote_text_map?.isEmpty() != true) {
            "remote_text_map cannot be empty"
        }

        require(text?.isEmpty() != true) {
            "text cannot be empty"
        }


        require(
            (media_key_map == null && media_type == null && muid == null) ||
            (media_key_map != null && media_type != null && muid != null)
        ) {
            "media_key_map, media_type, and muid must all be either null, or not null"
        }

        require(media_key_map?.isEmpty() != true) {
            "media_key_map cannot be empty"
        }

        require(media_type?.isEmpty() != true) {
            "media_type cannot be empty"
        }

        require(muid?.isEmpty() != true) {
            "muid cannot be empty"
        }

        require(!(remote_text_map == null && media_key_map == null && !pay)) {
            "Both remote_text_map and media_key_map cannot be null unless it's a tribe payment"
        }
    }
}
