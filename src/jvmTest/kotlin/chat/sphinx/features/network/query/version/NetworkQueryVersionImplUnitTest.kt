package chat.sphinx.features.network.query.version

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.fail

class NetworkQueryVersionImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `getAppVersions returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqVersion.getAppVersions().collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            // will fail on error
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {
                        }
                        is LoadResponse.Loading -> {
                        }
                    }

                }
            }
        }
}