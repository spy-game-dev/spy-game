package ru.internet.spygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import ru.internet.spygame.presentation.navigation.NavGraph
import ru.internet.spygame.presentation.theme.SpyGameTheme

/**
 * Единственная Activity приложения.
 *
 * Обязанности:
 * 1. Установить Splash Screen до рендера UI.
 * 2. Включить edge-to-edge отображение (контент под статус/навбаром).
 * 3. Отдать управление Compose — весь UI живёт в [NavGraph].
 *
 * Не хранит никакого состояния — ViewModel-и живут в [NavGraph]-дочерних
 * Composable-функциях через hiltViewModel().
 *
 * В AndroidManifest.xml обязательно:
 *   android:name=".SpyGameApplication"   — на <application>
 *   android:theme="@style/Theme.SpyGame.Splash"  — на <activity>
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen ДОЛЖЕН быть вызван до super.onCreate(),
        // иначе система не успевает перехватить window background.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Контент рисуется под системными барами; Composable-ы сами управляют
        // insets через WindowInsets.systemBars / Modifier.windowInsetsPadding.
        enableEdgeToEdge()

        setContent {
            SpyGameTheme {
                NavGraph()
            }
        }
    }
}
