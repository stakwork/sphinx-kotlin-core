package chat.sphinx.features.meme_input_stream

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.network.client.crypto.CryptoHeader
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.MediaKeyDecrypted
import com.stakwork.koi.InputStream
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly


internal data class MemeInputStreamRetriever(
    val url: Url,
    val authenticationToken: AuthenticationToken?,
    val mediaKeyDecrypted: MediaKeyDecrypted?
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getMemeInputStream(
        dispatchers: CoroutineDispatchers,
        okHttpClient: HttpClient
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
