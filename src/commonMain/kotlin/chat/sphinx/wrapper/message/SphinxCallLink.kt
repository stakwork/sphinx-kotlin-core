package chat.sphinx.wrapper.message

import chat.sphinx.utils.platform.getCurrentTimeInMillis
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
inline fun String.toSphinxCallLink(): SphinxCallLink? =
    try {
        SphinxCallLink(this)
    } catch (e: IllegalArgumentException) {
        null
    }

inline val String.isValidSphinxCallLink: Boolean
    get() = isNotEmpty() && matches("^${SphinxCallLink.REGEX}\$".toRegex())

inline val String.isValidJitsiCallLink: Boolean
    get() = isNotEmpty() && startsWith(SphinxCallLink.DEFAULT_CALL_SERVER_URL)

inline val String.isValidLiveKitCallLink: Boolean
    get() = isNotEmpty() && startsWith(SphinxCallLink.NEW_CALL_SERVER_URL)


@JvmInline
value class SphinxCallLink(val value: String) {

    companion object {
        const val REGEX = "https:\\/\\/.*\\/sphinx\\.call\\..*"

        const val CALL_SERVER_URL_KEY = "meeting-server-url"
        const val DEFAULT_CALL_SERVER_URL = "https://jitsi.sphinx.chat"
        const val NEW_CALL_SERVER_URL = "https://chat.sphinx.chat/rooms"
        private const val CALL_ROOM_NAME = "sphinx.call"

        const val AUDIO_ONLY_PARAM = "config.startAudioOnly"

        fun newCallLink(
            customServerUrl: String?,
            startAudioOnly: Boolean
        ): String? {
            val currentTime = getCurrentTimeInMillis()
            val linkString = "${customServerUrl ?: NEW_CALL_SERVER_URL}/$CALL_ROOM_NAME.$currentTime"

            return linkString.toSphinxCallLink()?.value
        }

        fun newCallLinkMessage(
            customServerUrl: String?,
            startAudioOnly: Boolean,
        ): String? {
            val currentTime = System.currentTimeMillis()
            val linkString = "${customServerUrl ?: NEW_CALL_SERVER_URL}/$CALL_ROOM_NAME.$currentTime"

            linkString.toSphinxCallLink()?.let { sphinxCallLink ->
                val callLinkMessage = CallLinkMessage(
                    sphinxCallLink,
                    false,
                    ""
                )

                callLinkMessage.toJson()?.let { jsonLink ->
                    return "${CallLinkMessage.MESSAGE_PREFIX}$jsonLink"
                }
            }
            return null
        }
    }

    init {
        require(value.isValidSphinxCallLink || value.isValidJitsiCallLink || value.isValidLiveKitCallLink) {
            "Invalid Sphinx Call Link"
        }
    }

    inline val startAudioOnly : Boolean
        get() = getParameter(AUDIO_ONLY_PARAM).toBoolean()

    inline val audioCallLink : String
        get() {
            if (value.contains("#$AUDIO_ONLY_PARAM=true")) {
                return value
            }
            return "$value#$AUDIO_ONLY_PARAM=true"
        }

    inline val videoCallLink : String
        get() = value.replace("#$AUDIO_ONLY_PARAM=true", "")

    inline val callServer : String
        get() = value.substringBefore("sphinx.call")

    inline val isJitsiLink: Boolean
        get() = value.isValidJitsiCallLink

    inline val callRoom : String
        get() = "sphinx.call." + value.substringAfter("sphinx.call.").substringBefore("#").substringBefore("?")

    fun getParameter(k: String): String? {
        val parameters = value.substringAfter("#").split("&")
        for (parameter in parameters) {
            val paramComponents = parameter.split("=")
            val key:String? = if (paramComponents.isNotEmpty()) paramComponents.elementAtOrNull(0) else null
            val value:String? = if (paramComponents.size > 1) paramComponents.elementAtOrNull(1) else null

            if (key == k) return value
        }
        return null
    }

}