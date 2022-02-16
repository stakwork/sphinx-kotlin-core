package chat.sphinx.features.meme_input_stream

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.network.client.crypto.CryptoHeader
import chat.sphinx.utils.platform.InputStream
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.MediaKeyDecrypted
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly


internal data class MemeInputStreamRetriever(
    val url: HttpUrl,
    val authenticationToken: AuthenticationToken?,
    val mediaKeyDecrypted: MediaKeyDecrypted?
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getMemeInputStream(
        dispatchers: CoroutineDispatchers,
        okHttpClient: OkHttpClient
    ): InputStream? {
        val request = Request.Builder().apply {
            url(url)
            authenticationToken?.let {
                addHeader(authenticationToken.headerKey, authenticationToken.headerValue)
            }

            mediaKeyDecrypted?.value?.let { key ->
                val header = CryptoHeader.Decrypt.Builder()
                    .setScheme(CryptoScheme.Decrypt.JNCryptor)
                    .setPassword(key)
                    .build()

                addHeader(header.key, header.value)
            }
        }.build()

        var response: Response?

        withContext(dispatchers.io) {
            response =
                try {
                    okHttpClient.newCall(request).execute()
                } catch (e: Exception) {
                    null
                }

            if (response?.isSuccessful == null) {
                response?.body?.closeQuietly()
            }
        }

        return response?.body?.source()?.inputStream()

    }
}
