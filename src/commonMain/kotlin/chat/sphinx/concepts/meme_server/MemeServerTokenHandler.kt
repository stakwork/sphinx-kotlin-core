package chat.sphinx.concepts.meme_server

import chat.sphinx.wrapper.meme_server.AuthenticationToken
import chat.sphinx.wrapper.message.media.token.MediaHost

abstract class MemeServerTokenHandler {
    abstract suspend fun retrieveAuthenticationToken(
        mediaHost: MediaHost
    ): AuthenticationToken?
}
