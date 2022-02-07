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
package chat.sphinx.features.authentication.view.ui

import chat.sphinx.concepts.views.viewstate.ViewState
import chat.sphinx.features.authentication.core.AuthenticationCoreManager

sealed class AuthenticationViewState: ViewState<AuthenticationViewState>() {
    abstract val pinPadChars: Array<Char>
    abstract val pinLength: Int
    abstract val inputLockState: InputLockState
    val confirmButtonShow: Boolean
        get() = pinLength >= AuthenticationCoreManager.minUserInputLength

    class Idle(
        override val pinLength: Int = 0,

        shufflePin: Boolean = true,

        @Suppress("RemoveExplicitTypeArguments")
        override val pinPadChars: Array<Char> =
            arrayOf<Char>('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                .let { array ->
                    if (shufflePin) {
                        array.shuffle()
                    }
                    array
                },

        override val inputLockState: InputLockState = InputLockState.Unlocked
    ): AuthenticationViewState()

    class ConfirmPin(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState()

    class LogIn(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState()

    sealed class ResetPin(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState() {

        class Step1(
            pinLength: Int,
            pinPadChars: Array<Char>,
            inputLockState: InputLockState
        ): ResetPin(pinLength, pinPadChars, inputLockState)

        class Step2(
            pinLength: Int,
            pinPadChars: Array<Char>,
            inputLockState: InputLockState
        ): ResetPin(pinLength, pinPadChars, inputLockState)
    }
}