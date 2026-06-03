package ru.internet.spygame.data.local.assets

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import ru.internet.spygame.data.local.datastore.SettingsDataStore
import ru.internet.spygame.data.local.db.AppDatabase
import ru.internet.spygame.data.local.db.CategoryEntity
import ru.internet.spygame.data.local.db.WordEntity
import ru.internet.spygame.data.model.CategoriesFileDto
import ru.internet.spygame.data.model.ContentManifest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Отвечает за первоначальное и обновлённое наполнение Room из JSON-ассетов.
 *
 * Алгоритм:
 * 1. Читаем manifest.json → получаем contentVersion
 * 2. Сравниваем с сохранённой версией в DataStore
 * 3. Если версия изменилась → полностью пересеиваем базу в транзакции
 * 4. Сохраняем новую версию в DataStore
 *
 * Вызывается один раз при старте приложения (из SpyGameApplication или SplashViewModel).
 */
@Singleton
class ContentLoader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    // Единый JSON-парсер: ignoreUnknownKeys позволяет безопасно расширять JSON
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Запускает seeding если версия контента в assets новее сохранённой.
     * Безопасно вызывать при каждом запуске — лишней работы не делает.
     */
    suspend fun seedIfNeeded() {
        val manifest = loadManifest()
        val storedVersion = settingsDataStore.getContentVersion()

        if (manifest.contentVersion > storedVersion) {
            database.withTransaction {
                // Сначала удаляем слова, потом категории
                // (ForeignKey CASCADE сделал бы это автоматически,
                //  но явный вызов надёжнее при будущих рефакторингах)
                database.wordDao().deleteAll()
                database.categoryDao().deleteAll()

                for (language in manifest.supportedLanguages) {
                    val categoriesFile = loadCategories(language)
                    insertCategoriesWithWords(categoriesFile, language)
                }
            }
            settingsDataStore.setContentVersion(manifest.contentVersion)
        }
    }

    // ─────────────────────────────────────────
    // Приватные методы
    // ─────────────────────────────────────────

    /** Читает и десериализует assets/content/manifest.json */
    private fun loadManifest(): ContentManifest {
        val jsonString = context.assets
            .open("content/manifest.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(jsonString)
    }

    /** Читает и десериализует assets/content/categories_{language}.json */
    private fun loadCategories(language: String): CategoriesFileDto {
        val jsonString = context.assets
            .open("content/categories_$language.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(jsonString)
    }

    /**
     * Вставляет категории и все их слова в базу данных.
     * Должна вызываться внутри транзакции.
     */
    private suspend fun insertCategoriesWithWords(
        data: CategoriesFileDto,
        language: String
    ) {
        // Маппим DTO → Entity категорий
        val categoryEntities = data.categories.map { dto ->
            CategoryEntity(
                id = dto.id,
                name = dto.name,
                language = language
            )
        }
        database.categoryDao().insertCategories(categoryEntities)

        // Маппим DTO → Entity слов (flatMap по всем категориям)
        val wordEntities = data.categories.flatMap { dto ->
            dto.words.map { word ->
                WordEntity(
                    categoryId = dto.id,
                    word = word,
                    language = language
                )
            }
        }
        database.wordDao().insertWords(wordEntities)
    }
}