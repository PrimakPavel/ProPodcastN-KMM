package com.propodcast.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** The database file in the app's standard databases folder. */
fun databaseBuilder(context: Context): RoomDatabase.Builder<ProPodcastDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<ProPodcastDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(ProPodcastDatabase.NAME).absolutePath,
    )
}
