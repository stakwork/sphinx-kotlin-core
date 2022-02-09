/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
package chat.sphinx.test.features.authentication.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Simple utility class to initialize the test helper with defaults instead and setup things
 * */
abstract class AuthenticationCoreDefaultsTestHelper: AuthenticationCoreTestHelper<
        TestAuthenticationManagerInitializer,
        TestEncryptionKeyHandler,
        TestAuthenticationCoreStorage
        >()
{
    override val testInitializer: TestAuthenticationManagerInitializer =
        TestAuthenticationManagerInitializer()
    override val testHandler: TestEncryptionKeyHandler =
        TestEncryptionKeyHandler()
    override val testStorage: TestAuthenticationCoreStorage =
        TestAuthenticationCoreStorage()

    @BeforeTest
    fun setup() {
        setupAuthenticationCoreTestHelper()
    }

    @AfterTest
    fun tearDown() {
        tearDownAuthenticationCoreTestHelper()
    }
}