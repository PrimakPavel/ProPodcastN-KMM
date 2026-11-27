package com.propodcast.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.propodcast.app.core.database.entity.FavoritePodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePodcastDao {
    /** Newest first; rowid breaks ties when two rows share added_at. */
    @Query("SELECT * FROM favorite_podcasts ORDER BY added_at DESC, rowid DESC")
    fun observeAll(): Flow<List<FavoritePodcastEntity>>

    @Query("SELECT id FROM favorite_podcasts")
    fun observeIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(podcast: FavoritePodcastEntity)

    @Query("SELECT * FROM favorite_podcasts WHERE id = :id")
    suspend fun get(id: String): FavoritePodcastEntity?

    @Query("DELETE FROM favorite_podcasts WHERE id = :id")
    suspend fun delete(id: String)

    /** Deletes the row and returns it, so it can be put back as it was. */
    @Transaction
    suspend fun remove(id: String): FavoritePodcastEntity? = get(id)?.also { delete(id) }
}
