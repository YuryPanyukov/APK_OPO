package code_sys.apkopo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import code_sys.apkopo.di.AppContainer
import code_sys.apkopo.ui.AppNav
import code_sys.apkopo.ui.theme.APKOPOTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = AppContainer(applicationContext)
        setContent {
            APKOPOTheme {
                AppNav(container = container)
            }
        }
    }
}
