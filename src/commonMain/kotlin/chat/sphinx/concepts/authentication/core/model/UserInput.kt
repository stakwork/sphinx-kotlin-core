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
package chat.sphinx.concepts.authentication.core.model

import kotlinx.coroutines.flow.StateFlow

interface UserInput {
    val inputLengthStateFlow: StateFlow<Int>

    /**
     * Add a character to the [UserInput]
     *
     * @throws [IllegalArgumentException] if defined maximum input length is exceeded.
     * */
    @Throws(IllegalArgumentException::class)
    fun addCharacter(c: Char)

    fun clearInput()

    fun compare(userInput: UserInput): Boolean

    /**
     * Remove the last character from the [UserInput]
     *
     * @throws [IllegalArgumentException] if no more characters can be removed.
     * */
    @Throws(IllegalArgumentException::class)
    fun dropLastCharacter()
}