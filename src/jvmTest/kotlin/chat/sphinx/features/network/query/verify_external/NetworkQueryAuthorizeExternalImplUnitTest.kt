package chat.sphinx.features.network.query.verify_external

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.fail

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class NetworkQueryAuthorizeExternalImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `verifyExternal returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqAuthorizeExternal.verifyExternal().collect { loadResponse ->

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