# Contributing to dust-core-kotlin

Thanks for your interest in contributing! This guide will help you get set up and understand our development workflow.

## Prerequisites

- **JDK 17** or later
- **Git**

No Android SDK or emulator is needed — this is a pure JVM library.

## Getting Started

```bash
# Clone the repo
git clone <repo-url>
cd dust-core-kotlin

# Build
./gradlew build

# Run tests
./gradlew test
```

## Project Structure

```
src/main/kotlin/io/t6x/dust/core/
  DustCore.kt             # VERSION constant
  Types.kt              # ModelDescriptor, ModelStatus, tensors, enums
  DustCoreError.kt        # Sealed class with 17 error cases
  ModelServer.kt        # suspend interface — model lifecycle
  ModelSession.kt       # suspend interface — inference
  VectorStore.kt        # suspend interface — vector search
  EmbeddingService.kt   # suspend interface — text-to-vector
  DustCoreRegistry.kt     # Thread-safe singleton registry

src/test/kotlin/io/t6x/dust/core/
  DustCoreRegistryTest.kt # 13 tests — singleton, register/resolve, concurrency
  DustCoreTypesTest.kt    # 22 tests — interfaces, enums, tensors, errors
```

## Making Changes

### 1. Create a branch

```bash
git checkout -b feat/my-feature
```

### 2. Make your changes

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- No wildcard imports
- All new public APIs must be `suspend` (coroutine-based)
- Add tests for new functionality

### 3. Add the license header

All `.kt` files must include the Apache 2.0 header:

```kotlin
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
```

### 4. Run checks

```bash
./gradlew test        # All 35 tests must pass
./gradlew build       # Clean build
```

### 5. Commit with a conventional message

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add BatchEmbedding interface
fix: correct ModelStatus sealed hierarchy
docs: update README examples
chore(deps): bump coroutines to 1.11
```

### 6. Open a pull request

Push your branch and open a PR against `main`.

## Reporting Issues

- **Bugs**: Open an issue with steps to reproduce
- **Features**: Open an issue describing the use case and proposed API

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). Please be respectful and constructive.

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
