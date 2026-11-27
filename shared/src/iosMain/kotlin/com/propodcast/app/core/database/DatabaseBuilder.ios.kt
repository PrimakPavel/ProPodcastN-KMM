package com.propodcast.app.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.propodcast.app.core.common.documentsDirectory

/** The database file in the app's Documents folder. */
fun databaseBuilder(): RoomDatabase.Builder<ProPodcastDatabase> =
    Room.databaseBuilder<ProPodcastDatabase>(name = "${documentsDirectory()}/${ProPodcastDatabase.NAME}")
