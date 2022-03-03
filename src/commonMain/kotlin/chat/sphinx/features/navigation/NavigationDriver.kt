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
package chat.sphinx.features.navigation

import chat.sphinx.concepts.navigation.BaseNavigationDriver
import chat.sphinx.concepts.navigation.NavigationRequest
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidOf
import com.soywiz.krypto.SecureRandom
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * This class is consumed by the driver (for Android, an activity) and injected
 * as the [BaseNavigationDriver] to feature modules.
 * */
abstract class NavigationDriver<T>(
    // For android, 3 is a good value. This really depends on if you have navigation being
    // executed automatically w/o user input (say, after animation completes). This is due
    // to configuration changes which make tracking what requests have been executed a necessity.
    protected val replayCacheSize: Int
): BaseNavigationDriver<T>() {

    init {
        require(replayCacheSize > 0)
    }

    val navigationRequestSharedFlow: SharedFlow<Pair<NavigationRequest<T>, Uuid>>
        get() = _navigationRequestSharedFlow.asSharedFlow()

    private val executedNavigationRequestsLock = SynchronizedObject()
    private val executedNavigationRequests: Array<Uuid?> =
        arrayOfNulls(replayCacheSize)

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun hasBeenExecuted(request: Pair<NavigationRequest<T>, Uuid>): Boolean =
        synchronized(executedNavigationRequestsLock) {
            executedNavigationRequests.contains(request.second)
        }

    /**
     * Allows for conditional checking in the implementation based off of
     * the request.
     *
     * Returning false will result in the request _not_ being executed, as well
     * as _not_ being added to [executedNavigationRequests].
     * */
    protected abstract suspend fun whenTrueExecuteRequest(request: NavigationRequest<T>): Boolean

    /**
     * Returns true if the request was executed, and false if it was not
     * */
    open suspend fun executeNavigationRequest(controller: T, request: Pair<NavigationRequest<T>, Uuid>): Boolean {
        if (hasBeenExecuted(request)) {
            return false
        }

        if (!whenTrueExecuteRequest(request.first)) {
            return false
        }

        if (replayCacheSize > 0) {
            synchronized(executedNavigationRequestsLock) {
                for (i in 0 until executedNavigationRequests.lastIndex) {
                    executedNavigationRequests[i] = executedNavigationRequests[i + 1]
                }
                executedNavigationRequests[executedNavigationRequests.lastIndex] = request.second
            }
        }

        request.first.navigate(controller)
        return true
    }

    /**
     * Expose in the implementation via:
     *
     * ```
     *   val navigationRequestSharedFlow: SharedFlow
     *       get() = _navigationRequestSharedFlow.asSharedFlow()
     * ```
     * */
    @Suppress("RemoveExplicitTypeArguments", "PropertyName")
    protected val _navigationRequestSharedFlow: MutableSharedFlow<Pair<NavigationRequest<T>, Uuid>> by lazy {
        MutableSharedFlow<Pair<NavigationRequest<T>, Uuid>>(replayCacheSize)
    }

    /**
     * Assigns a [Uuid] to the navigation request such that execution of it
     * can be tracked.
     *
     * This is needed for Android due to configuration changes which inhibits the
     * ability to use [kotlinx.coroutines.flow.SharedFlow]'s buffer overflow.
     * */
    override suspend fun submitNavigationRequest(request: NavigationRequest<T>) {
        _navigationRequestSharedFlow.emit(Pair(request, uuidOf(SecureRandom.nextBytes(16))))
    }
}