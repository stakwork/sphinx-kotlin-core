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
package chat.sphinx.concepts.navigation

/**
 * Implement a single [Navigator] for each module, then define any abstract methods
 * in the Application (which is connected to everything). This allows for modules to
 * be completely decoupled while ensuring that arguments are passed if need be (by
 * requiring an argument in the [NavigationRequest]'s constructor, and implementing
 * the passing of said argument via [NavigationRequest.navigate]'s execution.
 * */
abstract class Navigator<T>(
    protected val navigationDriver: BaseNavigationDriver<T>
)
