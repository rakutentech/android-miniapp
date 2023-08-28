package com.rakuten.tech.mobile.testapp.helper

import android.content.Context
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.analytics.MAAnalyticsInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileUtils {
    companion object{
        private val ANALYTIC_LOG_FILE_NAME = "logs.txt"
        private val ANALYTIC_LOG_FOLDER_NAME = "maAnalytics"

        fun miniAppOpenLogs(context: Context, miniAppId: String) {
            checkAndCreateLogFile(context).appendText(
                "MiniApp Launched-$miniAppId-${getCurrentTimeStamp()} \n",
                Charsets.UTF_8
            )
        }

        fun miniAppCloseLogs(context: Context, miniAppId: String) {
            checkAndCreateLogFile(context).appendText(
                "MiniAp Closed-$miniAppId-${getCurrentTimeStamp()} \n",
                Charsets.UTF_8
            )
        }

        fun saveMiniAppAnalyticLogs(context: Context, maAnalyticsInfo: MAAnalyticsInfo) {
            checkAndCreateLogFile(context).appendText(
                Gson().toJson(maAnalyticsInfo) + "\n",
                Charsets.UTF_8
            )
        }

        fun deleteLogs(context: Context) {
            val logFile =
                File(context.filesDir, "$ANALYTIC_LOG_FOLDER_NAME/$ANALYTIC_LOG_FILE_NAME")
            if (logFile.exists())
                logFile.delete()
        }

        fun getAnalyticLogs(context: Context): String {
            val logFile =
                File(context.filesDir, "$ANALYTIC_LOG_FOLDER_NAME/$ANALYTIC_LOG_FILE_NAME")
            // read the file and return the file content as a String
            return if (logFile.exists())
                logFile.readText()
            else ""
        }

        private fun checkAndCreateLogFile(context: Context): File{
            var logFileDirectory = File(context.filesDir, ANALYTIC_LOG_FOLDER_NAME)
            var logFile = File(logFileDirectory, ANALYTIC_LOG_FILE_NAME)

            if (!logFileDirectory.exists())
                logFileDirectory.mkdirs()
            if (!logFile.exists())
                logFile = File(logFileDirectory, ANALYTIC_LOG_FILE_NAME)
            return logFile
        }

        private fun getCurrentTimeStamp(): String = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
    }
}
