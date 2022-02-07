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
package chat.sphinx.features.foreground_state

import chat.sphinx.concepts.foreground_state.ForegroundState
import chat.sphinx.concepts.foreground_state.ForegroundStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ForegroundStateManagerImpl: ForegroundStateManager {

    @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
    private val _foregroundStateFlow: MutableStateFlow<ForegroundState> by lazy {
        MutableStateFlow<ForegroundState>(ForegroundState.Background)
    }

    override val foregroundStateFlow: StateFlow<ForegroundState>
        get() = _foregroundStateFlow.asStateFlow()

    protected open fun updateForegroundState(state: ForegroundState) {
        _foregroundStateFlow.value = state
    }
}