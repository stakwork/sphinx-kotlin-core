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
package chat.sphinx.features.authentication.core.components

/**
 * Initializes parameters at runtime.
 *
 * See init requirements for user input min/max character lengths.
 *
 * Wrong Pin Lockout feature will be enabled **only** if both
 * [wrongPinAttemptsUntilLockedOut] and [wrongPinLockoutDuration] are
 * greater than 0.
 * */
open class AuthenticationManagerInitializer(
    val minimumUserInputLength: Int = 8,
    val maximumUserInputLength: Int = 42,
    val wrongPinAttemptsUntilLockedOut: Int = 0,
    val wrongPinLockoutDuration: Long = 0L,
) {
    init {
        require(minimumUserInputLength >= 4) {
            "minimumUserInputLength must be greater than or equal to 4"
        }
        require(maximumUserInputLength >= minimumUserInputLength) {
            "maximumUserInputLength must be greater than or equal to minimumUserInputLength"
        }
    }
}
