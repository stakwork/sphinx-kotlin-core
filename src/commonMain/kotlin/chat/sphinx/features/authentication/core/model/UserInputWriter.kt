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
package chat.sphinx.features.authentication.core.model

import chat.sphinx.concepts.authentication.core.model.UserInput
import chat.sphinx.crypto.common.annotations.RawPasswordAccess
import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.crypto.common.clazzes.clear
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.utils.CharArrayWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Synchronized

internal class UserInputWriter private constructor(): CharArrayWriter (
    AuthenticationCoreManager.maxUserInputLength
), UserInput {

    companion object {
        @JvmSynthetic
        internal fun instantiate(): UserInputWriter =
            UserInputWriter()
    }

    @Suppress("RemoveExplicitTypeArguments", "PropertyName")
    private val _inputLengthStateFlow: MutableStateFlow<Int> by lazy {
        MutableStateFlow<Int>(0)
    }

    override val inputLengthStateFlow: StateFlow<Int>
        get() = _inputLengthStateFlow.asStateFlow()

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun addCharacter(c: Char) {
        if (size() + 1 > AuthenticationCoreManager.maxUserInputLength) {
            throw IllegalArgumentException("Cannot add anymore characters")
        }

        append(c)
        _inputLengthStateFlow.value += 1
    }

    @Synchronized
    override fun clearInput() {
        buf.fill('0')
        reset()
        _inputLengthStateFlow.value = 0
    }

    @Synchronized
    fun clone(): UserInputWriter =
        UserInputWriter().also { newWriter ->
            for (i in 0 until size()) {
                newWriter.addCharacter(buf[i])
            }
        }

    @Synchronized
    override fun compare(userInput: UserInput): Boolean {
        if (this.hashCode() == userInput.hashCode()) {
            return true
        }

        try {
            if (size() != (userInput as UserInputWriter).size()) {
                return false
            }
        } catch (e: ClassCastException) {
            return false
        }

        var inputMatch = true
        @OptIn(RawPasswordAccess::class)
        Password(userInput.toCharArray()).let { copy ->
            try {
                for (i in 0 until size()) {
                    if (buf[i] != copy.value[i]) {
                        inputMatch = false
                        break
                    }
                }
            } finally {
                copy.clear()
            }
        }

        return inputMatch
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun dropLastCharacter() {
        if (count > 0) {
            count -= 1
            _inputLengthStateFlow.value -= 1
        } else {
            throw IllegalArgumentException("Cannot remove any more characters")
        }
    }

    override fun toString(): String {
        return ""
    }
}
