package chat.sphinx.wrapper.contact

import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toDeviceId(): DeviceId? =
    try {
        DeviceId(this)
    } catch (e: IllegalArgumentException) {
        null
    }

@JvmInline
value class DeviceId(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "DeviceId cannot be empty"
        }
    }
}
