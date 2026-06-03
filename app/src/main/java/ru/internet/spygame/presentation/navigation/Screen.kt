package ru.internet.spygame.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Навигационные маршруты приложения.
 *
 * Используем type-safe navigation (Navigation Compose 2.8+):
 * вместо строковых route-ов — @Serializable data object-ы.
 * kotlinx.serialization генерирует уникальный route-идентификатор
 * из fully-qualified class name → нет риска опечатки в строке.
 *
 * Граф навигации:
 *   [Game] ←→ [Settings]
 *
 * Стартовый экран — [Game] (пользователь сразу попадает в игру,
 * настройки доступны через иконку шестерёнки на экране игры).
 */
@Serializable
sealed class Screen {

    /**
     * Основной игровой экран — стек карточек, таймер, кнопка обновления.
     */
    @Serializable
    data object Game : Screen()

    /**
     * Экран настроек — число игроков, язык.
     * Открывается поверх [Game] и возвращает управление через navigateUp().
     */
    @Serializable
    data object Settings : Screen()
}
