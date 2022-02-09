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
package io.matthewnelson.test_concept_coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.test.assertEquals

fun <F> Flow<F>.testObserver(scope: CoroutineScope): TestObserver<F> =
    TestObserver(scope, this)

class TestObserver<T>(scope: CoroutineScope, flow: Flow<T>) {
    val values = mutableListOf<T>()
    private val job: Job = scope.launch {
        flow.collect { values.add(it) }
    }
    fun assertNoValues(): TestObserver<T> {
        assertEquals(emptyList<T>(), this.values)
        return this
    }
    suspend fun delay10(): TestObserver<T> {
        delay(10L)
        return this
    }
    fun assertValues(vararg values: T): TestObserver<T> {
        assertEquals(values.toList(), this.values)
        return this
    }
    fun finish() {
        job.cancel()
    }
}
