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
package chat.sphinx.crypto.common.extensions

import chat.sphinx.crypto.common.annotations.UnencryptedDataAccess
import chat.sphinx.crypto.common.clazzes.UnencryptedByteArray
import chat.sphinx.crypto.common.clazzes.UnencryptedCharArray
import java.nio.ByteBuffer
import java.nio.CharBuffer

@Suppress("NOTHING_TO_INLINE")
@OptIn(UnencryptedDataAccess::class)
inline fun UnencryptedByteArray.toUnencryptedCharArray(): UnencryptedCharArray =
    UnencryptedCharArray(value.toCharArray())

/** securely converts a ByteArray to a CharArray */
@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toCharArray(fill: Char = '0'): CharArray =
    copyOf().let { copyByteArray ->
        ByteBuffer.wrap(copyByteArray).let { byteBuffer ->
            Charsets.UTF_8.newDecoder().decode(byteBuffer).let { charBuffer ->
                charBuffer.array().copyOf(charBuffer.limit()).let { charArray ->
                    byteBuffer.array().fill(fill.code.toByte())
                    charBuffer.array().fill(fill)
                    charArray
                }
            }
        }
    }

inline val ByteArray.isValidUTF8: Boolean
    get() = try {
        Charsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(this))
        true
    } catch (e: CharacterCodingException) {
        false
    }

/** securely converts a CharArray to a ByteArray */
@Suppress("NOTHING_TO_INLINE")
inline fun CharArray.toByteArray(fill: Char = '0'): ByteArray =
    copyOf().let { copyCharArray ->
        CharBuffer.wrap(copyCharArray).let { charBuffer ->
            Charsets.UTF_8.newEncoder().encode(charBuffer).let { byteBuffer ->
                byteBuffer.array().copyOf(byteBuffer.limit()).let { byteArray ->
                    charBuffer.array().fill(fill)
                    byteBuffer.array().fill(fill.code.toByte())
                    byteArray
                }
            }
        }
    }

