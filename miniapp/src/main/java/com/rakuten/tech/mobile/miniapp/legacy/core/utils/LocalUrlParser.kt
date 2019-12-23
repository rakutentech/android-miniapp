package com.rakuten.tech.mobile.miniapp.legacy.core.utils

import java.io.File

/**
 * URL parser. Manifest URL contains information such as MiniApp ID, Version ID.
 */
class LocalUrlParser {
    // TODO: Add URL validator for each method.

    /**
     * Returns the appId from the manifest URL. AppId is the string after "miniapp" in the URL.
     * Returns empty String when not found.
     */
    fun getAppId(manifestUrl: String): String {
        val splitStrings: List<String> = manifestUrl.split(File.separator)
        return splitStrings[splitStrings.indexOf(MINI_APP_ID_KEY).plus(1)]
    }

    /**
     * Returns the versionId from the file URL. VersionId is the string after "version" in the URL.
     * Returns empty String when not found.
     */
    fun getVersionId(fileUrl: String): String {
        val splitStrings: List<String> = fileUrl.split(File.separator)
        return splitStrings[splitStrings.indexOf(VERSION_KEY).plus(1)]
    }

    /**
     * Returns the path between versionId to file name.
     * For example: In URL "http://version/123/foo/bar/foo.html", "/foo/bar/" will be returned.
     * Returns empty String when not found.
     */
    fun getFilePath(fileUrl: String): String {
        val splitStrings: List<String> = fileUrl.split(File.separator)
        val versionStringIndex = splitStrings.indexOf(VERSION_KEY)
        // If versionStringIndex = -1, version string was not found, then this URL is invalid.
        if (versionStringIndex < 0) {
            return ""
        }

        // The second string after "version" is the beginning of path.
        val startIndex = versionStringIndex + 2
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
    }
}
