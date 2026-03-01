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

import java.util.concurrent.locks.ReentrantReadWriteLock

class DustCoreRegistry private constructor() {

    companion object {
        @Volatile
        private var instance: DustCoreRegistry? = null

        fun getInstance(): DustCoreRegistry =
            instance ?: synchronized(this) {
                instance ?: DustCoreRegistry().also { instance = it }
            }

        @JvmStatic
        fun resetForTesting() {
            synchronized(this) {
                instance?.let {
                    val w = it.rwLock.writeLock()
                    w.lock()
                    try {
                        it.vectorStore = null
                        it.modelServer = null
                        it.embeddingService = null
                    } finally {
                        w.unlock()
                    }
                }
                instance = null
            }
        }
    }

    private val rwLock = ReentrantReadWriteLock()
    private var vectorStore: VectorStore? = null
    private var modelServer: ModelServer? = null
    private var embeddingService: EmbeddingService? = null

    fun registerVectorStore(store: VectorStore) {
        val w = rwLock.writeLock()
        w.lock()
        try { vectorStore = store } finally { w.unlock() }
    }

    fun registerModelServer(server: ModelServer) {
        val w = rwLock.writeLock()
        w.lock()
        try { modelServer = server } finally { w.unlock() }
    }

    fun registerEmbeddingService(service: EmbeddingService) {
        val w = rwLock.writeLock()
        w.lock()
        try { embeddingService = service } finally { w.unlock() }
    }

    fun resolveVectorStore(): VectorStore {
        val r = rwLock.readLock()
        r.lock()
        try {
            return vectorStore ?: throw DustCoreError.ServiceNotRegistered("VectorStore")
        } finally { r.unlock() }
    }

    fun resolveModelServer(): ModelServer {
        val r = rwLock.readLock()
        r.lock()
        try {
            return modelServer ?: throw DustCoreError.ServiceNotRegistered("ModelServer")
        } finally { r.unlock() }
    }

    fun resolveEmbeddingService(): EmbeddingService {
        val r = rwLock.readLock()
        r.lock()
        try {
            return embeddingService ?: throw DustCoreError.ServiceNotRegistered("EmbeddingService")
        } finally { r.unlock() }
    }
}
