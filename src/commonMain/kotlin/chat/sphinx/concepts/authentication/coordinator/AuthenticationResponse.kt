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
package chat.sphinx.concepts.authentication.coordinator

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey

sealed class AuthenticationResponse(val authenticationRequest: AuthenticationRequest) {

    sealed class Success(request: AuthenticationRequest): AuthenticationResponse(request) {

        class Authenticated(
            request: AuthenticationRequest
        ): Success(request)

        class Key(
            request: AuthenticationRequest,
            val encryptionKey: EncryptionKey
        ): Success(request)
    }

    class Failure(
        request: AuthenticationRequest
    ): AuthenticationResponse(request)
}
