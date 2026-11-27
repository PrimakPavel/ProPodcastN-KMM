package com.propodcast.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.propodcast.app.core.database.entity.DownloadedEpisodeEntity
import com.propodcast.app.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedEpisodeDao {
    /** Newest first, without downloads waiting for their Undo to expire. */
    @Query("SELECT * FROM downloaded_episodes WHERE pending_delete = 0 ORDER BY added_at DESC, rowid DESC")
    fun observeVisible(): Flow<List<DownloadedEpisodeEntity>>

    @Query("SELECT * FROM downloaded_episodes WHERE episode_id = :episodeId")
    suspend fun get(episodeId: String): DownloadedEpisodeEntity?

    @Upsert
    suspend fun upsert(episode: DownloadedEpisodeEntity)

    @Query("UPDATE downloaded_episodes SET status = :status WHERE episode_id = :episodeId")
    suspend fun updateStatus(episodeId: String, status: DownloadStatus)

    @Query("UPDATE downloaded_episodes SET status = 'DOWNLOADED', size_bytes = :sizeBytes WHERE episode_id = :episodeId")
    suspend fun markDownloaded(episodeId: String, sizeBytes: Long)

    @Query("UPDATE downloaded_episodes SET pending_delete = :pending WHERE episode_id = :episodeId")
    suspend fun setPendingDelete(episodeId: String, pending: Boolean)

    @Query("SELECT episode_id FROM downloaded_episodes WHERE pending_delete = 1")
    suspend fun pendingDeleteIds(): List<String>

    @Query("DELETE FROM downloaded_episodes WHERE episode_id = :episodeId")
    suspend fun delete(episodeId: String)
}
