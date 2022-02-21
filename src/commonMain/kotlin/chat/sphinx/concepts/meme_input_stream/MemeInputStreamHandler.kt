package chat.sphinx.concepts.meme_input_stream

import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.MediaKeyDecrypted
import com.stakwork.koi.InputStream


abstract class MemeInputStreamHandler {
    abstract suspend fun retrieveMediaInputStream(
        url: String,
        authenticationToken: AuthenticationToken?,
        mediaKeyDecrypted: MediaKeyDecrypted?
    ): InputStream?
}
