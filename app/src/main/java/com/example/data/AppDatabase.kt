package com.example.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        UserProfile::class,
        RoadmapStep::class,
        Flashcard::class,
        QuizQuestion::class,
        QuizAttempt::class,
        MockInterviewSession::class,
        MockInterviewMessage::class,
        CareerCoachMessage::class,
        JobApplication::class,
        AppNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interview_pilot_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
