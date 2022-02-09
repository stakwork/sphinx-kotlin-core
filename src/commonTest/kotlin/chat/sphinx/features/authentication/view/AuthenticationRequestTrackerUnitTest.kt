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
package chat.sphinx.features.authentication.view

import chat.sphinx.concepts.authentication.coordinator.AuthenticationRequest
import chat.sphinx.features.authentication.view.components.AuthenticationRequestTracker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AuthenticationRequestTrackerUnitTest {

    private val tracker = AuthenticationRequestTracker()

    @Test
    fun `adding of requests sorts by request priority and returns true if highest priority changed`() {
        assertTrue(tracker.getRequestsList().isEmpty())
        assertTrue(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        assertTrue(tracker.addRequest(AuthenticationRequest.LogIn()))
        assertFalse(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        assertFalse(tracker.addRequest(AuthenticationRequest.ResetPassword()))
        assertFalse(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        assertTrue(tracker.addRequest(AuthenticationRequest.LogIn()))
    }
}
