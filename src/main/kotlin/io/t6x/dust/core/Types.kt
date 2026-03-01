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

// ─── ModelFormat ────────────────────────────────────────────────────────────

enum class ModelFormat(val value: String) {
    ONNX("onnx"),
    COREML("coreml"),
    TFLITE("tflite"),
    GGUF("gguf"),
    CUSTOM("custom");

    companion object {
        fun fromValue(value: String): ModelFormat? = entries.find { it.value == value }
    }
}

// ─── SessionPriority ────────────────────────────────────────────────────────

enum class SessionPriority(val rawValue: Int) {
    BACKGROUND(0),
    INTERACTIVE(1);

    companion object {
        fun fromRawValue(value: Int): SessionPriority? = entries.find { it.rawValue == value }
    }
}

// ─── ModelStatus ────────────────────────────────────────────────────────────

sealed class ModelStatus {
    data object NotLoaded : ModelStatus()
    data class Downloading(val progress: Float) : ModelStatus()
    data object Verifying : ModelStatus()
    data object Loading : ModelStatus()
    data object Ready : ModelStatus()
    data class Failed(val error: DustCoreError) : ModelStatus()
    data object Unloading : ModelStatus()
}

// ─── EmbeddingStatus ────────────────────────────────────────────────────────

enum class EmbeddingStatus(val value: String) {
    IDLE("idle"),
    COMPUTING("computing"),
    READY("ready"),
    FAILED("failed");

    companion object {
        fun fromValue(value: String): EmbeddingStatus? = entries.find { it.value == value }
    }
}

// ─── ModelDescriptor ────────────────────────────────────────────────────────

data class ModelDescriptor(
    val id: String,
    val name: String,
    val format: ModelFormat,
    val sizeBytes: Long,
    val version: String,
    val url: String? = null,
    val sha256: String? = null,
    val quantization: String? = null,
    val metadata: Map<String, String>? = null,
)

// ─── VectorSearchResult ─────────────────────────────────────────────────────

data class VectorSearchResult(
    val id: String,
    val score: Float,
    val metadata: Map<String, String>? = null,
)

// ─── DustInputTensor ──────────────────────────────────────────────────────

data class DustInputTensor(
    val name: String,
    val data: List<Float>,
    val shape: List<Int>,
)

// ─── DustOutputTensor ─────────────────────────────────────────────────────

data class DustOutputTensor(
    val name: String,
    val data: List<Float>,
    val shape: List<Int>,
)
