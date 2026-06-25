package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticDao {
    @Query("SELECT * FROM diagnostic_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<DiagnosticSession>>

    @Query("SELECT * FROM diagnostic_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: Int): Flow<DiagnosticSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DiagnosticSession): Long

    @Update
    suspend fun updateSession(session: DiagnosticSession)

    @Query("DELETE FROM diagnostic_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)
}

@Database(entities = [DiagnosticSession::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosticDao(): DiagnosticDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_diagnostic_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
