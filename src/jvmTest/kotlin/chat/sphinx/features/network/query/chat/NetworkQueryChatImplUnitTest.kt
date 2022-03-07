package chat.sphinx.features.network.query.chat

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.fail

class NetworkQueryChatImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `getChats returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqChat.getChats().collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {}
                        is LoadResponse.Loading -> {}
                    }

                }

            }
        }
}
