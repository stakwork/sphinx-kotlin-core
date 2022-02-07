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

import chat.sphinx.features.authentication.core.model.AuthenticateFlowResponse
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

internal class ConfirmPressAction {

    @Volatile
    private var action: Action = Action.Authenticate

    @JvmSynthetic
    @Synchronized
    fun getAction(): Action =
        action

    @JvmSynthetic
    @Synchronized
    fun updateAction(newAction: Action) {
        action.let { currentAction ->
            Exhaustive@
            when (currentAction) {
                is Action.Authenticate -> {}
                is Action.ResetPassword -> {
                    currentAction.flowResponseResetPassword.clearOriginalValidatedPassword()
                    currentAction.flowResponseResetPassword.clearNewPassword()
                }
                is Action.SetPasswordFirstTime -> {
                    currentAction.flowResponseConfirmInputToSetFirstTime.clearInitialUserInput()
                }
            }
        }
        action = newAction
    }

    sealed class Action {
        object Authenticate: Action()

        class ResetPassword private constructor(
            val flowResponseResetPassword: AuthenticateFlowResponse.PasswordConfirmedForReset
        ): Action() {
            companion object {
                @JvmSynthetic
                fun instantiate(flowResponseResetPassword: AuthenticateFlowResponse.PasswordConfirmedForReset): ResetPassword =
                    ResetPassword(flowResponseResetPassword)
            }
        }

        class SetPasswordFirstTime private constructor(
            val flowResponseConfirmInputToSetFirstTime: AuthenticateFlowResponse.ConfirmInputToSetForFirstTime
        ): Action() {
            companion object {
                @JvmSynthetic
                fun instantiate(
                    flowResponseConfirmInputToSetForFirstTime: AuthenticateFlowResponse.ConfirmInputToSetForFirstTime
                ): SetPasswordFirstTime =
                    SetPasswordFirstTime(flowResponseConfirmInputToSetForFirstTime)
            }
        }
    }
}
