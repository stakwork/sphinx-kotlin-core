package chat.sphinx.wrapper.mqtt

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class PubkeyDto(
   val pubkey: String
)

@Serializable
data class HopsDto(
   val pubkeys: List<PubkeyDto>
)

@Throws(AssertionError::class)
fun HopsDto.toJson(): String {
   return Json.encodeToString(this)
}

fun String.hopsDtoOrNull(): HopsDto? {
   return try {
      this.toHopsDto()
   } catch (e: Exception) {
      null
   }
}

@Throws(Exception::class)
fun String.toHopsDto(): HopsDto {
   return Json.decodeFromString(this)
}
