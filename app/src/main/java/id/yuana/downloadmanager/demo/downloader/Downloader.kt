package id.yuana.downloadmanager.demo.downloader

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import androidx.core.net.toUri
import java.io.File

object Downloader {

    const val URL =
        "https://raw.githubusercontent.com/andhikayuana/jokes-bapack2-api/main/storage/jokes/text.v1.json"
//    const val URL = "https://github.com/andhikayuana/jokes-bapack2-android/raw/main/app-debug.apk"

    fun createRequest(context: Context): DownloadManager.Request {
        val fileName = URL.split("/").last()
        File(context.filesDir, "downloader").let {
            if (it.exists().not()) {
                it.mkdirs()
            }
        }

//        val outputFile = File(context.filesDir, "downloader/${fileName}").let {
//            if (it.exists().not()) {
//                it.createNewFile()
//            }
//            return@let it
//        }
        val outputFile = File(context.filesDir, "downloader/${fileName}")

        return DownloadManager.Request(URL.toUri()).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setDestinationUri(outputFile.toUri())
            setDestinationInExternalFilesDir(context, "demo", fileName)
            setTitle("Downloader Demo")
            setDescription("Downloading Jokes...")
            setAllowedOverMetered(true)
        }


    }

    fun getDownloadManager(context: Context): DownloadManager =
        context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

    fun start(context: Context): Long {
        val request = createRequest(context)
        getDownloadManager(context).let {
            return it.enqueue(request)
        }
    }
}