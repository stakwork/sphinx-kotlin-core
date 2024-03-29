package chat.sphinx.features.meme_input_stream

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.network.client.cache.NetworkClientCache
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.FileName
import chat.sphinx.wrapper.message.media.MediaKeyDecrypted
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.InputStream

class MemeInputStreamHandlerImpl(
    private val dispatchers: CoroutineDispatchers,
    private val networkClientCache: NetworkClientCache,
) : MemeInputStreamHandler() {

    override suspend fun retrieveMediaInputStream(
        url: String,
        authenticationToken: AuthenticationToken?,
        mediaKeyDecrypted: MediaKeyDecrypted?
    ): Pair<InputStream?, FileName?>? {
        return url.toHttpUrlOrNull()?.let { httpUrl ->
            MemeInputStreamRetriever(
                httpUrl,
                authenticationToken,
                mediaKeyDecrypted
            ).getMemeInputStream(dispatchers, networkClientCache.getCachingClient())
        }
    }
}
