package com.skul9x.ytsummary.data

import android.content.Context
import androidx.room.*

import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase

/**
 * Lớp khởi tạo Room Database gộp lại toàn bộ project.
 */
@Database(entities = [SummaryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun summaryDao(): SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Passphrase cho Database: Trong thực tế nên lấy từ Android Keystore
        private val PASSPHRASE = SQLiteDatabase.getBytes("yts-secure-vault-2026".toCharArray())

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Khởi tạo SQLCipher library
                SQLiteDatabase.loadLibs(context)
                
                val factory = SupportFactory(PASSPHRASE)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yts_database"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
