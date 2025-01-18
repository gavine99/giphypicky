package top.bobfox.giphypicky.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.Intent.ACTION_PICK
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.createChooser
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.core.models.enums.RatingType
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.pagination.GPHContent
import com.giphy.sdk.ui.views.GPHGridCallback
import com.giphy.sdk.ui.views.GPHSearchGridCallback
import com.giphy.sdk.ui.views.GifView
import okhttp3.OkHttpClient
import okhttp3.Request
import top.bobfox.giphypicky.R
import top.bobfox.giphypicky.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


open class MainActivity : AppCompatActivity() {
    companion object {
        const val CACHE_SUBDIR = "giphy"

        const val API_KEY_FILE = "giphy-api.key"

        const val FILE_NAME_KEY = "filename"
        const val FILE_TITLE_KEY = "title"

        const val HANDLER_DELETE_FILE = 0
        const val HANDLER_FILE_READY = 1
    }

    private lateinit var binding: ActivityMainBinding
    private var resultHandler: Handler? = null
    private var launchedByThirdParty = false
    private var apiKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get api key
        try {
            val file = File(cacheDir, API_KEY_FILE)
            apiKey = file.readText()
        } catch(e :Exception) {
        }

        Giphy.configure(this, apiKey ?: "")

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (apiKey.isNullOrEmpty())
            binding.searchEditText.hint = getString(R.string.enter_api_key_hint)

        binding.enterApiKey.setOnClickListener {
            val apiKeyText = EditText(this)
            apiKeyText.setText(apiKey)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.giphy_apikey_dialog_title))
                .setMessage(R.string.giphy_apikey_dialog_message)
                .setView(apiKeyText)
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> null }
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    if (apiKeyText.text.toString().isNullOrEmpty())
                        null
                    try {
                        apiKey = apiKeyText.text.toString()
                        File(cacheDir, API_KEY_FILE).writeText(apiKey!!)
                        // restart activity to (re-)configure giphy sdk
                        finish()
                        startActivity(intent)
                    } catch (e: Exception) {
                    }
                }
                .create()
                .show()
        }

        // focus to search box
        binding.searchEditText.requestFocus()

        // default to a 'cancelled' (no file return) result
        setResult(RESULT_CANCELED, null)

        // did a third party app launch this activity for file content/pick
        when (this.intent.action) {
            ACTION_GET_CONTENT, ACTION_PICK -> launchedByThirdParty = true
        }

        // create handler to get message from background thread and return result from main thread
        resultHandler = Handler(Looper.getMainLooper()) {
            val filename = it.data.getString(FILE_NAME_KEY)
            if (filename !== null)
                true

            val file = File(this@MainActivity!!.cacheDir, filename)

            when (it.what) {
                // if error with fetching file, delete file to clean it up
                HANDLER_DELETE_FILE -> {
                    failProofDeleteFile(file.absolutePath)

                    this@MainActivity.binding.downloading.visibility = View.GONE
                }
                // file downloaded ok
                HANDLER_FILE_READY -> {
                    val title = it.data.getString(FILE_TITLE_KEY) ?: ""

                    val fileUri =
                        FileProvider.getUriForFile(
                            this,
                            "$packageName.fileprovider",
                            file
                        )

                    // third party app requested file, return in a result intent
                    if (launchedByThirdParty) {
                        setResult(
                            RESULT_OK,
                            Intent("$packageName.ACTION_RETURN_FILE").apply {
                                type = contentResolver.getType(fileUri)
                                putExtra(EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                clipData = ClipData(title, arrayOf(type), ClipData.Item(fileUri))
                            }
                        )
                    }
                    // else, launched by user, pop up OS share chooser
                    else {
                        grantUriPermission("android", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        startActivity(createChooser(Intent(
                            Intent.ACTION_SEND).apply {
                                type = contentResolver.getType(fileUri)
                                putExtra(EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            fileUri.lastPathSegment)
                        )
                    }

                    finish()
                }
            }

            true    // always return true for no more processing required
        }

        // if handler can't be created, abort now
        if (resultHandler === null) {
            finish()
            return
        }

        // search triggered by user
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    // hide soft keyboard
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(view.windowToken, 0)

                    // get gifs
                    binding.gifsGridView.content = GPHContent.searchQuery(
                        binding.searchEditText.text.toString(),
                        MediaType.gif, RatingType.nsfw
                    )

                    true
                }
                else -> false
            }
        }

        binding.gifsGridView.searchCallback = object: GPHSearchGridCallback {
            override fun didTapUsername(username: String) { /* nothing */ }
            override fun didLongPressCell(cell: GifView) {  /* nothing */ }
            override fun didScroll(dx: Int, dy: Int) {  /* nothing */ }
        }

        // user has selected a file
        binding.gifsGridView.callback = object: GPHGridCallback {
            override fun contentDidUpdate(resultCount: Int) { /* nothing */ }

            override fun didSelectMedia(media: Media) {
                this@MainActivity.binding.downloading.visibility = View.VISIBLE

                Thread {
                    // send message to main thread with filename of downloaded gif
                    val handlerMessage = Message().apply {
                        what = HANDLER_DELETE_FILE        // assume to send error until known good
                        data = Bundle()
                    }

                    try {
                        if (media.images.original?.gifUrl.isNullOrEmpty())
                            return@Thread

                        // open file from giphy site
                        val inputStream = OkHttpClient
                            .Builder()
                            .readTimeout(15, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .build()
                            .newCall(Request.Builder().get().url(media.images.original?.gifUrl.orEmpty()).build())
                            .execute().body?.byteStream()

                        if (inputStream === null)
                            return@Thread

                        // delete any cached downloaded files left over for any reason
                        File("${cacheDir}/${CACHE_SUBDIR}").deleteRecursively()

                        // make cache dir for downloaded files if it doesn't exist
                        val dir = "${this@MainActivity!!.cacheDir}/${CACHE_SUBDIR}"
                        File(dir).mkdirs()

                        val file = File(
                            "${this@MainActivity!!.cacheDir}/${CACHE_SUBDIR}",
                            "${media.title}.${media.type?.name}"
                        )

                        handlerMessage.data.putString(FILE_TITLE_KEY, media.title)
                        handlerMessage.data.putString(FILE_NAME_KEY, file.absolutePath)

                        val outputStream = FileOutputStream(file)
                        if (outputStream === null)
                            return@Thread

                        inputStream.copyTo(outputStream)

                        inputStream.close()
                        outputStream.close()

                        handlerMessage.what = HANDLER_FILE_READY
                    }
                    finally {
                        resultHandler?.sendMessage(handlerMessage)
                    }
                }.start()
            }
        }
    }

    private fun failProofDeleteFile(file: String) {
        try {
            deleteFile(file)
        } catch (e: Exception) {
        }
    }
}
