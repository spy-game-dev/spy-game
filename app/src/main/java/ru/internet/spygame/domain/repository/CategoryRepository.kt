package ru.internet.spygame.domain.repository

import ru.internet.spygame.domain.model.Category

/**
 * Интерфейс репозитория категорий.
 * Реализуется в data-слое ([ru.internet.spygame.data.repository.CategoryRepositoryImpl]).
 * Domain-слой зависит только от этого интерфейса — не от Room.
 */
interface CategoryRepository {

    /**
     * Возвращает случайную категорию для заданного языка.
     *
     * @param language  Код языка: "ru" или "en".
     * @param excludeId ID категории, которую нужно исключить из выборки.
     *                  Используется при «Обновить» — чтобы не повторить текущую.
     *                  null — без исключений (первый запуск).
     * @throws IllegalStateException если база пуста (seeding не был выполнен).
     */
    suspend fun getRandomCategory(language: String, excludeId: String? = null): Category

    /**
     * Возвращает все категории для заданного языка.
     * Используется в тестах и потенциальном экране настроек категорий.
     */
    suspend fun getCategories(language: String): List<Category>
}
