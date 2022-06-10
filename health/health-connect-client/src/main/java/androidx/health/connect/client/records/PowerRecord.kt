/*
 * Copyright (C) 2022 The Android Open Source Project
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

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregateMetric.AggregationType.AVERAGE
import androidx.health.connect.client.aggregate.AggregateMetric.AggregationType.MAXIMUM
import androidx.health.connect.client.aggregate.AggregateMetric.AggregationType.MINIMUM
import androidx.health.connect.client.aggregate.AggregateMetric.Companion.doubleMetric
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Power
import java.time.Instant
import java.time.ZoneOffset

/**
 * Captures the power generated by the user, e.g. during cycling or rowing with a power meter. Each
 * record represents a series of measurements.
 */
public class PowerRecord(
    override val startTime: Instant,
    override val startZoneOffset: ZoneOffset?,
    override val endTime: Instant,
    override val endZoneOffset: ZoneOffset?,
    override val samples: List<Sample>,
    override val metadata: Metadata = Metadata.EMPTY,
) : SeriesRecord<PowerRecord.Sample> {

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PowerRecord) return false

        if (startTime != other.startTime) return false
        if (startZoneOffset != other.startZoneOffset) return false
        if (endTime != other.endTime) return false
        if (endZoneOffset != other.endZoneOffset) return false
        if (samples != other.samples) return false
        if (metadata != other.metadata) return false

        return true
    }

    /*
     * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
     */
    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + (startZoneOffset?.hashCode() ?: 0)
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (endZoneOffset?.hashCode() ?: 0)
        result = 31 * result + samples.hashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }

    companion object {
        private const val TYPE = "Power"
        private const val POWER_FIELD = "power"

        /**
         * Metric identifier to retrieve average power from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val POWER_AVG: AggregateMetric<Power> =
            doubleMetric(
                dataTypeName = TYPE,
                aggregationType = AVERAGE,
                fieldName = POWER_FIELD,
                mapper = Power::watts,
            )

        /**
         * Metric identifier to retrieve minimum power from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val POWER_MIN: AggregateMetric<Power> =
            doubleMetric(
                dataTypeName = TYPE,
                aggregationType = MINIMUM,
                fieldName = POWER_FIELD,
                mapper = Power::watts,
            )

        /**
         * Metric identifier to retrieve maximum power from
         * [androidx.health.connect.client.aggregate.AggregationResult].
         */
        @JvmField
        val POWER_MAX: AggregateMetric<Power> =
            doubleMetric(
                dataTypeName = TYPE,
                aggregationType = MAXIMUM,
                fieldName = POWER_FIELD,
                mapper = Power::watts,
            )
    }

    /**
     * Represents a single measurement of power. For example, using a power meter when exercising on
     * a stationary bike.
     *
     * @param time The point in time when the measurement was taken.
     * @param power Power generated, in [Power] unit. Valid range: 0-100000 Watts.
     *
     * @see PowerRecord
     */
    public class Sample(
        val time: Instant,
        val power: Power,
    ) {

        init {
            power.requireNotLess(other = power.zero(), name = "power")
        }

        /*
         * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Sample) return false

            if (time != other.time) return false
            if (power != other.power) return false

            return true
        }

        /*
         * Generated by the IDE: Code -> Generate -> "equals() and hashCode()".
         */
        override fun hashCode(): Int {
            var result = time.hashCode()
            result = 31 * result + power.hashCode()
            return result
        }
    }
}
