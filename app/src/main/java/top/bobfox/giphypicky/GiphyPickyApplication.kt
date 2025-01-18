package top.bobfox.giphypicky

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate


class GiphyPickyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
