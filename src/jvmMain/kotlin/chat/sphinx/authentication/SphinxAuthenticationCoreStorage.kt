package chat.sphinx.authentication

import chat.sphinx.concepts.authentication.data.AuthenticationStorage.Companion.CREDENTIALS
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.features.authentication.core.data.AuthenticationCoreStorage
import chat.sphinx.utils.createPlatformSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

open class SphinxAuthenticationCoreStorage(
    val dispatchers: CoroutineDispatchers
): AuthenticationCoreStorage() {
    companion object {
        const val AUTHENTICATION_STORAGE_MASTER_KEY = "_sphinx_master_key"
        const val AUTHENTICATION_STORAGE_NAME = "sphinx_authentication"
    }

    private val settings: Settings = createPlatformSettings()

    override suspend fun saveCredentialString(credentialString: CredentialString) {
        settings.putString(key = CREDENTIALS, value = credentialString.value)
    }

    override suspend fun retrieveCredentialString(): CredentialString? {
        return getString(CREDENTIALS, null)?.let { string ->
            CredentialString(string)
        }

    }

    override suspend fun getString(key: String, defaultValue: String?): String? {
        return settings.getStringOrNull(key = key) ?: defaultValue
    }

    override suspend fun putString(key: String, value: String?) {
        if (key == CREDENTIALS) {
            throw IllegalArgumentException(
                "The value for key $CREDENTIALS cannot be overwritten from this method"
            )
        }
        withContext(dispatchers.io) {
            value?.let {
                settings.putString(key, value)
            }
        }
    }

    override suspend fun removeString(key: String) {
        if (key == CREDENTIALS) {
            throw IllegalArgumentException(
                "The value for key $CREDENTIALS cannot be removed from this method"
            )
        }
        withContext(dispatchers.io) {
            settings.remove(key)
        }
    }

    /**
     * This should only be called from [SphinxKeyRestore] upon failure. This should
     * **never** be called elsewhere, except for good reason, thus it being only in
     * the implementation which lower layered modules have no idea about.
     * */
    suspend fun clearAuthenticationStorage() {
        withContext(dispatchers.io) {
            settings.clear()
        }
    }
}
