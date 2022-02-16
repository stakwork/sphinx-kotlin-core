package chat.sphinx.features.network.query.message

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.test.network.query.NetworkQueryTestHelper
import chat.sphinx.wrapper.message.MessagePagination
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NetworkQueryMessageImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `getMessages returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                // get all available messages
                nqMessage.getMessages(null).collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error<*> -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success<*> -> {}
                        is LoadResponse.Loading -> {}
                    }

                }

            }
        }

    @Test
    fun `pagination returns correct number when limited`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {

                val expectedListSize = 10
                val paginationParams = MessagePagination.instantiate(
                    limit = expectedListSize,
                    offset = 0,
                    date = null
                )

                nqMessage.getMessages(paginationParams).collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {
                            assertEquals(
                                expectedListSize,
                                loadResponse.value.new_messages.size
                            )
                        }
                        is LoadResponse.Loading -> {}
                    }


                }
            }
        }

    @Test
    fun `pagination returns correct offset`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                val limit = 2
                val offset = 8

                val paginationParams = MessagePagination.instantiate(
                    limit = limit,
                    offset = offset,
                    date = null
                )

                nqMessage.getMessages(paginationParams).collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is Response.Error<*> -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {
                            assertEquals(
                                offset + 1L,
                                loadResponse.value.new_messages.firstOrNull()?.id
                            )
                        }
                        is LoadResponse.Loading -> {}
                    }

                }
            }
        }

    @Test
    fun `getPayments returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqMessage.getPayments().collect { loadResponse ->

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
    fun `readMessages returns success`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqMessage.readMessages(ChatId(1)).collect { loadResponse ->

                    Exhaustive@
                    when (loadResponse) {
                        is LoadResponse.Loading -> {}
                        is Response.Error -> {
                            loadResponse.exception?.printStackTrace()
                            fail(loadResponse.message)
                        }
                        is Response.Success -> {}
                    }

                }
            }
        }
}