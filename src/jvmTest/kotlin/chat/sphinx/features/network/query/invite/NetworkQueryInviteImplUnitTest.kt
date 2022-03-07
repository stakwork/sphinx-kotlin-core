package chat.sphinx.features.network.query.invite

import chat.sphinx.test.network.query.NetworkQueryTestHelper
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

class NetworkQueryInviteImplUnitTest: NetworkQueryTestHelper() {

    @Test
    fun `test stub`() =
        testDispatcher.runBlockingTest {
            getCredentials()?.let {
                nqInvite
            }
        }
}