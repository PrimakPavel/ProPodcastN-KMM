package com.propodcast.app.core.data

import android.content.Context

/** Same place as `Context.preferencesDataStoreFile(name)`: files/datastore/<name>.preferences_pb. */
fun dataStorePath(context: Context, name: String): String =
    context.applicationContext.filesDir.resolve("datastore/${DataStoreFiles.fileName(name)}").absolutePath
