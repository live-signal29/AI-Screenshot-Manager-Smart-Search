package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryStat(
    val category: String,
    val count: Int,
    val totalBytes: Long
)

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots ORDER BY dateTaken DESC")
    fun getAllFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY dateTaken DESC")
    suspend fun getAllList(): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<ScreenshotEntity?>

    @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScreenshotEntity?

    @Query("SELECT * FROM screenshots WHERE isProcessed = 0 ORDER BY dateTaken DESC LIMIT :limit")
    suspend fun getUnprocessed(limit: Int = 50): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots WHERE isFavorite = 1 ORDER BY dateTaken DESC")
    fun getFavoritesFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE category = :category ORDER BY dateTaken DESC")
    fun getByCategoryFlow(category: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE isDuplicate = 1 ORDER BY duplicateGroupId, dateTaken DESC")
    fun getDuplicatesFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE duplicateGroupId = :groupId ORDER BY dateTaken DESC")
    suspend fun getByDuplicateGroup(groupId: String): List<ScreenshotEntity>

    @Query("""
        SELECT * FROM screenshots 
        WHERE ocrText LIKE '%' || :query || '%' 
           OR displayName LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR extractedPrices LIKE '%' || :query || '%'
           OR extractedDates LIKE '%' || :query || '%'
           OR extractedEmails LIKE '%' || :query || '%'
           OR extractedPhones LIKE '%' || :query || '%'
           OR extractedUrls LIKE '%' || :query || '%'
           OR extractedOrderNumbers LIKE '%' || :query || '%'
        ORDER BY dateTaken DESC
    """)
    fun searchScreenshots(query: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT category, COUNT(*) as count, SUM(size) as totalBytes FROM screenshots GROUP BY category")
    fun getCategoryStats(): Flow<List<CategoryStat>>

    @Query("SELECT COUNT(*) FROM screenshots")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(size), 0) FROM screenshots")
    fun getTotalStorageFlow(): Flow<Long>

    @Query("SELECT COUNT(*) FROM screenshots WHERE isDuplicate = 1")
    fun getDuplicateCountFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(size), 0) FROM screenshots WHERE isDuplicate = 1")
    fun getDuplicateStorageFlow(): Flow<Long>

    @Query("SELECT * FROM screenshots WHERE size > :minBytes ORDER BY size DESC")
    fun getLargeScreenshotsFlow(minBytes: Long = 3 * 1024 * 1024L): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE dateTaken < :maxDateTaken ORDER BY dateTaken ASC")
    fun getOldScreenshotsFlow(maxDateTaken: Long): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE isPotentiallyRemovable = 1 OR isDuplicate = 1 ORDER BY dateTaken DESC")
    fun getRemovableScreenshotsFlow(): Flow<List<ScreenshotEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(screenshots: List<ScreenshotEntity>): List<Long>

    @Update
    suspend fun update(screenshot: ScreenshotEntity)

    @Update
    suspend fun updateAll(screenshots: List<ScreenshotEntity>)

    @Query("UPDATE screenshots SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE screenshots SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)

    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM screenshots WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM screenshots")
    suspend fun clearAll()
}
