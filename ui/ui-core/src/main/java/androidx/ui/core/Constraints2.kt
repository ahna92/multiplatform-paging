/*
 * Copyright 2019 The Android Open Source Project
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
@file:Suppress("NOTHING_TO_INLINE")

package androidx.ui.core

import androidx.annotation.IntRange
import androidx.compose.Immutable
import androidx.ui.unit.IntSize
import kotlin.math.max

/**
 * Immutable constraints used for measuring child Layouts or [LayoutModifier]s. A parent layout
 * can measure their children using the measure method on the corresponding [Measurable]s,
 * method which takes the [Constraints2] the child has to follow. A measured child is then
 * responsible to choose for themselves and return a size which satisfies the set of [Constraints2]
 * received from their parent:
 * - minWidth <= chosenWidth <= maxWidth
 * - minHeight <= chosenHeight <= maxHeight
 * The parent can then access the child chosen size on the resulting [Placeable]. The parent is
 * responsible of defining a valid positioning of the children according to their sizes, so the
 * parent needs to measure the children with appropriate [Constraints2], such that whatever valid
 * sizes children choose, they can be laid out in a way that also respects the parent's incoming
 * [Constraints2]. Note that different children can be measured with different [Constraints2].
 * A child is allowed to choose a size that does not satisfy its constraints. However, when this
 * happens, the parent will not read from the [Placeable] the real size of the child, but rather
 * one that was coerced in the child's constraints; therefore, a parent can assume that its
 * children will always respect the constraints in their layout algorithm. When this does not
 * happen in reality, the position assigned to the child will be automatically offset to be centered
 * on the space assigned by the parent under the assumption that constraints were respected.
 * A set of [Constraints2] can have infinite maxWidth and/or maxHeight. This is a trick often
 * used by parents to ask their children for their preferred size: unbounded constraints force
 * children whose default behavior is to fill the available space (always size to
 * maxWidth/maxHeight) to have an opinion about their preferred size. Most commonly, when measured
 * with unbounded [Constraints2], these children will fallback to size themselves to wrap their
 * content, instead of expanding to fill the available space (this is not always true
 * as it depends on the child layout model, but is a common behavior for core layout components).
 *
 * [Constraints2] uses a [Long] to represent four values, [minWidth], [minHeight], [maxWidth],
 * and [maxHeight]. The range of the values varies to allow for at most 256K in one dimension.
 * There are four possible maximum ranges, 13 bits/18 bits, and 15 bits/16 bits for either width
 * or height, depending on the needs. For example, a width could range up to 18 bits
 * and the height up to 13 bits. Alternatively, the width could range up to 16 bits and the height
 * up to 15 bits. The height and width requirements can be reversed, with a height of up to 18 bits
 * and width of 13 bits or height of 16 bits and width of 15 bits. Any constraints exceeding
 * this range will fail.
 */
@Immutable
inline class Constraints2(
    @PublishedApi internal val value: Long
) {
    /**
     * Indicates how the bits are assigned. One of:
     * * MinFocusWidth
     * * MaxFocusWidth
     * * MinFocusHeight
     * * MaxFocusHeight
     */
    private val focusIndex
        get() = (value and FocusMask).toInt()

    /**
     * The minimum width that the measurement can take.
     */
    val minWidth: Int
        @IntRange(from = 0)
        get() {
            val mask = WidthMask[focusIndex]
            return ((value shr 2).toInt() and mask)
        }

    /**
     * The maximum width that the measurement can take. This will either be
     * a positive value greater than or equal to [minWidth] or [Constraints2.Infinity].
     */
    val maxWidth: Int
        get() {
            val mask = WidthMask[focusIndex]
            val width = ((value shr 33).toInt() and mask)
            return if (width == 0) Infinity else width - 1
        }

    /**
     * The minimum height that the measurement can take.
     */
    val minHeight: Int
        @IntRange(from = 0)
        get() {
            val focus = focusIndex
            val mask = HeightMask[focus]
            val offset = MinHeightOffsets[focus]
            return (value shr offset).toInt() and mask
        }

    /**
     * The maximum height that the measurement can take. This will either be
     * a positive value greater than or equal to [minHeight] or [Constraints2.Infinity].
     */
    val maxHeight: Int
        get() {
            val focus = focusIndex
            val mask = HeightMask[focus]
            val offset = MinHeightOffsets[focus] + 31
            val height = (value shr offset).toInt() and mask
            return if (height == 0) Infinity else height - 1
        }

    /**
     * Whether or not the upper bound on the maximum width.
     * @see hasBoundedHeight
     */
    val hasBoundedWidth: Boolean
        get() {
            val mask = WidthMask[focusIndex]
            return ((value shr 33).toInt() and mask) != 0
        }

    /**
     * Whether or not the upper bound on the maximum height.
     * @see hasBoundedWidth
     */
    val hasBoundedHeight: Boolean
        get() {
            val focus = focusIndex
            val mask = HeightMask[focus]
            val offset = MinHeightOffsets[focus] + 31
            return ((value shr offset).toInt() and mask) != 0
        }

    /**
     * Copies the existing [Constraints2], replacing some of [minWidth], [minHeight], [maxWidth],
     * or [maxHeight] as desired. [minWidth] and [minHeight] must be positive and
     * [maxWidth] and [maxHeight] must be greater than or equal to [minWidth] and [minHeight],
     * respectively, or [Infinity].
     */
    fun copy(
        minWidth: Int = this.minWidth,
        maxWidth: Int = this.maxWidth,
        minHeight: Int = this.minHeight,
        maxHeight: Int = this.maxHeight
    ): Constraints2 {
        require(minHeight >= 0 && minWidth >= 0) {
            "minHeight($minHeight) and minWidth($minWidth) must be >= 0"
        }
        require(maxWidth > minWidth || maxWidth == Infinity) {
            "maxWidth($maxWidth) must be greater than minWidth($minWidth)"
        }
        require(maxHeight > minHeight || maxHeight == Infinity) {
            "maxHeight($maxHeight) must be greater than minHeight($minHeight)"
        }
        return createConstraints(minWidth, maxWidth, minHeight, maxHeight)
    }

    companion object {
        // Infinity is set to a negative value in the middle of the Int range so that
        // errors can be more easily detected. Adding or subtracting from [Infinity] will
        // not easily roll over into a valid constraint value.
        /**
         * A value that [maxWidth] or [maxHeight] will be set to when the constraint should
         * be considered infinite. [hasBoundedHeight] or [hasBoundedWidth] will be
         * `true` when [maxHeight] or [maxWidth] is [Infinity], respectively.
         */
        const val Infinity = Integer.MIN_VALUE / 2

        /**
         * The bit distribution when the focus of the bits should be on the width, but only
         * a minimal difference in focus.
         *
         * 16 bits assigned to width, 15 bits assigned to height.
         */
        private const val MinFocusWidth = 0x00L

        /**
         * The bit distribution when the focus of the bits should be on the width, and a
         * maximal number of bits assigned to the width.
         *
         * 18 bits assigned to width, 13 bits assigned to height.
         */
        private const val MaxFocusWidth = 0x01L

        /**
         * The bit distribution when the focus of the bits should be on the height, but only
         * a minimal difference in focus.
         *
         * 15 bits assigned to width, 16 bits assigned to height.
         */
        private const val MinFocusHeight = 0x02L

        /**
         * The bit distribution when the focus of the bits should be on the height, and a
         * a maximal number of bits assigned to the height.
         *
         * 13 bits assigned to width, 18 bits assigned to height.
         */
        private const val MaxFocusHeight = 0x03L

        /**
         * The mask to retrieve the focus ([MinFocusWidth], [MaxFocusWidth],
         * [MinFocusHeight], [MaxFocusHeight]).
         */
        private const val FocusMask = 0x03L

        /**
         * The number of bits used for the focused dimension when there is minimal focus.
         */
        private const val MinFocusBits = 16

        /**
         * The mask to use for the focused dimension when there is minimal focus.
         */
        private const val MinFocusMask = 0xFFFF // 64K (16 bits)

        /**
         * The number of bits used for the non-focused dimension when there is minimal focus.
         */
        private const val MinNonFocusBits = 15

        /**
         * The mask to use for the non-focused dimension when there is minimal focus.
         */
        private const val MinNonFocusMask = 0x7FFF // 32K (15 bits)

        /**
         * The number of bits to use for the focused dimension when there is maximal focus.
         */
        private const val MaxFocusBits = 18

        /**
         * The mask to use for the focused dimension when there is maximal focus.
         */
        private const val MaxFocusMask = 0x3FFFF // 256K (18 bits)

        /**
         * The number of bits to use for the non-focused dimension when there is maximal focus.
         */
        private const val MaxNonFocusBits = 13

        /**
         * The mask to use for the non-focused dimension when there is maximal focus.
         */
        private const val MaxNonFocusMask = 0x1FFF // 8K (13 bits)

        /**
         * Minimum Height shift offsets into Long value, indexed by FocusMask
         * Max offsets are these + 31
         * Width offsets are always either 2 (min) or 33 (max)
         */
        private val MinHeightOffsets = intArrayOf(
            18, // MinFocusWidth: 2 + 16
            20, // MaxFocusWidth: 2 + 18
            17, // MinFocusHeight: 2 + 15
            15 // MaxFocusHeight: 2 + 13
        )

        /**
         * The mask to use for both minimum and maximum width.
         */
        private val WidthMask = intArrayOf(
            MinFocusMask, // MinFocusWidth (16 bits)
            MaxFocusMask, // MaxFocusWidth (18 bits)
            MinNonFocusMask, // MinFocusHeight (15 bits)
            MaxNonFocusMask // MaxFocusHeight (13 bits)
        )

        /**
         * The mask to use for both minimum and maximum height.
         */
        private val HeightMask = intArrayOf(
            MinNonFocusMask, // MinFocusWidth (15 bits)
            MaxNonFocusMask, // MaxFocusWidth (13 bits)
            MinFocusMask, // MinFocusHeight (16 bits)
            MaxFocusMask // MaxFocusHeight (18 bits)
        )

        /**
         * Creates constraints for fixed size in both dimensions.
         */
        fun fixed(width: Int, height: Int): Constraints2 {
            require(width >= 0 && height >= 0) {
                "width($width) and height($height) must be >= 0"
            }
            return createConstraints(width, width, height, height)
        }

        /**
         * Creates constraints for fixed width and unspecified height.
         */
        fun fixedWidth(width: Int): Constraints2 {
            require(width >= 0) {
                "width($width) must be >= 0"
            }
            return createConstraints(
                minWidth = width,
                maxWidth = width,
                minHeight = 0,
                maxHeight = Infinity
            )
        }

        /**
         * Creates constraints for fixed height and unspecified width.
         */
        fun fixedHeight(height: Int): Constraints2 {
            require(height >= 0) {
                "height($height) must be >= 0"
            }
            return createConstraints(
                minWidth = 0,
                maxWidth = Infinity,
                minHeight = height,
                maxHeight = height
            )
        }

        /**
         * Creates a [Constraints2], only checking that the values fit in the packed Long.
         */
        internal fun createConstraints(
            minWidth: Int,
            maxWidth: Int,
            minHeight: Int,
            maxHeight: Int
        ): Constraints2 {
            val heightVal = max(minHeight, maxHeight) // maxHeight can be Infinity
            val heightBits = bitsNeedForSize(heightVal)

            val widthVal = max(minWidth, maxWidth) // maxWidth can be Infinity
            val widthBits = bitsNeedForSize(widthVal)

            if (widthBits + heightBits > 31) {
                throw IllegalArgumentException(
                    "Can't represent a width of $widthVal and height " +
                            "of $heightVal in Constraints"
                )
            }

            val focus = when (widthBits) {
                MinNonFocusBits -> MinFocusHeight
                MinFocusBits -> MinFocusWidth
                MaxNonFocusBits -> MaxFocusHeight
                MaxFocusBits -> MaxFocusWidth
                else -> throw IllegalStateException("Should only have the provided constants.")
            }

            val maxWidthValue = if (maxWidth == Infinity) 0 else maxWidth + 1
            val maxHeightValue = if (maxHeight == Infinity) 0 else maxHeight + 1

            val minHeightOffset = MinHeightOffsets[focus.toInt()]
            val maxHeightOffset = minHeightOffset + 31

            val value = focus or
                    (minWidth.toLong() shl 2) or
                    (maxWidthValue.toLong() shl 33) or
                    (minHeight.toLong() shl minHeightOffset) or
                    (maxHeightValue.toLong() shl maxHeightOffset)
            return Constraints2(value)
        }

        private fun bitsNeedForSize(size: Int): Int {
            return when {
                size < MaxNonFocusMask -> MaxNonFocusBits
                size < MinNonFocusMask -> MinNonFocusBits
                size < MinFocusMask -> MinFocusBits
                size < MaxFocusMask -> MaxFocusBits
                else -> throw IllegalArgumentException(
                    "Can't represent a size of $size in " +
                            "Constraints"
                )
            }
        }
    }
}

/**
 * Create a [Constraints2]. [minWidth] and [minHeight] must be positive and
 * [maxWidth] and [maxHeight] must be greater than or equal to [minWidth] and [minHeight],
 * respectively, or [Infinity][Constraints2.Infinity].
 */
fun Constraints2(
    @IntRange(from = 0)
    minWidth: Int = 0,
    maxWidth: Int = Constraints2.Infinity,
    @IntRange(from = 0)
    minHeight: Int = 0,
    maxHeight: Int = Constraints2.Infinity
): Constraints2 {
    require(maxWidth >= minWidth || maxWidth == Constraints2.Infinity) {
        "maxWidth($maxWidth) must be >= than minWidth($minWidth)"
    }
    require(maxHeight >= minHeight || maxHeight == Constraints2.Infinity) {
        "maxHeight($maxHeight) must be >= than minHeight($minHeight)"
    }
    require(minWidth >= 0 && minHeight >= 0) {
        "minWidth($minWidth) and minHeight($minHeight) must be >= 0"
    }
    return Constraints2.createConstraints(minWidth, maxWidth, minHeight, maxHeight)
}

/**
 * Whether there is exactly one width value that satisfies the constraints.
 */
val Constraints2.hasFixedWidth get() = maxWidth == minWidth

/**
 * Whether there is exactly one height value that satisfies the constraints.
 */
val Constraints2.hasFixedHeight get() = maxHeight == minHeight

/**
 * Whether the area of a component respecting these constraints will definitely be 0.
 * This is true when at least one of maxWidth and maxHeight are 0.
 */
val Constraints2.isZero get() = maxWidth == 0 || maxHeight == 0

/**
 * Returns the result of coercing the current constraints in a different set of constraints.
 */
fun Constraints2.enforce(otherConstraints: Constraints2) = Constraints2(
    minWidth = minWidth.coerceIn(otherConstraints.minWidth, otherConstraints.maxWidth),
    maxWidth = maxWidth.coerceIn(otherConstraints.minWidth, otherConstraints.maxWidth),
    minHeight = minHeight.coerceIn(otherConstraints.minHeight, otherConstraints.maxHeight),
    maxHeight = maxHeight.coerceIn(otherConstraints.minHeight, otherConstraints.maxHeight)
)

/**
 * Takes a size and returns the closest size to it that satisfies the constraints.
 */
fun Constraints2.constrain(size: IntSize) = IntSize(
    width = size.width.coerceIn(minWidth, maxWidth),
    height = size.height.coerceIn(minHeight, maxHeight)
)

/**
 * Takes a size and returns whether it satisfies the current constraints.
 */
fun Constraints2.satisfiedBy(size: IntSize) =
    size.width in minWidth..maxWidth && size.height in minHeight..maxHeight

/**
 * Returns the Constraints obtained by offsetting the current instance with the given values.
 */
fun Constraints2.offset(horizontal: Int = 0, vertical: Int = 0) = Constraints2(
    (minWidth + horizontal).coerceAtLeast(0),
    (maxWidth + horizontal).coerceAtLeast(0),
    (minHeight + vertical).coerceAtLeast(0),
    (maxHeight + vertical).coerceAtLeast(0)
)
