package com.rakuten.tech.mobile.miniapp.storage

import java.io.File

/**
 * URL parser. Manifest URL contains information such as MiniApp ID, Version ID.
 */
class UrlToFileInfoParser {
    // TODO: Add URL validator for each method.

    /**
     * Returns the appId from the legacy manifest URL. AppId is the string after "miniapp" in the URL.
     * Returns empty String when not found.
     */
    fun getAppIdForLegacy(manifestUrl: String): String {
        val splitStrings: List<String> = manifestUrl.split(File.separator)
        return splitStrings[splitStrings.indexOf(MINI_APP_ID_KEY).plus(1)]
    }

    /**
     * Returns the versionId from the legacy file URL.
     * VersionId is the string after "version" in the URL.
     * Returns empty String when not found.
     */
    fun getVersionIdForLegacy(fileUrl: String): String {
        val splitStrings: List<String> = fileUrl.split(File.separator)
        return splitStrings[splitStrings.indexOf(VERSION_KEY).plus(1)]
    }

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

    companion object {
        private const val MINI_APP_ID_KEY = "miniapp"
        private const val VERSION_KEY = "version"
        private const val KEY_MAP_PUB = "map-published"
    }
}
