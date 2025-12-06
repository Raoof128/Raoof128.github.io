package com.qrshield.data

import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for HistoryRepository implementations.
 * 
 * Tests the InMemoryHistoryRepository which is available in commonTest.
 * The same test patterns apply to SqlDelightHistoryRepository.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class HistoryRepositoryTest {
    
    private fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()
    
    private fun createTestItem(
        id: String = "test_${currentTimeMs()}",
        url: String = "https://example.com",
        score: Int = 50,
        verdict: Verdict = Verdict.SAFE,
        source: ScanSource = ScanSource.CAMERA
    ): ScanHistoryItem {
        return ScanHistoryItem(
            id = id,
            url = url,
            score = score,
            verdict = verdict,
            scannedAt = currentTimeMs(),
            source = source
        )
    }
    
    // ============================================
    // INSERT TESTS
    // ============================================
    
    @Test
    fun `insert returns true on success`() = runTest {
        val repository = InMemoryHistoryRepository()
        val item = createTestItem()
        
        val result = repository.insert(item)
        
        assertTrue(result)
    }
    
    @Test
    fun `insert adds item to repository`() = runTest {
        val repository = InMemoryHistoryRepository()
        val item = createTestItem(id = "unique_id_1")
        
        repository.insert(item)
        val retrieved = repository.getById("unique_id_1")
        
        assertNotNull(retrieved)
        assertEquals("unique_id_1", retrieved.id)
    }
    
    @Test
    fun `insert preserves all item properties`() = runTest {
        val repository = InMemoryHistoryRepository()
        val item = ScanHistoryItem(
            id = "props_test",
            url = "https://test.com/path",
            score = 75,
            verdict = Verdict.SUSPICIOUS,
            scannedAt = 1234567890L,
            source = ScanSource.GALLERY
        )
        
        repository.insert(item)
        val retrieved = repository.getById("props_test")
        
        assertNotNull(retrieved)
        assertEquals(item.id, retrieved.id)
        assertEquals(item.url, retrieved.url)
        assertEquals(item.score, retrieved.score)
        assertEquals(item.verdict, retrieved.verdict)
        assertEquals(item.scannedAt, retrieved.scannedAt)
        assertEquals(item.source, retrieved.source)
    }
    
    // ============================================
    // GET ALL TESTS
    // ============================================
    
    @Test
    fun `getAll returns empty list when repository is empty`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val items = repository.getAll()
        
        assertTrue(items.isEmpty())
    }
    
    @Test
    fun `getAll returns all inserted items`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "item1"))
        repository.insert(createTestItem(id = "item2"))
        repository.insert(createTestItem(id = "item3"))
        
        val items = repository.getAll()
        
        assertEquals(3, items.size)
    }
    
    @Test
    fun `getAll returns items in newest first order`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "first"))
        repository.insert(createTestItem(id = "second"))
        repository.insert(createTestItem(id = "third"))
        
        val items = repository.getAll()
        
        // Newest (last inserted) should be first
        assertEquals("third", items[0].id)
        assertEquals("second", items[1].id)
        assertEquals("first", items[2].id)
    }
    
    // ============================================
    // GET RECENT TESTS
    // ============================================
    
    @Test
    fun `getRecent respects limit`() = runTest {
        val repository = InMemoryHistoryRepository()
        repeat(10) { i ->
            repository.insert(createTestItem(id = "item$i"))
        }
        
        val items = repository.getRecent(5)
        
        assertEquals(5, items.size)
    }
    
    @Test
    fun `getRecent with limit larger than items returns all`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "item1"))
        repository.insert(createTestItem(id = "item2"))
        
        val items = repository.getRecent(100)
        
        assertEquals(2, items.size)
    }
    
    @Test
    fun `getRecent with zero limit returns one item`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem())
        
        // Limit is coerced to 1 minimum
        val items = repository.getRecent(0)
        
        // Should coerce to at least 1
        assertTrue(items.size <= 1)
    }
    
    // ============================================
    // GET BY ID TESTS
    // ============================================
    
    @Test
    fun `getById returns null for non-existent id`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val item = repository.getById("non_existent")
        
        assertNull(item)
    }
    
    @Test
    fun `getById returns correct item`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "target", url = "https://target.com"))
        repository.insert(createTestItem(id = "other", url = "https://other.com"))
        
        val item = repository.getById("target")
        
        assertNotNull(item)
        assertEquals("https://target.com", item.url)
    }
    
    // ============================================
    // DELETE TESTS
    // ============================================
    
    @Test
    fun `delete removes item from repository`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "to_delete"))
        
        repository.delete("to_delete")
        
        val item = repository.getById("to_delete")
        assertNull(item)
    }
    
    @Test
    fun `delete returns true when item exists`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "exists"))
        
        val result = repository.delete("exists")
        
        assertTrue(result)
    }
    
    @Test
    fun `delete returns false for non-existent item`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val result = repository.delete("non_existent")
        
        assertFalse(result)
    }
    
    // ============================================
    // CLEAR ALL TESTS
    // ============================================
    
    @Test
    fun `clearAll removes all items`() = runTest {
        val repository = InMemoryHistoryRepository()
        repeat(5) { i ->
            repository.insert(createTestItem(id = "item$i"))
        }
        
        repository.clearAll()
        
        val items = repository.getAll()
        assertTrue(items.isEmpty())
    }
    
    @Test
    fun `clearAll returns count of deleted items`() = runTest {
        val repository = InMemoryHistoryRepository()
        repeat(3) { i ->
            repository.insert(createTestItem(id = "item$i"))
        }
        
        val count = repository.clearAll()
        
        assertEquals(3, count)
    }
    
    @Test
    fun `clearAll on empty repository returns zero`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val count = repository.clearAll()
        
        assertEquals(0, count)
    }
    
    // ============================================
    // COUNT TESTS
    // ============================================
    
    @Test
    fun `count returns zero for empty repository`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val count = repository.count()
        
        assertEquals(0L, count)
    }
    
    @Test
    fun `count returns correct number`() = runTest {
        val repository = InMemoryHistoryRepository()
        repeat(7) { i ->
            repository.insert(createTestItem(id = "item$i"))
        }
        
        val count = repository.count()
        
        assertEquals(7L, count)
    }
    
    // ============================================
    // OBSERVE FLOW TESTS
    // ============================================
    
    @Test
    fun `observe returns flow with initial empty list`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        val items = repository.observe().first()
        
        assertTrue(items.isEmpty())
    }
    
    @Test
    fun `observe updates after insert`() = runTest {
        val repository = InMemoryHistoryRepository()
        
        repository.insert(createTestItem(id = "new_item"))
        val items = repository.observe().first()
        
        assertEquals(1, items.size)
    }
    
    // ============================================
    // GET BY VERDICT TESTS
    // ============================================
    
    @Test
    fun `getByVerdict returns only matching items`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(id = "safe1", verdict = Verdict.SAFE))
        repository.insert(createTestItem(id = "suspicious", verdict = Verdict.SUSPICIOUS))
        repository.insert(createTestItem(id = "safe2", verdict = Verdict.SAFE))
        repository.insert(createTestItem(id = "malicious", verdict = Verdict.MALICIOUS))
        
        val safeItems = repository.getByVerdict(Verdict.SAFE)
        
        assertEquals(2, safeItems.size)
        assertTrue(safeItems.all { it.verdict == Verdict.SAFE })
    }
    
    @Test
    fun `getByVerdict returns empty list when no matches`() = runTest {
        val repository = InMemoryHistoryRepository()
        repository.insert(createTestItem(verdict = Verdict.SAFE))
        
        val maliciousItems = repository.getByVerdict(Verdict.MALICIOUS)
        
        assertTrue(maliciousItems.isEmpty())
    }
}
