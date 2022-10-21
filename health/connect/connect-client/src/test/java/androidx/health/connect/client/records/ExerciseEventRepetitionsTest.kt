/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.health.connect.client.records

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlin.reflect.typeOf
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseEventRepetitionsTest {

    @Test
    fun validRecord_equals() {
        assertThat(
                ExerciseRepetitionsRecord(
                    startTime = Instant.ofEpochMilli(1234L),
                    startZoneOffset = null,
                    endTime = Instant.ofEpochMilli(1236L),
                    endZoneOffset = null,
                    count = 10,
                    type = ExerciseRepetitionsRecord.REPETITION_TYPE_ARM_CURL,
                )
            )
            .isEqualTo(
                ExerciseRepetitionsRecord(
                    startTime = Instant.ofEpochMilli(1234L),
                    startZoneOffset = null,
                    endTime = Instant.ofEpochMilli(1236L),
                    endZoneOffset = null,
                    count = 10,
                    type = ExerciseRepetitionsRecord.REPETITION_TYPE_ARM_CURL,
                )
            )
    }

    @Test
    fun invalidTimes_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExerciseRepetitionsRecord(
                startTime = Instant.ofEpochMilli(1234L),
                startZoneOffset = null,
                endTime = Instant.ofEpochMilli(1234L),
                endZoneOffset = null,
                count = 10,
                type = ExerciseRepetitionsRecord.REPETITION_TYPE_ARM_CURL,
            )
        }
    }

    @Test
    fun allRepetitions_hasMapping() {
        val allEnums =
            ExerciseRepetitionsRecord.Companion::class
                .members
                .asSequence()
                .filter { it ->
                    it.name.startsWith("REPETITION_TYPE") && !it.name.endsWith("UNKNOWN")
                }
                .filter { it -> it.returnType == typeOf<Int>() }
                .map { it -> it.call(ExerciseSessionRecord.Companion) }
                .toHashSet()

        assertThat(ExerciseRepetitionsRecord.REPETITION_TYPE_STRING_TO_INT_MAP.values)
            .containsExactlyElementsIn(allEnums)
        assertThat(ExerciseRepetitionsRecord.REPETITION_TYPE_INT_TO_STRING_MAP.keys)
            .containsExactlyElementsIn(allEnums)
    }
}
