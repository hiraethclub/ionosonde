package club.hiraeth.ionosonde.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import club.hiraeth.ionosonde.data.preferences.UserPreferences
import club.hiraeth.ionosonde.data.repository.IonosondeRepository
import club.hiraeth.ionosonde.ui.navigation.IonosondeNavHost
import club.hiraeth.ionosonde.ui.theme.IonosondeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Trigger initial data load
        lifecycleScope.launch {
            try {
                IonosondeRepository.getInstance(applicationContext).refreshAll()
            } catch (_: Exception) {
                // Will retry via WorkManager
            }
        }

        val prefs = UserPreferences.getInstance(applicationContext)

        setContent {
            val settings by prefs.observeSettings().collectAsState(
                initial = club.hiraeth.ionosonde.data.preferences.AppSettings()
            )

            IonosondeTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IonosondeNavHost()
                }
            }
        }
    }
}
