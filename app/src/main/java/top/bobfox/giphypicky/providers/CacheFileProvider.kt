package top.bobfox.giphypicky.providers

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import top.bobfox.giphypicky.ui.MainActivity
import java.io.File


class CacheFileProvider : FileProvider() {
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return ParcelFileDescriptor.open(
                File("${context?.cacheDir}/${MainActivity.CACHE_SUBDIR}", uri.lastPathSegment),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
    }
}