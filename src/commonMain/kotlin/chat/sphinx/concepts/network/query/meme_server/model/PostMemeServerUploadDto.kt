package chat.sphinx.concepts.network.query.meme_server.model

import chat.sphinx.serialization.SphinxBoolean
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// {
// "muid":"DldRwCblQ7Loqy6wYJnaodHl30d3j3eH-qtFzfEv46g=",
// "owner_pub_key":"A5qCmBhpsOHKpQIs2_-r2freTUDP_LdPLWY_VE2MrHn4",
// "name":"image",
// "description":"description",
// "price":0,
// "tags":[],
// "filename":"IMG_2021_07_01_07_42_57_387.jpg",
// "ttl":31536000,
// "size":0,
// "mime":"image/jpg",
// "created":"2021-07-01T11:43:04.361585638Z",
// "updated":"2021-07-01T11:43:04.361585638Z",
// "expiry":null,
// "width":0,
// "height":0,
// "template":false
// }
@Serializable
data class PostMemeServerUploadDto(
    val muid: String,
    val owner_pub_key: String,
    val name: String,
    val description: String,
    val price: Long,
//    val tags: List<String>,
    val filename: String,
    val ttl: Long,
    val size: Long,
    val mime: String,
    val created: String,
    val updated: String,
    val expiry: String? = null,
    val width: Long,
    val height: Long,
    val template: SphinxBoolean? = null,
) {
    @Transient
    val templateActual: Boolean = template?.value ?: false
}