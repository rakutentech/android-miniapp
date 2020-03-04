package com.rakuten.tech.mobile.miniapp.storage

import java.io.File

private const val KEY_MAP_PUB = "map-published"
private const val INDEX_PATH = 3

internal class UrlToFileInfoParser {

    /**
     * Returns the path between versionId to file name e.g. for a given source,
     * "https://host.os.net/map-published/min-872f9172-804f-44e2-addd-ed612170dac9/
     * >>ver-6181004c-a6aa-4eda-b145-a5ff73fc4ad0/foo/bar/asset-manifest.json",
     * "/foo/bar/" will be returned.
     * Returns empty String when not found.
     */
    fun getFilePath(fileUrl: String): String = fileUrl.split(File.separator).run {
        val versionIndex = indexOf(KEY_MAP_PUB)
        return when {
            // If versionIndex = -1, map-published was not found, then this source is invalid.
            versionIndex < 0 -> ""
            else -> {
                var path = "/"
                // The third string after "map-published" is the beginning of path.
                subList(versionIndex + INDEX_PATH, lastIndex).forEach { path += "$it/" }
                path
            }
        }
    }

    /**
     * Returns the last element of the split string. Returns empty String when not found.
     */
    fun getFileName(fileUrl: String): String = fileUrl.split(File.separator).run {
        return when {
            "." in last() -> last()
            else -> ""
        }
    }
}
