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
package chat.sphinx.crypto.common.clazzes

import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import kotlin.jvm.JvmInline

@Suppress("NOTHING_TO_INLINE")
@OptIn(UnencryptedDataAccess::class)
inline fun UnencryptedByteArray.clear(char: Char = '0') {
    value.fill(char.code.toByte())
}


@Suppress("NOTHING_TO_INLINE")
@OptIn(UnencryptedDataAccess::class)
inline fun UnencryptedByteArray.toUnencryptedString(trim: Boolean = true): UnencryptedString =
    UnencryptedString(value.decodeToString().let { if (trim) it.trim() else it})

@JvmInline
value class UnencryptedByteArray(@property: UnencryptedDataAccess val value: ByteArray) {

    override fun toString(): String {
        throw IllegalStateException("toUnencryptedString must be used.")
    }
}
