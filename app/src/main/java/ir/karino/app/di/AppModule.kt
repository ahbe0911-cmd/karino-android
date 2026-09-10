package ir.karino.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.karino.app.data.local.CategoryDao
import ir.karino.app.data.local.KarinoDatabase
import ir.karino.app.data.local.TaskDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KarinoDatabase =
        Room.databaseBuilder(
            context,
            KarinoDatabase::class.java,
            "karino.db",
        ).build()

    @Provides
    fun provideTaskDao(database: KarinoDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideCategoryDao(database: KarinoDatabase): CategoryDao = database.categoryDao()
}
