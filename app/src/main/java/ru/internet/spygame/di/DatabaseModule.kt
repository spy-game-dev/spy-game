package ru.internet.spygame.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.internet.spygame.data.local.db.AppDatabase
import ru.internet.spygame.data.local.db.CategoryDao
import ru.internet.spygame.data.local.db.WordDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Единственный экземпляр Room-базы на весь процесс приложения.
     *
     * [fallbackToDestructiveMigration] — при изменении схемы без написанной
     * миграции база пересоздаётся. Контент при этом восстанавливается через
     * ContentLoader при следующем запуске (contentVersion в ManifestJson
     * всегда актуален, а DataStore-версия сбросится вместе с базой).
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "spy_game.db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideWordDao(db: AppDatabase): WordDao = db.wordDao()
}
