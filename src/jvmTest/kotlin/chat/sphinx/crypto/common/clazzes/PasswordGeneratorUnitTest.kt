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

/**
 * Checks the output file located in the `test/resources` directory for changes
 * to [PasswordGenerator].kt before running the test again such that if there is
 * a change to the Sha256Sum, or the output failed, it will run; otherwise this
 * test gets skipped as it takes for ever due to the large sample size needed.
 * */

// TODO: Make this tests work...
//@OptIn(RawPasswordAccess::class)
//class PasswordGeneratorUnitTest {
//
//    companion object {
//        const val PASSWORD_LENGTH = 50
//        const val SAMPLE_SIZE = 10_000_000
//
//        const val MAX_DEVIATION_THRESHOLD: Float = 5.0E-5F
//
//        const val FAIL = "FAIL"
//        const val PASS = "PASS"
//
//        private val WORKING_DIR_PATH: String by lazy {
//            System.getProperty("user.dir")
//        }
//
//        private val DIR_PATH_DELIMITER: Char by lazy {
//            if (WORKING_DIR_PATH.contains(":\\")) {
//                '\\'
//            } else {
//                '/'
//            }
//        }
//    }
//
//    @Test
//    fun `test character distribution`() {
//
//        val testOutputFile: File = getOrCreateTestOutputDir()
//        val passwordGeneratorKtHash: Sha256Hash = getPasswordGeneratorFileHash()
//
//        if (shouldRunTest(testOutputFile, passwordGeneratorKtHash)) {
//
//            println("""
//                *************************************************
//                *                                               *
//                *       ${PasswordGenerator::class.java.simpleName}.kt was modified       *
//                * Running UnitTest... This will take a while... *
//                *                                               *
//                *************************************************
//
//            """.trimIndent())
//
//            val chars = PasswordGenerator.DEFAULT_CHARS
//
//            val sb1 = StringBuilder()
//            sb1.append(PasswordGenerator::class.simpleName)
//            sb1.append(".kt Sha256Sum:")
//            sb1.append("\n")
//            sb1.append(passwordGeneratorKtHash.value)
//            sb1.append("\n")
//            sb1.append("================================================================")
//            sb1.append("\n")
//
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//
//            sb1.append("Run Date:                                             ")
//            sb1.append(sdf.format(Date()))
//            sb1.append("\n")
//            sb1.append("Char Pool Size:                                       ")
//            sb1.append(chars.size)
//            sb1.append("\n")
//            sb1.append("Password Length:                                      ")
//            sb1.append(PASSWORD_LENGTH)
//            sb1.append("\n")
//            sb1.append("Password Sample Size:                                 ")
//            sb1.append(NumberFormat.getInstance(Locale.ENGLISH).format(SAMPLE_SIZE.toLong()))
//
//            val sb1Output = sb1.toString()
//            println(sb1Output)
//
//            // populate frequency map with sample outputs
//            val frequencyMap: MutableMap<Char, Int> = LinkedHashMap(chars.size)
//
//            repeat(SAMPLE_SIZE) {
//                val password = PasswordGenerator(PASSWORD_LENGTH, chars).password.value
//                for (char in password) {
//                    frequencyMap[char] = (frequencyMap[char] ?: 0) + 1
//                }
//            }
//
//            // calculate deviation
//            val sortedByValue = frequencyMap.entries.sortedBy { it.value }
//            val maxFrequencyDiff = sortedByValue.last().value - sortedByValue.first().value
//            val maxDeviation = maxFrequencyDiff.toFloat() / (SAMPLE_SIZE * PASSWORD_LENGTH).toFloat()
//
//            val sb2 = StringBuilder()
//            sb2.append("Max Frequency Diff (highest - lowest):                ")
//            sb2.append(maxFrequencyDiff)
//            sb2.append("\n")
//            sb2.append("Max Deviation Threshold:                              ")
//            sb2.append(MAX_DEVIATION_THRESHOLD)
//            sb2.append("\n")
//            sb2.append("Max Deviation Actual:                                 ")
//            sb2.append(maxDeviation)
//            sb2.append("\n")
//            sb2.append("================================================================")
//            sb2.append("\n")
//            sb2.append("\n")
//
//            val sortedByKey = frequencyMap.entries.sortedBy { it.key }
//            sb2.append("Char | Frequency")
//            sb2.append("\n")
//            sb2.append("----------------")
//            sb2.append("\n")
//
//            for (entry in sortedByKey) {
//                sb2.append(entry.key)
//                sb2.append("    | ")
//                sb2.append(entry.value)
//                sb2.append("\n")
//            }
//
//            sb2.append("\n")
//            sb2.append("================================================================")
//            sb2.append("\n")
//            sb2.append("\n")
//
//            // pass or fail based on our set threshold
//            val passed: Boolean = if (maxDeviation > MAX_DEVIATION_THRESHOLD) {
//                sb2.append(FAIL)
//                false
//            } else {
//                sb2.append(PASS)
//                true
//            }
//
//            val sb2Output = sb2.toString()
//            testOutputFile.writeText(sb1Output + "\n" + sb2Output, charset("UTF-8"))
//            println(sb2Output)
//
//            if (!passed) {
//                fail("Threshold of $MAX_DEVIATION_THRESHOLD exceeded")
//            }
//
//        }
//    }
//
//    private fun getOrCreateTestOutputDir(): File {
//        StringBuilder().let { sb ->
//            sb.append(WORKING_DIR_PATH)
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("src")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("test")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("resources")
//
//            val testResourceDir = File(sb.toString())
//
//            if (!testResourceDir.exists() && !testResourceDir.mkdirs()) {
//                fail("Failed to create test resource directory")
//            }
//
//            val outputFile = File(testResourceDir, "PasswordGeneratorUnitTestOutput.txt")
//
//            if (!outputFile.exists() && !outputFile.createNewFile()) {
//                fail("Failed to create test output file")
//            }
//
//            return outputFile
//        }
//    }
//
//    private fun getPasswordGeneratorFileHash(): Sha256Hash {
//        StringBuilder().let { sb ->
//            sb.append(WORKING_DIR_PATH)
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("src")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("main")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("java")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("io")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("matthewnelson")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("crypto_common")
//            sb.append(DIR_PATH_DELIMITER)
//            sb.append("clazzes")
//
//            val clazzesDir = File(sb.toString())
//
//            if (!clazzesDir.exists()) {
//                fail("crypto_common/clazzes/ directory does not exist")
//            }
//
//            val pwGen = File(clazzesDir, "${PasswordGenerator::class.simpleName}.kt")
//
//            if (!pwGen.exists()) {
//                fail("${PasswordGenerator::class.simpleName}.kt does not exist")
//            }
//
//            return pwGen.readText().toSha256Hash()
//        }
//    }
//
//    /**
//     * First line of output file is the Sha256Sum of the [PasswordGenerator].kt
//     *
//     * Last line of output file is either [PASS] or [FAIL]
//     * */
//    private fun shouldRunTest(testOutput: File, pwGenHash: Sha256Hash): Boolean {
//        val outputLines = testOutput.readLines(charset("UTF-8"))
//
//        return if (outputLines.elementAtOrNull(1) == pwGenHash.value) {
//            if (outputLines.lastOrNull() != PASS) {
//                true
//            } else {
//
//                // Show previous run results (minus the char frequency output)
//                var breakOnNext = false
//                for (line in outputLines) {
//
//                    println(line)
//
//                    if (line.startsWith("====")) {
//                        if (breakOnNext) {
//                            break
//                        } else {
//                            breakOnNext = true
//                        }
//                    }
//                }
//                println(PASS)
//
//                false
//            }
//        } else {
//            true
//        }
//    }
//}
