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
package chat.sphinx.features.authentication.view.components

import chat.sphinx.concepts.authentication.coordinator.AuthenticationRequest
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile


internal class AuthenticationRequestTracker {

    private val requests = mutableListOf<AuthenticationRequest>()
    @Volatile
    private var highestPriorityRequest = 100

    /**
     * Adds the [request] to the list. If the [AuthenticationRequest.priority],
     * is lower than the priority level of the last index, it will sort the list.
     *
     * @return true if priority was changed, false if not
     * */
    @JvmSynthetic
    @Synchronized
    fun addRequest(request: AuthenticationRequest): Boolean {
        requests.add(request)
        return if (request.priority <= highestPriorityRequest) {
            highestPriorityRequest = request.priority
            true
        } else {
            false
        }
    }

    @JvmSynthetic
    @Synchronized
    fun getRequestListSize(): Int =
        requests.size

    @JvmSynthetic
    @Synchronized
    fun getRequestsList(): List<AuthenticationRequest> =
        requests.toList()
}
