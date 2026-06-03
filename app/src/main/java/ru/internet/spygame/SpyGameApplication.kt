package ru.internet.spygame

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.internet.spygame.data.local.assets.ContentLoader
import javax.inject.Inject

/**
 * Точка входа Hilt для всего приложения.
 * Обязательно указать в AndroidManifest.xml:
 *   android:name=".SpyGameApplication"
 */
@HiltAndroidApp
class SpyGameApplication : Application() {

    /**
     * ContentLoader внедряется Hilt-ом после того, как DI-граф собран.
     * Вызов [ContentLoader.seedIfNeeded] происходит в [onCreate] на
     * фоновом потоке — UI не блокируется.
     */
    @Inject
    lateinit var contentLoader: ContentLoader

    /**
     * Application-уровня CoroutineScope с SupervisorJob.
     * SupervisorJob: падение одной дочерней корутины не отменяет остальные.
     * Живёт столько же, сколько процесс приложения — утечки нет.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Запускаем seeding сразу при старте приложения.
        // Если база уже актуальна (contentVersion совпадает) — операция мгновенная.
        applicationScope.launch {
            runCatching {
                contentLoader.seedIfNeeded()
            }.onFailure { e ->
                // В production здесь можно добавить логирование через Timber:
                // Timber.e(e, "ContentLoader: ошибка seeding")
                e.printStackTrace()
            }
        }
    }
}
