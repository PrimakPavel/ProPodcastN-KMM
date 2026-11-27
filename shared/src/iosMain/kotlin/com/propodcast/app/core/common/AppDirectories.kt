package com.propodcast.app.core.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/** The app's Documents folder: persistent and backed up, like Android's internal files. */
internal fun documentsDirectory(): String = directory(NSDocumentDirectory, "Documents")

/**
 * A persistent folder for files the app can download again (episodes): Application Support/[name].
 * The system does not purge it (unlike Caches or tmp); it is excluded from iCloud / device backups so audio
 * does not use the user's backup storage.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun persistentNonBackedUpDirectory(name: String): String {
    val path = "${directory(NSApplicationSupportDirectory, "Application Support")}/$name"
    val fileManager = NSFileManager.defaultManager
    fileManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
    NSURL.fileURLWithPath(path, isDirectory = true).setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    return path
}

@OptIn(ExperimentalForeignApi::class)
private fun directory(type: NSSearchPathDirectory, name: String): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = type,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "No $name directory" }
}
