package chat.sphinx.features.network.query.lightning

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.fail

class NetworkQueryLightningImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `getInvoices returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqLightning.getInvoices().collect { loadResponse ->

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

    @Test
    fun `getChannels returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqLightning.getChannels().collect { loadResponse ->

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

    @Test
    fun `getBalance returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqLightning.getBalance().collect { loadResponse ->
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

    @Test
    fun `getBalanceAll returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqLightning.getBalanceAll().collect { loadResponse ->
                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success<*> -> {}
                        is LoadResponse.Loading -> {}
                    }

                }
            }
        }
}