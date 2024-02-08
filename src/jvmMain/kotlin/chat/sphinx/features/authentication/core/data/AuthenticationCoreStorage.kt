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
package chat.sphinx.features.authentication.core.data

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.features.authentication.core.model.Credentials
import kotlinx.coroutines.delay

abstract class AuthenticationCoreStorage: AuthenticationStorage {

    protected inner class CredentialString(val value: String)

    protected abstract suspend fun saveCredentialString(credentialString: CredentialString)
    protected abstract suspend fun retrieveCredentialString(): CredentialString?
    
    suspend fun hasCredential(): Boolean {
        return retrieveCredentialString() != null
    }

    @JvmSynthetic
    internal suspend fun saveCredentials(credentials: Credentials) {
        saveCredentialString(CredentialString(credentials.toString()))
        delay(25L)
    }

    @JvmSynthetic
    internal suspend fun retrieveCredentials(): String? =
        retrieveCredentialString()?.value
}
