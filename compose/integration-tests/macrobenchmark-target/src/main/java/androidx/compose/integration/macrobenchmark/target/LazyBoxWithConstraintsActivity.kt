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

package androidx.compose.integration.macrobenchmark.target

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

class LazyBoxWithConstraintsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemCount = intent.getIntExtra(EXTRA_ITEM_COUNT, 3000)
        val items = List(itemCount) { entryIndex ->
            NestedListEntry(
                buildList {
                    repeat(10) {
                        add("${entryIndex}x$it")
                    }
                }
            )
        }

        setContent {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "IamLazy" }
            ) {
                items(items) { entry ->
                    NonLazyRow(entry)
                }
            }
        }

        launchIdlenessTracking()
    }

    companion object {
        const val EXTRA_ITEM_COUNT = "ITEM_COUNT"
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope") // Need the nested subcompose layout for testing
@Composable
private fun NonLazyRow(entry: NestedListEntry) {
    BoxWithConstraints {
        Row(
            Modifier
                .padding(16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            entry.list.forEach {
                Card(Modifier.size(80.dp)) {
                    Text(text = it)
                }
                Spacer(Modifier.size(16.dp))
            }
        }
    }
}
