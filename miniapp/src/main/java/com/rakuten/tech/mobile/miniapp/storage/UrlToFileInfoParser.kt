package com.rakuten.tech.mobile.miniapp.storage

import java.io.File

private const val KEY_MAP_PUB = "map-published"

/**
 * URL parser. Manifest URL contains information such as MiniApp ID, Version ID.
 */
class UrlToFileInfoParser {

    /**
     * Returns the path between versionId to file name.
     * e.g. in
     * "https://host.os.net/map-published/min-872f9172-804f-44e2-addd-ed612170dac9/
     * >>ver-6181004c-a6aa-4eda-b145-a5ff73fc4ad0/foo/bar/asset-manifest.json",
     * "/foo/bar/" will be returned.
     * Returns empty String when not found.
     */
    @Suppress("MagicNumber")
    fun getFilePath(fileUrl: String): String {
        val splitStrings: List<String> = fileUrl.split(File.separator)
        val versionStringIndex = splitStrings.indexOf(KEY_MAP_PUB)
        // If versionStringIndex = -1, version string was not found, then this URL is invalid.
        if (versionStringIndex < 0) {
            return ""
        }

        // The second string after "version" is the beginning of path.
        val startIndex = versionStringIndex + 3
        val lastIndex = splitStrings.lastIndex

        var path = "/"
        for (str in splitStrings.subList(startIndex, lastIndex)) {
            path += "$str/"
        }

        return path
    }

    /**
     * Returns the last element of the split string. Returns empty String when not found.
     */
    fun getFileName(fileUrl: String): String {
        val splitStrings: List<String> = fileUrl.split(File.separator)
        val fileName = splitStrings.last()
        if (fileName.contains(".")) {
            return fileName
        } else {
            return ""
        }
    }
}
