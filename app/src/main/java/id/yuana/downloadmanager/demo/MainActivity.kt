package id.yuana.downloadmanager.demo

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import id.yuana.downloadmanager.demo.data.local.Cache
import id.yuana.downloadmanager.demo.databinding.ActivityMainBinding
import id.yuana.downloadmanager.demo.downloader.Downloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        val needPermissions = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        const val PERMISSION_REQUEST_CODE = 11
    }

    private lateinit var binding: ActivityMainBinding
    private val cache: Cache by lazy { Cache(this) }
    private val onDownloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("YUANA", "onDownloadReceiver")
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                context?.let {
                    Downloader.getDownloadManager(it).let { dm ->
                        dm.query(DownloadManager.Query().apply {
                            setFilterById(downloadId)
                        })
                    }.run {
                        if (moveToFirst()) {
                            val colStatus =
                                getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            if (DownloadManager.STATUS_SUCCESSFUL == colStatus) {
                                val fileUri =
                                    getString(getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))

                                val downloadedFile = Uri.parse(fileUri).toFile()

                                Log.d("YUANA", "SUCCESS: ${downloadedFile.path}")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requestPermissions()

        with(binding) {
            btnDownload.setOnClickListener { onClickedDownload() }
            btnMoveToInternal.setOnClickListener { onClickedMoveToInternal() }
            btnShow.setOnClickListener { onClickedShow() }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(onDownloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(onDownloadReceiver)
    }

    private fun requestPermissions() {
        needPermissions
            .filter { permission -> hasPermission(permission).not() }
            .let { permissions ->
                if (permissions.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        this,
                        permissions.toTypedArray(),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            grantResults.forEachIndexed { index, item ->
                if (item == PackageManager.PERMISSION_GRANTED) {
                    Log.d("YUANA", "Permission: ${permissions[index]} Granted!")
                }
            }
        }
    }

    private fun onClickedDownload() {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadId = Downloader.start(this@MainActivity)
            cache.write(downloadId)
        }
    }

    private fun onClickedMoveToInternal() {
        val downloadId = runBlocking { cache.read().first() }
        val cursor = Downloader.getDownloadManager(this)
            .query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            val colIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val result = cursor.getString(colIdx)

            Log.d("YUANA", "PATH: ${result}")
            val downloadedFile = Uri.parse(result).toFile()

            downloadedFile.copyTo(File(filesDir, "downloader/demo/text.v1.json"), true)
            downloadedFile.delete()
            Downloader.getDownloadManager(this).remove(downloadId)

        } else {
            Toast.makeText(this, "File doesn't exists!", Toast.LENGTH_LONG).show()
        }
    }

    private fun onClickedShow() {
        val file = File(filesDir, "downloader/demo/text.v1.json")
        if (file.exists().not()) {
            Toast.makeText(this, "File doesn't exists!", Toast.LENGTH_LONG).show()
        } else {
            val jokes = JSONArray(file.readText())
            var arrayJokes = mutableListOf<String>()
            for (i in 0 until jokes.length()) {
                Log.d("YUANA", "jokes[$i]: ${jokes.getString(i)}")
                arrayJokes.add(jokes.getString(i))
            }
            startActivity(ShowActivity.createIntent(this, arrayJokes.toTypedArray()))
        }
    }
}