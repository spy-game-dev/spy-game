package ru.internet.spygame.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.internet.spygame.data.repository.CategoryRepositoryImpl
import ru.internet.spygame.data.repository.SettingsRepositoryImpl
import ru.internet.spygame.domain.repository.CategoryRepository
import ru.internet.spygame.domain.repository.SettingsRepository
import javax.inject.Singleton

/**
 * Привязывает domain-интерфейсы к их data-реализациям.
 *
 * Используем @Binds вместо @Provides: Hilt не создаёт лишний wrapper-объект,
 * а просто сообщает DI-графу, что при запросе интерфейса надо отдать impl.
 *
 * Абстрактный класс обязателен для @Binds.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
