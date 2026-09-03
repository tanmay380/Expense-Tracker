package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.CategoryRepository
import com.example.expensetracker.data.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "paisa_database"
        ).enableMultiInstanceInvalidation()
            .build()
    }

    @Singleton
    @Provides
    fun provideTransactionRepository(
        database: AppDatabase
    ): TransactionRepository {
        return TransactionRepository(
            database.transactionDao(),
            database.accountDao()
        )
    }

    @Singleton
    @Provides
    fun provideCategoryRepository(
        database: AppDatabase
    ): CategoryRepository {
        return CategoryRepository(database.categoryDao())
    }
}
