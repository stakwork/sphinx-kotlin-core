package chat.sphinx.concepts.meme_input_stream

import chat.sphinx.utils.platform.InputStream
import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.MediaKeyDecrypted


abstract class MemeInputStreamHandler {
    abstract suspend fun retrieveMediaInputStream(
        url: String,
        authenticationToken: AuthenticationToken?,
        mediaKeyDecrypted: MediaKeyDecrypted?
    ): InputStream?
}
