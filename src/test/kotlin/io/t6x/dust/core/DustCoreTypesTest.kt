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

import org.junit.Assert.*
import org.junit.Test

class DustCoreTypesTest {

    // ── M3-T4: Version constant ────────────────────────────────────────────

    @Test
    fun mlCoreVersionAccessible() {
        assertEquals("0.1.0", DustCore.VERSION)
        assertTrue(DustCore.VERSION.isNotEmpty())
    }

    // ── M1-T2: Interfaces compile — mock implementing VectorStore ───────────

    @Test
    fun vectorStoreInterfaceCompiles() {
        val mock = object : VectorStore {
            override suspend fun open(config: Map<String, String>) {}
            override suspend fun search(query: List<Float>, limit: Int) = emptyList<VectorSearchResult>()
            override suspend fun upsert(id: String, vector: List<Float>, metadata: Map<String, String>?) {}
            override suspend fun delete(id: String) {}
            override suspend fun close() {}
        }
        assertNotNull(mock)
    }

    @Test
    fun modelSessionInterfaceCompiles() {
        val mock = object : ModelSession {
            override suspend fun predict(inputs: List<DustInputTensor>) = emptyList<DustOutputTensor>()
            override fun status() = ModelStatus.Ready
            override fun priority() = SessionPriority.INTERACTIVE
            override suspend fun close() {}
        }
        assertNotNull(mock)
    }

    @Test
    fun embeddingServiceInterfaceCompiles() {
        val mock = object : EmbeddingService {
            override suspend fun embed(texts: List<String>) = emptyList<List<Float>>()
            override fun embeddingDimension() = 1536
            override fun status() = EmbeddingStatus.IDLE
        }
        assertNotNull(mock)
    }

    @Test
    fun modelServerInterfaceCompiles() {
        val mock = object : ModelServer {
            override suspend fun loadModel(descriptor: ModelDescriptor, priority: SessionPriority): ModelSession {
                return object : ModelSession {
                    override suspend fun predict(inputs: List<DustInputTensor>) = emptyList<DustOutputTensor>()
                    override fun status() = ModelStatus.Ready
                    override fun priority() = SessionPriority.INTERACTIVE
                    override suspend fun close() {}
                }
            }
            override suspend fun unloadModel(id: String) {}
            override suspend fun listModels() = emptyList<ModelDescriptor>()
            override suspend fun modelStatus(id: String): ModelStatus = ModelStatus.NotLoaded
        }
        assertNotNull(mock)
    }

    // ── M1-T3: ModelDescriptor value semantics ──────────────────────────────

    @Test
    fun modelDescriptorEquality() {
        val a = ModelDescriptor(
            id = "llama-3.2-1b",
            name = "Llama 3.2 1B",
            format = ModelFormat.GGUF,
            sizeBytes = 1_200_000_000L,
            version = "1.0.0",
            quantization = "Q4_K_M",
            metadata = mapOf("source" to "huggingface"),
        )
        val b = ModelDescriptor(
            id = "llama-3.2-1b",
            name = "Llama 3.2 1B",
            format = ModelFormat.GGUF,
            sizeBytes = 1_200_000_000L,
            version = "1.0.0",
            quantization = "Q4_K_M",
            metadata = mapOf("source" to "huggingface"),
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun modelDescriptorInequality() {
        val a = ModelDescriptor(id = "a", name = "A", format = ModelFormat.ONNX, sizeBytes = 100, version = "1.0")
        val b = ModelDescriptor(id = "b", name = "A", format = ModelFormat.ONNX, sizeBytes = 100, version = "1.0")
        assertNotEquals(a, b)
    }

    // ── M1-T4: SessionPriority ordering ─────────────────────────────────────

    @Test
    fun sessionPriorityOrdering() {
        assertEquals(0, SessionPriority.BACKGROUND.rawValue)
        assertEquals(1, SessionPriority.INTERACTIVE.rawValue)
        assertTrue(SessionPriority.BACKGROUND.rawValue < SessionPriority.INTERACTIVE.rawValue)
    }

    // ── M1-T5: ModelStatus associated values ────────────────────────────────

    @Test
    fun modelStatusDownloadingExtractsProgress() {
        val status: ModelStatus = ModelStatus.Downloading(progress = 0.47f)
        when (status) {
            is ModelStatus.Downloading -> assertEquals(0.47f, status.progress, 0.001f)
            else -> fail("Expected Downloading status")
        }
    }

    @Test
    fun modelStatusFailedExtractsError() {
        val status: ModelStatus = ModelStatus.Failed(error = DustCoreError.MemoryExhausted)
        when (status) {
            is ModelStatus.Failed -> assertEquals(DustCoreError.MemoryExhausted, status.error)
            else -> fail("Expected Failed status")
        }
    }

    // ── DustCoreError sealed class ────────────────────────────────────────────

    @Test
    fun mlCoreErrorIsThrowable() {
        val error: Exception = DustCoreError.ModelNotFound
        assertEquals("Model not found", error.message)
    }

    @Test
    fun mlCoreErrorWithDetail() {
        val error = DustCoreError.InvalidInput(detail = "shape mismatch")
        assertEquals("Invalid input: shape mismatch", error.message)
    }

    @Test
    fun mlCoreErrorWithoutDetail() {
        val error = DustCoreError.InvalidInput()
        assertEquals("Invalid input", error.message)
    }

    // ── ModelFormat ─────────────────────────────────────────────────────────

    @Test
    fun modelFormatFromValue() {
        assertEquals(ModelFormat.GGUF, ModelFormat.fromValue("gguf"))
        assertEquals(ModelFormat.ONNX, ModelFormat.fromValue("onnx"))
        assertNull(ModelFormat.fromValue("nonexistent"))
    }

    @Test
    fun modelFormatValues() {
        assertEquals("onnx", ModelFormat.ONNX.value)
        assertEquals("coreml", ModelFormat.COREML.value)
        assertEquals("tflite", ModelFormat.TFLITE.value)
        assertEquals("gguf", ModelFormat.GGUF.value)
        assertEquals("custom", ModelFormat.CUSTOM.value)
    }

    // ── EmbeddingStatus ─────────────────────────────────────────────────────

    @Test
    fun embeddingStatusValues() {
        assertEquals("idle", EmbeddingStatus.IDLE.value)
        assertEquals("computing", EmbeddingStatus.COMPUTING.value)
        assertEquals("ready", EmbeddingStatus.READY.value)
        assertEquals("failed", EmbeddingStatus.FAILED.value)
    }

    @Test
    fun embeddingStatusFromValue() {
        assertEquals(EmbeddingStatus.IDLE, EmbeddingStatus.fromValue("idle"))
        assertNull(EmbeddingStatus.fromValue("nonexistent"))
    }

    // ── Tensor equality ─────────────────────────────────────────────────────

    @Test
    fun inputTensorEquality() {
        val a = DustInputTensor(name = "input", data = listOf(1.0f, 2.0f), shape = listOf(1, 2))
        val b = DustInputTensor(name = "input", data = listOf(1.0f, 2.0f), shape = listOf(1, 2))
        assertEquals(a, b)
    }

    @Test
    fun outputTensorEquality() {
        val a = DustOutputTensor(name = "output", data = listOf(0.1f, 0.9f), shape = listOf(1, 2))
        val b = DustOutputTensor(name = "output", data = listOf(0.1f, 0.9f), shape = listOf(1, 2))
        assertEquals(a, b)
    }

    // ── VectorSearchResult ──────────────────────────────────────────────────

    @Test
    fun vectorSearchResultEquality() {
        val a = VectorSearchResult(id = "doc1", score = 0.95f, metadata = mapOf("key" to "val"))
        val b = VectorSearchResult(id = "doc1", score = 0.95f, metadata = mapOf("key" to "val"))
        assertEquals(a, b)
    }

    // ── Exhaustive when matching ────────────────────────────────────────────

    @Test
    fun modelStatusExhaustiveMatch() {
        val statuses = listOf<ModelStatus>(
            ModelStatus.NotLoaded,
            ModelStatus.Downloading(0.5f),
            ModelStatus.Verifying,
            ModelStatus.Loading,
            ModelStatus.Ready,
            ModelStatus.Failed(DustCoreError.Timeout),
            ModelStatus.Unloading,
        )
        for (status in statuses) {
            val label = when (status) {
                is ModelStatus.NotLoaded -> "notLoaded"
                is ModelStatus.Downloading -> "downloading"
                is ModelStatus.Verifying -> "verifying"
                is ModelStatus.Loading -> "loading"
                is ModelStatus.Ready -> "ready"
                is ModelStatus.Failed -> "failed"
                is ModelStatus.Unloading -> "unloading"
            }
            assertNotNull(label)
        }
    }

    @Test
    fun mlCoreErrorExhaustiveMatch() {
        val errors = listOf<DustCoreError>(
            DustCoreError.ModelNotFound,
            DustCoreError.ModelNotReady,
            DustCoreError.ModelCorrupted,
            DustCoreError.FormatUnsupported,
            DustCoreError.SessionClosed,
            DustCoreError.SessionLimitReached,
            DustCoreError.InvalidInput("test"),
            DustCoreError.InferenceFailed("test"),
            DustCoreError.MemoryExhausted,
            DustCoreError.DownloadFailed("test"),
            DustCoreError.StorageFull("test"),
            DustCoreError.NetworkPolicyBlocked("test"),
            DustCoreError.VerificationFailed("test"),
            DustCoreError.Cancelled,
            DustCoreError.Timeout,
            DustCoreError.ServiceNotRegistered("TestService"),
            DustCoreError.UnknownError("test"),
        )
        for (error in errors) {
            val label = when (error) {
                is DustCoreError.ModelNotFound -> "modelNotFound"
                is DustCoreError.ModelNotReady -> "modelNotReady"
                is DustCoreError.ModelCorrupted -> "modelCorrupted"
                is DustCoreError.FormatUnsupported -> "formatUnsupported"
                is DustCoreError.SessionClosed -> "sessionClosed"
                is DustCoreError.SessionLimitReached -> "sessionLimitReached"
                is DustCoreError.InvalidInput -> "invalidInput"
                is DustCoreError.InferenceFailed -> "inferenceFailed"
                is DustCoreError.MemoryExhausted -> "memoryExhausted"
                is DustCoreError.DownloadFailed -> "downloadFailed"
                is DustCoreError.StorageFull -> "storageFull"
                is DustCoreError.NetworkPolicyBlocked -> "networkPolicyBlocked"
                is DustCoreError.VerificationFailed -> "verificationFailed"
                is DustCoreError.Cancelled -> "cancelled"
                is DustCoreError.Timeout -> "timeout"
                is DustCoreError.ServiceNotRegistered -> "serviceNotRegistered"
                is DustCoreError.UnknownError -> "unknownError"
            }
            assertNotNull(label)
        }
    }
}
