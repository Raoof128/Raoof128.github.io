package com.qrshield.data

import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * Repository interface for scan history persistence.
 * 
 * Implementations should be platform-specific:
 * - Android: SQLDelight with Android driver
 * - iOS: SQLDelight with Native driver
 * - Desktop: SQLDelight with JVM driver
 * - Web: SQLDelight with Web driver
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
interface HistoryRepository {
    
    /**
     * Insert a new scan record.
     * 
     * @param item The scan history item to save
     * @return true if successful, false otherwise
     */
    suspend fun insert(item: ScanHistoryItem): Boolean
    
    /**
     * Get all scans ordered by date (newest first).
     * 
     * @return List of all scan history items
     */
    suspend fun getAll(): List<ScanHistoryItem>
    
    /**
     * Get recent scans with limit.
     * 
     * @param limit Maximum number of items to return
     * @return List of recent scan history items
     */
    suspend fun getRecent(limit: Int): List<ScanHistoryItem>
    
    /**
     * Get scan by ID.
     * 
     * @param id The scan ID
     * @return ScanHistoryItem if found, null otherwise
     */
    suspend fun getById(id: String): ScanHistoryItem?
    
    /**
     * Delete scan by ID.
     * 
     * @param id The scan ID to delete
     * @return true if deleted, false if not found
     */
    suspend fun delete(id: String): Boolean
    
    /**
     * Clear all history.
     * 
     * @return Number of items deleted
     */
    suspend fun clearAll(): Int
    
    /**
     * Get total count of scans.
     * 
     * @return Total number of scan records
     */
    suspend fun count(): Long
    
    /**
     * Observe history changes as Flow.
     * 
     * @return Flow of history item lists
     */
    fun observe(): Flow<List<ScanHistoryItem>>
    
    /**
     * Get scans by verdict.
     * 
     * @param verdict The verdict to filter by
     * @return List of scan history items with matching verdict
     */
    suspend fun getByVerdict(verdict: Verdict): List<ScanHistoryItem>
}

/**
 * In-memory implementation for testing and initial development.
 * 
 * NOT for production use - data is lost on app restart.
 */
class InMemoryHistoryRepository : HistoryRepository {
    
    companion object {
        private const val MAX_ITEMS = 1000
    }
    
    private val mutex = Mutex()
    private val items = mutableListOf<ScanHistoryItem>()
    private val _historyFlow = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    
    override suspend fun insert(item: ScanHistoryItem): Boolean = mutex.withLock {
        // Remove oldest if at capacity
        if (items.size >= MAX_ITEMS) {
            items.removeAt(items.size - 1)
        }
        
        // Insert at beginning (newest first)
        items.add(0, item)
        _historyFlow.value = items.toList()
        true
    }
    
    override suspend fun getAll(): List<ScanHistoryItem> = mutex.withLock {
        items.toList()
    }
    
    override suspend fun getRecent(limit: Int): List<ScanHistoryItem> = mutex.withLock {
        items.take(limit.coerceIn(1, MAX_ITEMS))
    }
    
    override suspend fun getById(id: String): ScanHistoryItem? = mutex.withLock {
        items.find { it.id == id }
    }
    
    override suspend fun delete(id: String): Boolean = mutex.withLock {
        val removed = items.removeAll { it.id == id }
        if (removed) {
            _historyFlow.value = items.toList()
        }
        removed
    }
    
    override suspend fun clearAll(): Int = mutex.withLock {
        val count = items.size
        items.clear()
        _historyFlow.value = emptyList()
        count
    }
    
    override suspend fun count(): Long = mutex.withLock {
        items.size.toLong()
    }
    
    override fun observe(): Flow<List<ScanHistoryItem>> = _historyFlow.asStateFlow()
    
    override suspend fun getByVerdict(verdict: Verdict): List<ScanHistoryItem> = mutex.withLock {
        items.filter { it.verdict == verdict }
    }
}

/**
 * Factory for creating HistoryRepository instances.
 * 
 * Expect/actual pattern for platform-specific implementations.
 */
expect class HistoryRepositoryFactory {
    fun create(): HistoryRepository
}

/**
 * Scan history manager providing high-level operations.
 * 
 * Wraps the repository with additional business logic.
 */
class ScanHistoryManager(
    private val repository: HistoryRepository
) {
    
    /**
     * Record a new scan.
     * 
     * @param url The scanned URL
     * @param score Risk score (0-100)
     * @param verdict Verdict classification
     * @param source Source of the scan
     * @return The created history item
     */
    suspend fun recordScan(
        url: String,
        score: Int,
        verdict: Verdict,
        source: ScanSource
    ): ScanHistoryItem {
        val item = ScanHistoryItem(
            id = generateId(),
            url = url.take(2048), // Bound URL length
            score = score.coerceIn(0, 100),
            verdict = verdict,
            scannedAt = Clock.System.now().toEpochMilliseconds(),
            source = source
        )
        
        repository.insert(item)
        return item
    }
    
    /**
     * Get history summary statistics.
     */
    suspend fun getStatistics(): HistoryStatistics {
        val all = repository.getAll()
        
        return HistoryStatistics(
            totalScans = all.size,
            safeCount = all.count { it.verdict == Verdict.SAFE },
            suspiciousCount = all.count { it.verdict == Verdict.SUSPICIOUS },
            maliciousCount = all.count { it.verdict == Verdict.MALICIOUS },
            averageScore = if (all.isEmpty()) 0.0 else all.map { it.score }.average()
        )
    }
    
    /**
     * Export history as JSON.
     */
    suspend fun exportAsJson(): String {
        val all = repository.getAll()
        return buildString {
            appendLine("[")
            all.forEachIndexed { index, item ->
                append("  {")
                append("\"id\":\"${item.id}\",")
                append("\"url\":\"${item.url.replace("\"", "\\\"")}\",")
                append("\"score\":${item.score},")
                append("\"verdict\":\"${item.verdict}\",")
                append("\"scannedAt\":${item.scannedAt},")
                append("\"source\":\"${item.source}\"")
                append("}")
                if (index < all.size - 1) append(",")
                appendLine()
            }
            append("]")
        }
    }
    
    private fun generateId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = (0..9999).random()
        return "scan_${timestamp}_$random"
    }
    
    /**
     * History statistics summary.
     */
    data class HistoryStatistics(
        val totalScans: Int,
        val safeCount: Int,
        val suspiciousCount: Int,
        val maliciousCount: Int,
        val averageScore: Double
    ) {
        val safePercentage: Double
            get() = if (totalScans > 0) safeCount.toDouble() / totalScans * 100 else 0.0
        
        val threatPercentage: Double
            get() = if (totalScans > 0) (suspiciousCount + maliciousCount).toDouble() / totalScans * 100 else 0.0
    }
}
