package chat.sphinx.features.network.query.subscription

import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.exception
import chat.sphinx.response.message
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import chat.sphinx.wrapper.subscription.SubscriptionId
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NetworkQuerySubscriptionImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `getSubscriptions returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqSubscription.getSubscriptions().collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            // will fail on error
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
    fun `getSubscriptionById returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                var subscription: SubscriptionDto? = null

                nqSubscription.getSubscriptions().collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            // will fail on error
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {
                            subscription = loadResponse.value.lastOrNull()
                        }
                        is LoadResponse.Loading -> {}
                    }

                }

                subscription?.let { nnSub ->
                    nqSubscription.getSubscriptionById(SubscriptionId(nnSub.id)).collect { loadResponse ->

                        Exhaustive@
                        when (loadResponse) {
                            is Response.Error -> {
                                // will fail on error
                                loadResponse.exception?.printStackTrace()
                                fail(loadResponse.message)
                            }
                            is Response.Success -> {
                                // should return the same object
                                assertEquals(
                                    nnSub.toString(),
                                    loadResponse.value.toString()
                                )
                            }
                            LoadResponse.Loading -> {}
                        }

                    }
                } ?: println("\nTest Account does not have any subscriptions")
            }
        }

    @Test
    fun `getSubscriptionById returns error if Id doesn't exist`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                var lastSubIdPlus1: Long? = null

                nqSubscription.getSubscriptions().collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            // will fail on error
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {
                            lastSubIdPlus1 = (loadResponse.value.lastOrNull()?.id ?: 0L) + 1L
                        }
                        is LoadResponse.Loading -> {}
                    }

                }

                nqSubscription.getSubscriptionById(SubscriptionId(lastSubIdPlus1!!)).collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            // an error should be returned as that Id does not exist
                            assertTrue(
                //                                Print statement:
                //                                Error(cause=ResponseError(message=, exception=java.io.IOException: Response{protocol=http/1.1, code=400, message=Bad Request, url=https://2218d66f0a-sphinx.m.relay.voltageapp.io:3001/subscription/1}))
                                loadResponse.cause.exception?.message?.contains("code=400") == true
                            )
                        }
                        is Response.Success -> {
                            fail()
                        }
                        LoadResponse.Loading -> {}
                    }

                }
            }
        }
}