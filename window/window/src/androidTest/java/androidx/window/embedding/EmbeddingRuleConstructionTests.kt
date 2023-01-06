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

package androidx.window.embedding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.test.core.app.ApplicationProvider
import androidx.window.embedding.SplitAttributes.LayoutDirection.Companion.BOTTOM_TO_TOP
import androidx.window.embedding.SplitAttributes.LayoutDirection.Companion.LEFT_TO_RIGHT
import androidx.window.embedding.SplitAttributes.LayoutDirection.Companion.LOCALE
import androidx.window.embedding.SplitAttributes.LayoutDirection.Companion.TOP_TO_BOTTOM
import androidx.window.embedding.SplitRule.Companion.DEFAULT_SPLIT_MIN_DIMENSION_DP
import androidx.window.embedding.SplitRule.FinishBehavior.Companion.ADJACENT
import androidx.window.embedding.SplitRule.FinishBehavior.Companion.ALWAYS
import androidx.window.embedding.SplitRule.FinishBehavior.Companion.NEVER
import androidx.window.test.R
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests creation of all embedding rule types via builders and from XML.
 * @see SplitPairRule
 * @see SplitRule
 * @see ActivityRule
 */
class EmbeddingRuleConstructionTests {
    private val application = ApplicationProvider.getApplicationContext<Context>()
    private val ruleController = RuleController.getInstance(application)
    private val density = application.resources.displayMetrics.density
    private lateinit var validBounds: Rect
    private lateinit var invalidBounds: Rect

    @Before
    fun setUp() {
        validBounds = minValidWindowBounds()
        invalidBounds = almostValidWindowBounds()
        ruleController.clearRules()
    }

    /**
     * Verifies that default params are set correctly when reading {@link SplitPairRule} from XML.
     */
    @Test
    fun testDefaults_SplitPairRule_Xml() {
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_default_split_pair_rule)
        assertEquals(1, rules.size)
        val rule: SplitPairRule = rules.first() as SplitPairRule
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.5f))
            .setLayoutDirection(LOCALE)
            .setAnimationBackgroundColor(0)
            .build()
        assertNull(rule.tag)
        assertEquals(NEVER, rule.finishPrimaryWithSecondary)
        assertEquals(ALWAYS, rule.finishSecondaryWithPrimary)
        assertEquals(false, rule.clearTop)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertTrue(rule.checkParentBounds(density, validBounds))
        assertFalse(rule.checkParentBounds(density, invalidBounds))
    }

    /** Verifies that horizontal layout are set correctly when reading [SplitPairRule] from XML. */
    @Test
    fun testHorizontalLayout_SplitPairRule_Xml() {
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_split_pair_rule_horizontal_layout)
        assertEquals(1, rules.size)
        val rule: SplitPairRule = rules.first() as SplitPairRule
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.3f))
            .setLayoutDirection(TOP_TO_BOTTOM)
            .setAnimationBackgroundColor(Color.BLUE)
            .build()
        assertEquals(TEST_TAG, rule.tag)
        assertEquals(NEVER, rule.finishPrimaryWithSecondary)
        assertEquals(ALWAYS, rule.finishSecondaryWithPrimary)
        assertEquals(false, rule.clearTop)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertTrue(rule.checkParentBounds(density, validBounds))
        assertFalse(rule.checkParentBounds(density, invalidBounds))
    }

    /**
     * Verifies that default params are set correctly when creating {@link SplitPairRule} with a
     * builder.
     */
    @Test
    fun testDefaults_SplitPairRule_Builder() {
        val rule = SplitPairRule.Builder(HashSet()).build()
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.5f))
            .setLayoutDirection(LOCALE)
            .setAnimationBackgroundColor(0)
            .build()
        assertNull(rule.tag)
        assertEquals(NEVER, rule.finishPrimaryWithSecondary)
        assertEquals(ALWAYS, rule.finishSecondaryWithPrimary)
        assertEquals(false, rule.clearTop)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertTrue(rule.checkParentBounds(density, validBounds))
        assertFalse(rule.checkParentBounds(density, invalidBounds))
    }

    /**
     * Verifies that the params are set correctly when creating {@link SplitPairRule} with a
     * builder.
     */
    @Test
    fun test_SplitPairRule_Builder() {
        val filters = HashSet<SplitPairFilter>()
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.3f))
            .setLayoutDirection(LEFT_TO_RIGHT)
            .setAnimationBackgroundColor(Color.GREEN)
            .build()
        filters.add(
            SplitPairFilter(
                ComponentName("a", "b"),
                ComponentName("c", "d"),
                "ACTION"
            )
        )
        val rule = SplitPairRule.Builder(filters)
            .setMinWidthDp(123)
            .setMinHeightDp(456)
            .setMinSmallestWidthDp(789)
            .setFinishPrimaryWithSecondary(ADJACENT)
            .setFinishSecondaryWithPrimary(ADJACENT)
            .setClearTop(true)
            .setDefaultSplitAttributes(expectedSplitLayout)
            .setTag(TEST_TAG)
            .build()
        assertEquals(ADJACENT, rule.finishPrimaryWithSecondary)
        assertEquals(ADJACENT, rule.finishSecondaryWithPrimary)
        assertEquals(true, rule.clearTop)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertEquals(TEST_TAG, rule.tag)
        assertEquals(filters, rule.filters)
        assertEquals(123, rule.minWidthDp)
        assertEquals(456, rule.minHeightDp)
        assertEquals(789, rule.minSmallestWidthDp)
    }

    /**
     * Verifies that illegal parameter values are not allowed when creating {@link SplitPairRule}
     * with a builder.
     */
    @Test
    fun test_SplitPairRule_Builder_illegalArguments() {
        assertThrows(IllegalArgumentException::class.java) {
            SplitPairRule.Builder(HashSet())
                .setMinWidthDp(-1)
                .setMinHeightDp(456)
                .setMinSmallestWidthDp(789)
                .build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SplitPairRule.Builder(HashSet())
                .setMinWidthDp(123)
                .setMinHeightDp(-1)
                .setMinSmallestWidthDp(789)
                .build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SplitPairRule.Builder(HashSet())
                .setMinWidthDp(123)
                .setMinHeightDp(456)
                .setMinSmallestWidthDp(-1)
                .build()
        }
    }

    /**
     * Verifies that default params are set correctly when reading {@link SplitPlaceholderRule} from
     * XML.
     */
    @Test
    fun testDefaults_SplitPlaceholderRule_Xml() {
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_default_split_placeholder_rule)
        assertEquals(1, rules.size)
        val rule: SplitPlaceholderRule = rules.first() as SplitPlaceholderRule
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.5f))
            .setLayoutDirection(LOCALE)
            .setAnimationBackgroundColor(0)
            .build()
        assertNull(rule.tag)
        assertEquals(ALWAYS, rule.finishPrimaryWithPlaceholder)
        assertEquals(false, rule.isSticky)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertTrue(rule.checkParentBounds(density, validBounds))
        assertFalse(rule.checkParentBounds(density, invalidBounds))
    }

    /**
     * Verifies that horizontal layout are set correctly when reading [SplitPlaceholderRule]
     * from XML.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @Test
    fun testHorizontalLayout_SplitPlaceholderRule_Xml() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_split_placeholder_horizontal_layout)
        assertEquals(1, rules.size)
        val rule: SplitPlaceholderRule = rules.first() as SplitPlaceholderRule
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.3f))
            .setLayoutDirection(BOTTOM_TO_TOP)
            .setAnimationBackgroundColor(application.resources.getColor(R.color.testColor, null))
            .build()
        assertEquals(TEST_TAG, rule.tag)
        assertEquals(ALWAYS, rule.finishPrimaryWithPlaceholder)
        assertEquals(false, rule.isSticky)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertTrue(rule.checkParentBounds(density, validBounds))
        assertFalse(rule.checkParentBounds(density, invalidBounds))
    }

    /**
     * Verifies that default params are set correctly when creating {@link SplitPlaceholderRule}
     * with a builder.
     */
    @Test
    fun testDefaults_SplitPlaceholderRule_Builder() {
        val rule = SplitPlaceholderRule.Builder(HashSet(), Intent())
            .setMinWidthDp(123)
            .setMinHeightDp(456)
            .setMinSmallestWidthDp(789)
            .build()
        assertNull(rule.tag)
        assertEquals(ALWAYS, rule.finishPrimaryWithPlaceholder)
        assertEquals(false, rule.isSticky)
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.5f))
            .setLayoutDirection(LOCALE)
            .setAnimationBackgroundColor(0)
            .build()
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertEquals(123, rule.minWidthDp)
        assertEquals(456, rule.minHeightDp)
        assertEquals(789, rule.minSmallestWidthDp)
    }

    /**
     * Verifies that the params are set correctly when creating {@link SplitPlaceholderRule} with a
     * builder.
     */
    @Test
    fun test_SplitPlaceholderRule_Builder() {
        val filters = HashSet<ActivityFilter>()
        filters.add(
            ActivityFilter(
                ComponentName("a", "b"),
                "ACTION"
            )
        )
        val intent = Intent("ACTION")
        val expectedSplitLayout = SplitAttributes.Builder()
            .setSplitType(SplitAttributes.SplitType.ratio(0.3f))
            .setLayoutDirection(LEFT_TO_RIGHT)
            .setAnimationBackgroundColor(Color.GREEN)
            .build()
        val rule = SplitPlaceholderRule.Builder(filters, intent)
            .setMinWidthDp(123)
            .setMinHeightDp(456)
            .setMinSmallestWidthDp(789)
            .setFinishPrimaryWithPlaceholder(ADJACENT)
            .setSticky(true)
            .setDefaultSplitAttributes(expectedSplitLayout)
            .setTag(TEST_TAG)
            .build()
        assertEquals(ADJACENT, rule.finishPrimaryWithPlaceholder)
        assertEquals(true, rule.isSticky)
        assertEquals(expectedSplitLayout, rule.defaultSplitAttributes)
        assertEquals(filters, rule.filters)
        assertEquals(intent, rule.placeholderIntent)
        assertEquals(123, rule.minWidthDp)
        assertEquals(456, rule.minHeightDp)
        assertEquals(789, rule.minSmallestWidthDp)
        assertEquals(TEST_TAG, rule.tag)
    }

    /**
     * Verifies that illegal parameter values are not allowed when creating
     * {@link SplitPlaceholderRule} with a builder.
     */
    @Test
    fun test_SplitPlaceholderRule_Builder_illegalArguments() {
        assertThrows(IllegalArgumentException::class.java) {
            SplitPlaceholderRule.Builder(HashSet(), Intent())
                .setMinWidthDp(-1)
                .setMinHeightDp(456)
                .setMinSmallestWidthDp(789)
                .build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SplitPlaceholderRule.Builder(HashSet(), Intent())
                .setMinWidthDp(123)
                .setMinHeightDp(-1)
                .setMinSmallestWidthDp(789)
                .build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SplitPlaceholderRule.Builder(HashSet(), Intent())
                .setMinWidthDp(123)
                .setMinHeightDp(456)
                .setMinSmallestWidthDp(-1)
                .build()
        }
        assertThrows(IllegalArgumentException::class.java) {
            SplitPlaceholderRule.Builder(HashSet(), Intent())
                .setMinWidthDp(123)
                .setMinHeightDp(456)
                .setMinSmallestWidthDp(789)
                .setFinishPrimaryWithPlaceholder(NEVER)
                .build()
        }
    }

    /**
     * Verifies that default params are set correctly when reading {@link ActivityRule} from XML.
     */
    @Test
    fun testDefaults_ActivityRule_Xml() {
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_default_activity_rule)
        assertEquals(1, rules.size)
        val rule: ActivityRule = rules.first() as ActivityRule
        assertNull(rule.tag)
        assertFalse(rule.alwaysExpand)
    }

    /**
     * Verifies that [ActivityRule.tag] and [ActivityRule.alwaysExpand] are set correctly when
     * reading [ActivityRule] from XML.
     */
    @Test
    fun testSetTagAndAlwaysExpand_ActivityRule_Xml() {
        val rules = RuleController
            .parseRules(application, R.xml.test_split_config_activity_rule_with_tag)
        assertEquals(1, rules.size)
        val rule: ActivityRule = rules.first() as ActivityRule
        assertEquals(TEST_TAG, rule.tag)
        assertTrue(rule.alwaysExpand)
    }

    /**
     * Verifies that default params are set correctly when creating {@link ActivityRule} with a
     * builder.
     */
    @Test
    fun testDefaults_ActivityRule_Builder() {
        val rule = ActivityRule.Builder(HashSet()).build()
        assertFalse(rule.alwaysExpand)
    }

    /**
     * Verifies that the params are set correctly when creating {@link ActivityRule} with a builder.
     */
    @Test
    fun test_ActivityRule_Builder() {
        val filters = HashSet<ActivityFilter>()
        filters.add(
            ActivityFilter(
                ComponentName("a", "b"),
                "ACTION"
            )
        )
        val rule = ActivityRule.Builder(filters)
            .setAlwaysExpand(true)
            .setTag(TEST_TAG)
            .build()
        assertTrue(rule.alwaysExpand)
        assertEquals(TEST_TAG, rule.tag)
        assertEquals(filters, rule.filters)
    }

    private fun minValidWindowBounds(): Rect {
        // Get the screen's density scale
        val scale: Float = density
        // Convert the dps to pixels, based on density scale
        val minValidWidthPx = (DEFAULT_SPLIT_MIN_DIMENSION_DP * scale + 0.5f).toInt()

        return Rect(0, 0, minValidWidthPx, minValidWidthPx)
    }

    private fun almostValidWindowBounds(): Rect {
        // Get the screen's density scale
        val scale: Float = density
        // Convert the dps to pixels, based on density scale
        val minValidWidthPx = ((DEFAULT_SPLIT_MIN_DIMENSION_DP) - 1 * scale + 0.5f).toInt()

        return Rect(0, 0, minValidWidthPx, minValidWidthPx)
    }

    @Test
    fun testIllegalTag_XML() {
        assertThrows(IllegalArgumentException::class.java) {
            RuleController.parseRules(application, R.xml.test_split_config_duplicated_tag)
        }
    }

    @Test
    fun testReplacingRuleWithTag() {
        var rules = RuleController
            .parseRules(application, R.xml.test_split_config_activity_rule_with_tag)
        assertEquals(1, rules.size)
        var rule = rules.first()
        assertEquals(TEST_TAG, rule.tag)
        val staticRule = rule as ActivityRule
        assertTrue(staticRule.alwaysExpand)
        ruleController.setRules(rules)

        val filters = HashSet<ActivityFilter>()
        filters.add(
            ActivityFilter(
                ComponentName("a", "b"),
                "ACTION"
            )
        )
        val rule1 = ActivityRule.Builder(filters)
            .setAlwaysExpand(true)
            .setTag(TEST_TAG)
            .build()
        ruleController.addRule(rule1)

        rules = ruleController.getRules()
        assertEquals(1, rules.size)
        rule = rules.first()
        assertEquals(rule1, rule)

        val intent = Intent("ACTION")
        val rule2 = SplitPlaceholderRule.Builder(filters, intent)
            .setMinWidthDp(123)
            .setMinHeightDp(456)
            .setMinSmallestWidthDp(789)
            .setTag(TEST_TAG)
            .build()

        ruleController.addRule(rule2)

        rules = ruleController.getRules()
        assertEquals(1, rules.size)
        rule = rules.first()
        assertEquals(rule2, rule)
    }

    companion object {
        const val TEST_TAG = "test"
    }
}