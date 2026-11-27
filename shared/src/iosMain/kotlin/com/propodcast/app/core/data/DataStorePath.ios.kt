package com.propodcast.app.core.data

import com.propodcast.app.core.common.documentsDirectory

/** Documents/<name>.preferences_pb. */
fun dataStorePath(name: String): String = "${documentsDirectory()}/${DataStoreFiles.fileName(name)}"
