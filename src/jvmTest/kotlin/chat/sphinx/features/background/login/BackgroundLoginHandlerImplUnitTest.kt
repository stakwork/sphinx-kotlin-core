package chat.sphinx.features.background.login

import chat.sphinx.concepts.background.login.BackgroundLoginHandler
import chat.sphinx.features.background.login.BackgroundLoginHandlerImpl
import chat.sphinx.test.features.authentication.core.AuthenticationCoreDefaultsTestHelper
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.*

class BackgroundLoginHandlerImplUnitTest: AuthenticationCoreDefaultsTestHelper() {

    private val backgroundLoginHandler: BackgroundLoginHandler = BackgroundLoginHandlerImpl(
        testCoreManager,
        testStorage
    )

    @Test
    fun `everything fails if not logged in for first time`() =
        testDispatcher.runBlockingTest {
            assertNull(backgroundLoginHandler.attemptBackgroundLogin())
            assertFalse(backgroundLoginHandler.updateLoginTime())
            assertFalse(backgroundLoginHandler.updateSetting(2))
            assertTrue(testStorage.storage.isEmpty())
            assertEquals(
                BackgroundLoginHandlerImpl.DEFAULT_TIMEOUT,
                backgroundLoginHandler.getTimeOutSetting()
            )
            assertTrue(testStorage.storage.isEmpty())
        }
}