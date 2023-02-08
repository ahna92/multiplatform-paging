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

package androidx.benchmark.integration.baselineprofiles.library.buildprovider

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.benchmark.integration.baselineprofiles.library.consumer.IncludeClass
import androidx.benchmark.integration.baselineprofiles.library.consumer.exclude.ExcludeClass

class EmptyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.txtNotice).setText(R.string.app_notice)

        val includeClass = IncludeClass()
        includeClass.doSomething()
        includeClass.doSomethingWithArgument("a string")
        includeClass.doSomethingWithReturnType(3, 5)

        val excludeClass = ExcludeClass()
        excludeClass.doSomething()
    }
}
