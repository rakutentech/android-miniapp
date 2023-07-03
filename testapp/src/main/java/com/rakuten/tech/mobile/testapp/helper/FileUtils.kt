package com.rakuten.tech.mobile.testapp.helper

import android.content.Context
import com.rakuten.tech.mobile.miniapp.analytics.MAAnalyticsInfo
import java.io.File

class FileUtils {
    companion object{
        private val ANALYTIC_LOG_FILE_NAME = "logs.txt"
        private val ANALYTIC_LOG_FOLDER_NAME = "maAnalytics"

        fun saveMiniAppAnalyticLogs(context: Context, maAnalyticsInfo: MAAnalyticsInfo) {
            var logFileDirectory = File(context.filesDir, ANALYTIC_LOG_FOLDER_NAME)
            var logFile = File(logFileDirectory, ANALYTIC_LOG_FILE_NAME)

            if (!logFileDirectory.exists())
                logFileDirectory.mkdirs()
            if (!logFile.exists())
                logFile = File(logFileDirectory, ANALYTIC_LOG_FILE_NAME)

            logFile.appendText(maAnalyticsInfo.toString()+"\n", Charsets.UTF_8)
        }

        fun getMiniAppAnalyticLogs(context: Context): String {
            val logFile =
                File(context.filesDir, "$ANALYTIC_LOG_FOLDER_NAME/$ANALYTIC_LOG_FILE_NAME")
            // read the file and return the file content as a String
            return if (logFile.exists())
                logFile.readText()
            else ""
        }
    }
}
