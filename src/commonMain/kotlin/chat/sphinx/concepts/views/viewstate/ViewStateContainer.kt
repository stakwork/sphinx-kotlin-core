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
package chat.sphinx.concepts.views.viewstate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

suspend inline fun <VS: ViewState<VS>> ViewStateContainer<VS>.collect(
    crossinline action: suspend (value: VS) -> Unit
): Unit =
    this.viewStateFlow.collect { action(it) }

inline val <VS: ViewState<VS>>ViewStateContainer<VS>.value: VS
    get() = this.viewStateFlow.value

open class ViewStateContainer<VS: ViewState<VS>>(initialViewState: VS) {
    @Suppress("PropertyName", "RemoveExplicitTypeArguments")
    protected val _viewStateFlow: MutableStateFlow<VS> by lazy {
        MutableStateFlow<VS>(initialViewState)
    }

    open val viewStateFlow: StateFlow<VS>
        get() = _viewStateFlow.asStateFlow()

    open fun updateViewState(viewState: VS) {
        _viewStateFlow.value = viewState
    }
}
