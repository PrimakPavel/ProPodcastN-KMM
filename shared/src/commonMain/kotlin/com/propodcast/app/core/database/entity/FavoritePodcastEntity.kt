package com.propodcast.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propodcast.app.core.model.Podcast

@Entity(tableName = "favorite_podcasts")
data class FavoritePodcastEntity(
    @PrimaryKey val id: String,
    val title: String,
    val publisher: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    val description: String,
    @ColumnInfo(name = "total_episodes") val totalEpisodes: Int,
    @ColumnInfo(name = "listennotes_url") val listennotesUrl: String?,
    @ColumnInfo(name = "latest_pub_date_ms") val latestPubDateMs: Long?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

fun FavoritePodcastEntity.toModel() = Podcast(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
)

fun Podcast.toFavoriteEntity(addedAt: Long) = FavoritePodcastEntity(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
    addedAt = addedAt,
)
