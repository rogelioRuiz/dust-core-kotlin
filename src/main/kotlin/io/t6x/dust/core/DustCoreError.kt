/*
 * Copyright 2026 T6X
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.t6x.dust.core

sealed class DustCoreError(override val message: String? = null) : Exception(message) {
    data object ModelNotFound : DustCoreError("Model not found")
    data object ModelNotReady : DustCoreError("Model not ready")
    data object ModelCorrupted : DustCoreError("Model file corrupted")
    data object FormatUnsupported : DustCoreError("Model format not supported")
    data object SessionClosed : DustCoreError("Session is closed")
    data object SessionLimitReached : DustCoreError("Session limit reached")
    data class InvalidInput(val detail: String? = null) : DustCoreError("Invalid input${detail?.let { ": $it" } ?: ""}")
    data class InferenceFailed(val detail: String? = null) : DustCoreError("Inference failed${detail?.let { ": $it" } ?: ""}")
    data object MemoryExhausted : DustCoreError("Memory exhausted")
    data class DownloadFailed(val detail: String? = null) : DustCoreError("Download failed${detail?.let { ": $it" } ?: ""}")
    data class StorageFull(val detail: String? = null) : DustCoreError("Storage full${detail?.let { ": $it" } ?: ""}")
    data class NetworkPolicyBlocked(val detail: String? = null) : DustCoreError("Network policy blocked${detail?.let { ": $it" } ?: ""}")
    data class VerificationFailed(val detail: String? = null) : DustCoreError("Verification failed${detail?.let { ": $it" } ?: ""}")
    data object Cancelled : DustCoreError("Operation cancelled")
    data object Timeout : DustCoreError("Operation timed out")
    data class ServiceNotRegistered(val serviceName: String) : DustCoreError("Service not registered: $serviceName")
    data class UnknownError(val errorMessage: String? = null) : DustCoreError(errorMessage ?: "Unknown error")
}
