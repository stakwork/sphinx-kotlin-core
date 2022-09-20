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

import com.soywiz.krypto.SecureRandom

class PasswordGenerator(passwordLength: Int, chars: Set<Char> = DEFAULT_CHARS) {

    companion object {
        const val MIN_PASSWORD_LENGTH = 12
        const val MIN_CHAR_POOL_SIZE = 30


        val DEFAULT_CHARS: Set<Char> by lazy {
            val numbers = NUMBERS
            val lettersLower = A_TO_Z_LOWER
            val lettersUpper = A_TO_Z_UPPER

            val set: MutableSet<Char> = LinkedHashSet(numbers.size + lettersLower.size + lettersUpper.size)

            set.addAll(numbers + lettersLower + lettersUpper)
            set.toSet()
        }

        @Suppress("MemberVisibilityCanBePrivate")
        val NUMBERS: Set<Char>
            get() = setOf(
                '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
            )

        @Suppress("MemberVisibilityCanBePrivate")
        val A_TO_Z_LOWER: Set<Char>
            get() = setOf(
                'a', 'b', 'c', 'd', 'e',
                'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o',
                'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y',
                'z',
            )

        @Suppress("MemberVisibilityCanBePrivate")
        val A_TO_Z_UPPER: Set<Char>
            get() = setOf(
                'A', 'B', 'C', 'D', 'E',
                'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O',
                'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y',
                'Z',
            )
    }

    init {
        require(passwordLength >= MIN_PASSWORD_LENGTH) {
            "passwordLength must be greater than or equal to $MIN_PASSWORD_LENGTH"
        }
        require(chars.size >= MIN_CHAR_POOL_SIZE) {
            "chars must contain greater than or equal to $MIN_CHAR_POOL_SIZE"
        }
    }

    val password: Password = CharArray(passwordLength).let { array ->
        repeat(passwordLength) { index ->
            array[index] = chars.elementAt(SecureRandom.nextDouble(chars.size.toDouble()).toInt())
        }

        Password(array)
    }
}
