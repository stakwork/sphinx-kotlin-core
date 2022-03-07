package chat.sphinx.features.authentication.core.components

import chat.sphinx.test.features.authentication.core.TestAuthenticationManagerInitializer
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class AuthenticationManagerInitializerUnitTest {

    @Test
    fun `min user input length below 4 throws exception`() {
        try {
            TestAuthenticationManagerInitializer(3)
            fail()
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("minimumUserInputLength") == true)
        }

        // Shouldn't throw exception
        TestAuthenticationManagerInitializer(4)
    }

    @Test
    fun `max user input greater than min throws exception`() {
        try {
            TestAuthenticationManagerInitializer(8, 7)
            fail()
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("maximumUserInputLength") == true)
        }

        // Shouldn't throw an exception
        TestAuthenticationManagerInitializer(8, 8)
    }
}