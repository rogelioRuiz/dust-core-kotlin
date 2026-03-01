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

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class DustCoreRegistryTest {

    @Before
    fun setUp() {
        DustCoreRegistry.resetForTesting()
    }

    @After
    fun tearDown() {
        DustCoreRegistry.resetForTesting()
    }

    // ── Singleton ──────────────────────────────────────────────────────────

    @Test
    fun getInstanceReturnsSameInstance() {
        assertSame(DustCoreRegistry.getInstance(), DustCoreRegistry.getInstance())
    }

    // ── M2-T1: Resolve unregistered throws ServiceNotRegistered ────────────

    @Test(expected = DustCoreError.ServiceNotRegistered::class)
    fun resolveUnregisteredVectorStoreThrows() {
        DustCoreRegistry.getInstance().resolveVectorStore()
    }

    @Test
    fun resolveUnregisteredVectorStoreErrorMessage() {
        try {
            DustCoreRegistry.getInstance().resolveVectorStore()
            fail("Expected ServiceNotRegistered")
        } catch (e: DustCoreError.ServiceNotRegistered) {
            assertEquals("VectorStore", e.serviceName)
            assertEquals("Service not registered: VectorStore", e.message)
        }
    }

    @Test(expected = DustCoreError.ServiceNotRegistered::class)
    fun resolveUnregisteredModelServerThrows() {
        DustCoreRegistry.getInstance().resolveModelServer()
    }

    @Test(expected = DustCoreError.ServiceNotRegistered::class)
    fun resolveUnregisteredEmbeddingServiceThrows() {
        DustCoreRegistry.getInstance().resolveEmbeddingService()
    }

    // ── M2-T2: Register + resolve round-trip (identity) ────────────────────

    @Test
    fun registerResolveVectorStoreIdentity() {
        val mock = mockVectorStore()
        val registry = DustCoreRegistry.getInstance()
        registry.registerVectorStore(mock)
        assertSame(mock, registry.resolveVectorStore())
    }

    @Test
    fun registerResolveModelServerIdentity() {
        val mock = mockModelServer()
        val registry = DustCoreRegistry.getInstance()
        registry.registerModelServer(mock)
        assertSame(mock, registry.resolveModelServer())
    }

    @Test
    fun registerResolveEmbeddingServiceIdentity() {
        val mock = mockEmbeddingService()
        val registry = DustCoreRegistry.getInstance()
        registry.registerEmbeddingService(mock)
        assertSame(mock, registry.resolveEmbeddingService())
    }

    // ── M2-T3: Re-registration replaces previous (last-write-wins) ─────────

    @Test
    fun reRegisterVectorStoreLastWriteWins() {
        val first = mockVectorStore()
        val second = mockVectorStore()
        val registry = DustCoreRegistry.getInstance()
        registry.registerVectorStore(first)
        registry.registerVectorStore(second)
        assertSame(second, registry.resolveVectorStore())
        assertNotSame(first, registry.resolveVectorStore())
    }

    @Test
    fun reRegisterModelServerLastWriteWins() {
        val first = mockModelServer()
        val second = mockModelServer()
        val registry = DustCoreRegistry.getInstance()
        registry.registerModelServer(first)
        registry.registerModelServer(second)
        assertSame(second, registry.resolveModelServer())
    }

    @Test
    fun reRegisterEmbeddingServiceLastWriteWins() {
        val first = mockEmbeddingService()
        val second = mockEmbeddingService()
        val registry = DustCoreRegistry.getInstance()
        registry.registerEmbeddingService(first)
        registry.registerEmbeddingService(second)
        assertSame(second, registry.resolveEmbeddingService())
    }

    // ── M2-T4: 100 threads concurrent register — no crash/deadlock ─────────

    @Test
    fun concurrentRegisterNoCrash() {
        val threadCount = 100
        val barrier = CyclicBarrier(threadCount)
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val error = AtomicReference<Throwable?>()

        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    barrier.await(5, TimeUnit.SECONDS)
                    val mock = mockVectorStore()
                    DustCoreRegistry.getInstance().registerVectorStore(mock)
                } catch (t: Throwable) {
                    error.compareAndSet(null, t)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue("Timed out", latch.await(10, TimeUnit.SECONDS))
        assertNull("Thread error: ${error.get()}", error.get())
        assertNotNull(DustCoreRegistry.getInstance().resolveVectorStore())
        executor.shutdownNow()
    }

    // ── M2-T5: 1000 concurrent resolve — all return same instance ──────────

    @Test
    fun concurrentResolveReturnsSameInstance() {
        val mock = mockVectorStore()
        DustCoreRegistry.getInstance().registerVectorStore(mock)

        val taskCount = 1000
        val poolSize = minOf(taskCount, Runtime.getRuntime().availableProcessors() * 4)
        val barrier = CyclicBarrier(poolSize)
        val latch = CountDownLatch(taskCount)
        val results = java.util.concurrent.ConcurrentLinkedQueue<VectorStore>()
        val executor = Executors.newFixedThreadPool(poolSize)
        val error = AtomicReference<Throwable?>()

        for (i in 0 until taskCount) {
            executor.submit {
                try {
                    if (i < poolSize) barrier.await(5, TimeUnit.SECONDS)
                    val resolved = DustCoreRegistry.getInstance().resolveVectorStore()
                    results.add(resolved)
                } catch (t: Throwable) {
                    error.compareAndSet(null, t)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue("Timed out", latch.await(30, TimeUnit.SECONDS))
        assertNull("Thread error: ${error.get()}", error.get())
        assertEquals(taskCount, results.size)
        for (r in results) {
            assertSame(mock, r)
        }
        executor.shutdownNow()
    }

    // ── Mock helpers ───────────────────────────────────────────────────────

    private fun mockVectorStore() = object : VectorStore {
        override suspend fun open(config: Map<String, String>) {}
        override suspend fun search(query: List<Float>, limit: Int) = emptyList<VectorSearchResult>()
        override suspend fun upsert(id: String, vector: List<Float>, metadata: Map<String, String>?) {}
        override suspend fun delete(id: String) {}
        override suspend fun close() {}
    }

    private fun mockModelServer() = object : ModelServer {
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

    private fun mockEmbeddingService() = object : EmbeddingService {
        override suspend fun embed(texts: List<String>) = emptyList<List<Float>>()
        override fun embeddingDimension() = 1536
        override fun status() = EmbeddingStatus.IDLE
    }
}
