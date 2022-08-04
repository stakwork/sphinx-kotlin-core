package chat.sphinx.utils

import chat.sphinx.wrapper.message.SphinxCallLink
import com.russhwolf.settings.Settings
import java.util.regex.Pattern

class ServersUrlsHelper {

    private val settings: Settings = createPlatformSettings()

    fun getMeetingServer(): String {

        val callServerUrl = SphinxCallLink.CALL_SERVER_URL_KEY
        val defaultCallServer = SphinxCallLink.DEFAULT_CALL_SERVER_URL

        return settings.getString(callServerUrl, defaultCallServer)

    }

    fun setMeetingServer(url: String?) {
        if(url == null || url.isEmpty() || !isValidURL(url)){
            return
        }
        else {
            settings.putString(key = SphinxCallLink.CALL_SERVER_URL_KEY, value = url)
        }
    }

    private fun isValidURL(url: String?): Boolean {
        val regex = ("((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]"
                + "{2,256}\\.[a-z]"
                + "{2,6}\\b([-a-zA-Z0-9@:%"
                + "._\\+~#?&//=]*)")

        val p = Pattern.compile(regex)
        if (url == null) {
            return false
        }
        val m = p.matcher(url)

        return m.matches()
    }
}