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
package androidx.health.data.client.records

import androidx.health.data.client.aggregate.DoubleAggregateMetric
import androidx.health.data.client.metadata.Metadata
import java.time.Instant
import java.time.ZoneOffset

/** Captures the elevation gained by the user since the last reading. */
public class ElevationGained(
    /** Elevation in meters. Required field. Valid range: -1000000-1000000. */
    public val elevationMeters: Double,
    override val startTime: Instant,
    override val startZoneOffset: ZoneOffset?,
    override val endTime: Instant,
    override val endZoneOffset: ZoneOffset?,
    override val metadata: Metadata = Metadata.EMPTY,
) : IntervalRecord {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ElevationGained) return false

        if (elevationMeters != other.elevationMeters) return false
        if (startTime != other.startTime) return false
        if (startZoneOffset != other.startZoneOffset) return false
        if (endTime != other.endTime) return false
        if (endZoneOffset != other.endZoneOffset) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + elevationMeters.hashCode()
        result = 31 * result + (startZoneOffset?.hashCode() ?: 0)
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (endZoneOffset?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }

    internal companion object {
        /** Metric identifier to retrieve total elevation gained from [AggregateDataRow]. */
        @JvmField
        internal val ELEVATION_TOTAL: DoubleAggregateMetric =
            DoubleAggregateMetric("ElevationGained", "total", "elevation")
    }
}
