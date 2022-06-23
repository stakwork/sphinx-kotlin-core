package chat.sphinx.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object SphinxBooleanSerializable : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SphinxBoolean", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Any) {
        val string = value.toString()
        if ((0..1).contains(string.toIntOrNull())) {
            encoder.encodeBoolean(
                string.toInt() == 1
            )
        } else {
            string.toBooleanStrictOrNull()?.let { encoder.encodeBoolean(it) }
        }
    }

    override fun deserialize(decoder: Decoder): SphinxBoolean {
        return try {
            SphinxBoolean(decoder.decodeBoolean())
        } catch(e: Exception) {
            null
        } ?: try {
            SphinxBoolean(decoder.decodeInt())
        } catch (e: Exception) {
            SphinxBoolean(false)
        }
    }
}