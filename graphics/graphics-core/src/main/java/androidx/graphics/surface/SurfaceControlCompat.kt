/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.graphics.surface

import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
class SurfaceControlCompat internal constructor(
    surface: Surface? = null,
    surfaceControl: SurfaceControlCompat? = null,
    debugName: String
) {
    private var mNativeSurfaceControl: Long = 0

    init {
        if (surface != null) {
            mNativeSurfaceControl = nCreateFromWindow(surface, debugName)
        } else if (surfaceControl != null) {
            mNativeSurfaceControl = nCreate(surfaceControl.mNativeSurfaceControl, debugName)
        }

        if (mNativeSurfaceControl == 0L) {
            throw IllegalArgumentException()
        }
    }

    open class Transaction() {
        private var mNativeSurfaceTransaction: Long

        init {
            mNativeSurfaceTransaction = nTransactionCreate()
            if (mNativeSurfaceTransaction == 0L) {
                throw java.lang.IllegalArgumentException()
            }
        }

        fun delete() {
            if (mNativeSurfaceTransaction != 0L) {
                nTransactionDelete(mNativeSurfaceTransaction)
            }
            mNativeSurfaceTransaction = 0L
        }

        fun finalize() {
            delete()
        }

        private external fun nTransactionCreate(): Long
        private external fun nTransactionDelete(surfaceTransaction: Long)

        companion object {
            init {
                System.loadLibrary("graphics-core")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) {
            return true
        }
        if ((other == null) or
            (other?.javaClass != SurfaceControlCompat::class.java)
        ) {
            return false
        }

        other as SurfaceControlCompat
        if (other.mNativeSurfaceControl == this.mNativeSurfaceControl) {
            return true
        }

        return false
    }

    override fun hashCode(): Int {
        return mNativeSurfaceControl.hashCode()
    }

    protected fun finalize() {
        nRelease(mNativeSurfaceControl)
        mNativeSurfaceControl = 0
    }

    /**
     * Builder class for SurfaceControlCompat.
     *
     * Requires a parent surface. Debug name is default empty string.
     */
    class Builder private constructor(surface: Surface?, surfaceControl: SurfaceControlCompat?) {
        private var mSurface: Surface? = surface
        private var mSurfaceControl: SurfaceControlCompat? = surfaceControl
        private var mDebugName: String = ""

        constructor(surface: Surface) : this(surface, null) {}

        constructor(surfaceControl: SurfaceControlCompat) : this(null, surfaceControl) {}

        @Suppress("MissingGetterMatchingBuilder")
        fun setDebugName(debugName: String): Builder {
            mDebugName = debugName
            return this
        }

        /**
         * Builds the SurfaceControlCompat object
         */
        fun build(): SurfaceControlCompat {
            return SurfaceControlCompat(mSurface, mSurfaceControl, mDebugName)
        }
    }

    private external fun nCreate(surfaceControl: Long, debugName: String): Long
    private external fun nCreateFromWindow(surface: Surface, debugName: String): Long
    private external fun nRelease(surfaceControl: Long)

    companion object {
        init {
            System.loadLibrary("graphics-core")
        }
    }
}