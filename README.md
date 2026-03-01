<p align="center">
  <img alt="dust" src="assets/dust_banner.png" width="400">
</p>

# dust-core-kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple.svg)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-17+-orange.svg)](https://openjdk.org)

DustCore contract types and interfaces for on-device ML — pure Kotlin/JVM, no Android dependency.

**Version: 0.1.0**

## Overview

Defines the shared interfaces and value types that ML plugins implement. Contains zero platform-specific code — runs on any JVM 17+ target.

```
dust-core-kotlin/
├── build.gradle                          # java-library + kotlin-jvm + maven-publish
├── settings.gradle                       # pluginManagement for standalone builds
├── VERSION                               # Single source of truth for version string
└── src/main/kotlin/io/t6x/dust/core/
    ├── DustCore.kt                         # VERSION constant
    ├── Types.kt                          # ModelDescriptor, ModelStatus, tensors, enums
    ├── DustCoreError.kt                    # Sealed class (17 error cases)
    ├── ModelServer.kt                    # suspend interface — model lifecycle
    ├── ModelSession.kt                   # suspend interface — inference
    ├── VectorStore.kt                    # suspend interface — vector search
    ├── EmbeddingService.kt               # suspend interface — text-to-vector
    └── DustCoreRegistry.kt                 # Thread-safe singleton (ReentrantReadWriteLock)
```

## Install

### Gradle — local project dependency

```groovy
// settings.gradle
include ':dust-core-kotlin'
project(':dust-core-kotlin').projectDir = new File('../dust-core-kotlin')

// build.gradle
dependencies {
    implementation project(':dust-core-kotlin')
}
```

### Gradle — Maven (when published)

```groovy
dependencies {
    implementation 'io.t6x.dust:dust-core:0.1.0'
}
```

## Interfaces

| Interface | Methods | Purpose |
|-----------|---------|---------|
| `ModelServer` | `loadModel`, `unloadModel`, `listModels`, `modelStatus` | Model lifecycle |
| `ModelSession` | `predict`, `status`, `priority`, `close` | Inference |
| `VectorStore` | `open`, `search`, `upsert`, `delete`, `close` | Vector search |
| `EmbeddingService` | `embed`, `embeddingDimension`, `status` | Text-to-vector |

All methods are `suspend` (coroutine-based async).

## Usage

### Implement a ModelServer

```kotlin
import io.t6x.dust.core.*

class MyModelServer : ModelServer {
    override suspend fun loadModel(
        descriptor: ModelDescriptor,
        priority: SessionPriority
    ): ModelSession {
        // Load model from descriptor.url, return session
    }

    override suspend fun unloadModel(id: String) { /* ... */ }
    override suspend fun listModels(): List<ModelDescriptor> { /* ... */ }
    override suspend fun modelStatus(id: String): ModelStatus { /* ... */ }
}
```

### Register and resolve via DustCoreRegistry

```kotlin
// Register at startup
DustCoreRegistry.getInstance().registerModelServer(MyModelServer())

// Resolve from anywhere
val server = DustCoreRegistry.getInstance().resolveModelServer()
val session = server.loadModel(descriptor, SessionPriority.INTERACTIVE)
val outputs = session.predict(listOf(
    DustInputTensor(name = "input", data = listOf(1.0f, 2.0f), shape = listOf(1, 2))
))
```

### Create a ModelDescriptor

```kotlin
val descriptor = ModelDescriptor(
    id = "my-model",
    name = "My Model",
    format = ModelFormat.GGUF,
    sizeBytes = 4_000_000_000L,
    version = "1.0",
    url = "/path/to/model.gguf",
    quantization = "Q4_K_M"
)
```

### Error handling

```kotlin
try {
    val server = DustCoreRegistry.getInstance().resolveModelServer()
} catch (e: DustCoreError.ServiceNotRegistered) {
    // No ModelServer registered yet
}

try {
    val session = server.loadModel(descriptor, SessionPriority.INTERACTIVE)
} catch (e: DustCoreError.ModelNotFound) {
    // Model file not found
} catch (e: DustCoreError.InferenceFailed) {
    println("Inference failed: ${e.message}")
}
```

## Value types

| Type | Kind | Fields |
|------|------|--------|
| `ModelDescriptor` | data class | `id`, `name`, `format`, `sizeBytes`, `version`, `url?`, `sha256?`, `quantization?`, `metadata?` |
| `ModelStatus` | sealed class | `NotLoaded`, `Downloading(progress)`, `Verifying`, `Loading`, `Ready`, `Failed(error)`, `Unloading` |
| `DustInputTensor` | data class | `name`, `data: List<Float>`, `shape: List<Int>` |
| `DustOutputTensor` | data class | `name`, `data: List<Float>`, `shape: List<Int>` |
| `VectorSearchResult` | data class | `id`, `score`, `metadata?` |
| `ModelFormat` | enum | `ONNX`, `COREML`, `TFLITE`, `GGUF`, `CUSTOM` |
| `SessionPriority` | enum | `BACKGROUND(0)`, `INTERACTIVE(1)` |
| `EmbeddingStatus` | enum | `IDLE`, `COMPUTING`, `READY`, `FAILED` |

## Thread safety

`DustCoreRegistry` uses `ReentrantReadWriteLock` — concurrent reads are lock-free, writes are exclusive. Safe to call from any thread or coroutine dispatcher.

## Test

```bash
cd dust-core-kotlin
./gradlew test    # 35 JUnit tests (13 registry + 22 types)
```

No Android SDK or emulator needed — pure JVM tests.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, coding conventions, and PR guidelines.

## License

Copyright 2026 T6X. Licensed under the [Apache License 2.0](LICENSE).
