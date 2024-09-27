package chat.sphinx.utils

import chat.sphinx.wrapper.message.SphinxCallLink
import com.russhwolf.settings.Settings
import java.util.regex.Pattern

class ServersUrlsHelper {

    private val settings: Settings = createPlatformSettings()

    companion object {
        const val ONION_STATE_KEY = "onion_state"
        const val NETWORK_MIXER_IP = "network_mixer_ip"
        const val TRIBE_SERVER_IP = "tribe_server_ip"
        const val DEFAULT_TRIBE_KEY = "default_tribe"
        const val ENVIRONMENT_TYPE = "environment_type"
        const val ROUTER_URL = "router_url"
        const val ROUTER_PUBKEY = "router_pubkey"
    }

    fun getMeetingServer(): String {
        val callServerUrl = SphinxCallLink.CALL_SERVER_URL_KEY
        val defaultCallServer = SphinxCallLink.DEFAULT_CALL_SERVER_URL
        return settings.getString(callServerUrl, defaultCallServer)
    }

    fun setMeetingServer(url: String?) {
        if (url == null || url.isEmpty() || !isValidURL(url)) {
            return
        } else {
            settings.putString(key = SphinxCallLink.CALL_SERVER_URL_KEY, value = url)
        }
    }

    private fun isValidURL(url: String?): Boolean {
        val regex = ("((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b"
                + "([-a-zA-Z0-9@:%._\\+~#?&//=]*)")
        val p = Pattern.compile(regex)
        if (url == null) {
            return false
        }
        val m = p.matcher(url)
        return m.matches()
    }

    fun getUserState(): String? {
        return settings.getString(ONION_STATE_KEY, "")
    }

    fun storeUserState(value: String?) {
        if (value != null) {
            settings.putString(ONION_STATE_KEY, value)
        }
    }

    fun getNetworkMixerIp(): String? {
        return settings.getString(NETWORK_MIXER_IP, "")
    }

    fun storeNetworkMixerIp(value: String?) {
        if (value != null) {
            settings.putString(NETWORK_MIXER_IP, value)
        }
    }

    fun getRouterUrl(): String? {
        return settings.getString(ROUTER_URL, "")
    }

    fun storeRouterUrl(value: String?) {
        if (value != null) {
            settings.putString(ROUTER_URL, value)
        }
    }

    fun getRouterPubkey(): String? {
        return settings.getString(ROUTER_PUBKEY, "")
    }

    fun storeRouterPubkey(value: String?) {
        if (value != null) {
            settings.putString(ROUTER_PUBKEY, value)
        }
    }

    fun getEnvironmentType(): Boolean {
        return settings.getBoolean(ENVIRONMENT_TYPE, false)
    }

    fun storeEnvironmentType(value: Boolean) {
        settings.putBoolean(ENVIRONMENT_TYPE, value)
    }

    fun getTribeServerIp(): String? {
        return settings.getString(TRIBE_SERVER_IP, "")
    }

    fun storeTribeServerIp(value: String?) {
        if (value != null) {
            settings.putString(TRIBE_SERVER_IP, value)
        }
    }

    fun getDefaultTribe(): String? {
        return settings.getString(DEFAULT_TRIBE_KEY, "")
    }

    fun storeDefaultTribe(value: String?) {
        if (value != null) {
            settings.putString(DEFAULT_TRIBE_KEY, value)
        }
    }
}