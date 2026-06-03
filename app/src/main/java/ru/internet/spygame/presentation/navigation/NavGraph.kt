package ru.internet.spygame.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.internet.spygame.presentation.game.GameScreen
import ru.internet.spygame.presentation.settings.SettingsScreen

/**
 * Корневой NavHost приложения.
 *
 * @param navController  Контроллер навигации. По умолчанию создаётся через
 *                       [rememberNavController] — удобно для Preview и тестов.
 * @param startDestination Стартовый маршрут. По умолчанию [Screen.Game].
 *                         Можно передать [Screen.Settings] в тестах.
 * @param modifier       Передаётся в [NavHost] — управляет размером контейнера.
 */
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Game
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Переход Game → Settings: Settings выезжает снизу
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(durationMillis = 350)
            )
        },
        // Переход Settings → Game (navigateUp): Settings уходит вниз
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(durationMillis = 300)
            )
        },
        // popEnter / popExit используют те же анимации, но в обратную сторону
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(durationMillis = 300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        // ─────────────────────────────────────────
        // Игровой экран — стартовый
        // ─────────────────────────────────────────
        composable<Screen.Game> {
            GameScreen(
                onOpenSettings = {
                    // Открываем Settings поверх Game.
                    // launchSingleTop = true: не создаём дубликат если Settings уже открыт.
                    navController.navigate(Screen.Settings) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ─────────────────────────────────────────
        // Экран настроек — открывается поверх Game
        // ─────────────────────────────────────────
        composable<Screen.Settings> {
            SettingsScreen(
                onBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
