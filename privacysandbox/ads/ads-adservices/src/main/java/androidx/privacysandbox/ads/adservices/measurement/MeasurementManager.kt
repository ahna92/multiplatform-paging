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

package androidx.privacysandbox.ads.adservices.measurement

import android.adservices.common.AdServicesPermissions.ACCESS_ADSERVICES_ATTRIBUTION
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.InputEvent
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresPermission
import androidx.core.os.BuildCompat
import androidx.core.os.asOutcomeReceiver
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * This class provides APIs to manage ads attribution using Privacy Sandbox.
 */
abstract class MeasurementManager {
    /**
     * Delete previous registrations.
     *
     * @param deletionRequest The request for deleting data.
     */
    @DoNotInline
    abstract suspend fun deleteRegistrations(deletionRequest: DeletionRequest)

    /**
     * Register an attribution source (click or view).
     *
     * @param attributionSource the platform issues a request to this URI in order to fetch metadata
     *     associated with the attribution source.
     * @param inputEvent either an {@link InputEvent} object (for a click event) or null (for a view
     *     event).
     */
    @DoNotInline
    @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
    abstract suspend fun registerSource(attributionSource: Uri, inputEvent: InputEvent?)

    /**
     * Register a trigger (conversion).
     *
     * @param trigger the API issues a request to this URI to fetch metadata associated with the
     *     trigger.
     */
    // TODO(b/258551492): Improve docs.
    @DoNotInline
    @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
    abstract suspend fun registerTrigger(trigger: Uri)

    /**
     * Register an attribution source(click or view) from web context. This API will not process any
     * redirects, all registration URLs should be supplied with the request. At least one of
     * appDestination or webDestination parameters are required to be provided.
     *
     * @param request source registration request
     */
    @DoNotInline
    @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
    abstract suspend fun registerWebSource(request: WebSourceRegistrationRequest)

    /**
     * Register an attribution trigger(click or view) from web context. This API will not process
     * any redirects, all registration URLs should be supplied with the request.
     *
     * @param request trigger registration request
     */
    @DoNotInline
    @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
    abstract suspend fun registerWebTrigger(request: WebTriggerRegistrationRequest)

    /**
     * Get Measurement API status.
     *
     * <p>The call returns an integer value (see {@link MEASUREMENT_API_STATE_DISABLED} and
     * {@link MEASUREMENT_API_STATE_ENABLED} for possible values).
     */
    @DoNotInline
    @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
    abstract suspend fun getMeasurementApiStatus(): Int

    @SuppressLint("ClassVerificationFailure", "NewApi")
    private class Api33Ext4Impl(
        private val mMeasurementManager: android.adservices.measurement.MeasurementManager
    ) : MeasurementManager() {
        constructor(context: Context) : this(
            context.getSystemService<android.adservices.measurement.MeasurementManager>(
                android.adservices.measurement.MeasurementManager::class.java
            )
        )

        @DoNotInline
        override suspend fun deleteRegistrations(deletionRequest: DeletionRequest) {
            suspendCancellableCoroutine<Any> { continuation ->
                mMeasurementManager.deleteRegistrations(
                    convertDeletionRequest(deletionRequest),
                    Runnable::run,
                    continuation.asOutcomeReceiver()
                )
            }
        }

        private fun convertDeletionRequest(
            request: DeletionRequest
        ): android.adservices.measurement.DeletionRequest {
            return android.adservices.measurement.DeletionRequest.Builder()
                .setDeletionMode(request.deletionMode.ordinal)
                .setMatchBehavior(request.matchBehavior.ordinal)
                .setStart(request.start)
                .setEnd(request.end)
                .setDomainUris(request.domainUris)
                .setOriginUris(request.originUris)
                .build()
        }

        @DoNotInline
        @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
        override suspend fun registerSource(attributionSource: Uri, inputEvent: InputEvent?) {
            suspendCancellableCoroutine<Any> { continuation ->
                mMeasurementManager.registerSource(
                    attributionSource,
                    inputEvent,
                    Runnable::run,
                    continuation.asOutcomeReceiver()
                )
            }
        }

        @DoNotInline
        @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
        override suspend fun registerTrigger(trigger: Uri) {
            suspendCancellableCoroutine<Any> { continuation ->
                mMeasurementManager.registerTrigger(
                    trigger,
                    Runnable::run,
                    continuation.asOutcomeReceiver())
            }
        }

        @DoNotInline
        @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
        override suspend fun registerWebSource(request: WebSourceRegistrationRequest) {
            suspendCancellableCoroutine<Any> { continuation ->
                mMeasurementManager.registerWebSource(
                    convertWebSourceRequest(request),
                    Runnable::run,
                    continuation.asOutcomeReceiver())
            }
        }

        private fun convertWebSourceRequest(
            request: WebSourceRegistrationRequest
        ): android.adservices.measurement.WebSourceRegistrationRequest {
            return android.adservices.measurement.WebSourceRegistrationRequest
                .Builder(
                    convertWebSourceParams(request.webSourceParams),
                    request.topOriginUri)
                .setWebDestination(request.webDestination)
                .setAppDestination(request.appDestination)
                .setInputEvent(request.inputEvent)
                .setVerifiedDestination(request.verifiedDestination)
                .build()
        }

        private fun convertWebSourceParams(
            request: List<WebSourceParams>
        ): List<android.adservices.measurement.WebSourceParams> {
            var result = mutableListOf<android.adservices.measurement.WebSourceParams>()
            for (param in request) {
                result.add(android.adservices.measurement.WebSourceParams
                    .Builder(param.registrationUri)
                    .setDebugKeyAllowed(param.debugKeyAllowed)
                    .build())
            }
            return result
        }

        @DoNotInline
        @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
        override suspend fun registerWebTrigger(request: WebTriggerRegistrationRequest) {
            suspendCancellableCoroutine<Any> { continuation ->
                mMeasurementManager.registerWebTrigger(
                    convertWebTriggerRequest(request),
                    Runnable::run,
                    continuation.asOutcomeReceiver())
            }
        }

        private fun convertWebTriggerRequest(
            request: WebTriggerRegistrationRequest
        ): android.adservices.measurement.WebTriggerRegistrationRequest {
            return android.adservices.measurement.WebTriggerRegistrationRequest
                .Builder(
                    convertWebTriggerParams(request.webTriggerParams),
                    request.destination)
                .build()
        }

        private fun convertWebTriggerParams(
            request: List<WebTriggerParams>
        ): List<android.adservices.measurement.WebTriggerParams> {
            var result = mutableListOf<android.adservices.measurement.WebTriggerParams>()
            for (param in request) {
                result.add(android.adservices.measurement.WebTriggerParams
                    .Builder(param.registrationUri)
                    .setDebugKeyAllowed(param.debugKeyAllowed)
                    .build())
            }
            return result
        }

        @DoNotInline
        @RequiresPermission(ACCESS_ADSERVICES_ATTRIBUTION)
        // TODO(b/259446937): Update this API to conform with API guidelines once clarified.
        override suspend fun getMeasurementApiStatus(): Int = suspendCancellableCoroutine {
                continuation ->
            mMeasurementManager.getMeasurementApiStatus(
                Runnable::run,
                continuation.asOutcomeReceiver())
        }
    }

    companion object {
        /**
         * This state indicates that Measurement APIs are unavailable. Invoking them will result
         * in an {@link UnsupportedOperationException}.
         */
        public const val MEASUREMENT_API_STATE_DISABLED = 0
        /**
         * This state indicates that Measurement APIs are enabled.
         */
        public const val MEASUREMENT_API_STATE_ENABLED = 1

        /**
         *  Creates [MeasurementManager].
         *
         *  @return MeasurementManager object.
         */
        @JvmStatic
        @androidx.annotation.OptIn(markerClass = [BuildCompat.PrereleaseSdkCheck::class])
        @SuppressLint("NewApi", "ClassVerificationFailure")
        fun obtain(context: Context): MeasurementManager? {
            // TODO: Add check SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4
            return if (BuildCompat.isAtLeastU()) {
                Api33Ext4Impl(context)
            } else {
                null
            }
        }
    }
}
