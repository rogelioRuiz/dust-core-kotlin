# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] — Unreleased

### Added

- `ModelServer` suspend interface — model lifecycle (`loadModel`, `unloadModel`, `listModels`, `modelStatus`)
- `ModelSession` suspend interface — inference (`predict`, `status`, `priority`, `close`)
- `VectorStore` suspend interface — vector search (`open`, `search`, `upsert`, `delete`, `close`)
- `EmbeddingService` suspend interface — text-to-vector (`embed`, `embeddingDimension`, `status`)
- `DustCoreRegistry` — thread-safe singleton service locator with `ReentrantReadWriteLock`
- `ModelDescriptor` data class with format, size, versioning, SHA256, and metadata fields
- `ModelStatus` sealed class lifecycle: `NotLoaded` → `Downloading` → `Verifying` → `Loading` → `Ready` / `Failed`
- `DustCoreError` sealed class with 17 error cases
- `DustInputTensor` / `DustOutputTensor` data classes
- `ModelFormat` enum: ONNX, COREML, TFLITE, GGUF, CUSTOM
- `SessionPriority` enum: BACKGROUND, INTERACTIVE
- 35 JUnit tests (13 registry + 22 types)
