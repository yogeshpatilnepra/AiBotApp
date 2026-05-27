package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // Roadmap Steps
    @Query("SELECT * FROM roadmap_step ORDER BY orderIndex ASC")
    fun getRoadmapSteps(): Flow<List<RoadmapStep>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapSteps(steps: List<RoadmapStep>)

    @Update
    suspend fun updateRoadmapStep(step: RoadmapStep)

    @Query("DELETE FROM roadmap_step")
    suspend fun clearRoadmap()

    // Flashcards
    @Query("SELECT * FROM flashcard")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Update
    suspend fun updateFlashcard(card: Flashcard)

    // Quiz Questions & Attempts
    @Query("SELECT * FROM quiz_question WHERE topic = :topic")
    suspend fun getQuestionsForTopic(topic: String): List<QuizQuestion>

    @Query("SELECT * FROM quiz_question")
    suspend fun getAllQuestions(): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestion>)

    @Query("SELECT * FROM quiz_attempt ORDER BY timestamp DESC")
    fun getQuizAttempts(): Flow<List<QuizAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttempt)

    // Mock Interviews
    @Query("SELECT * FROM mock_interview_session ORDER BY timestamp DESC")
    fun getInterviewSessions(): Flow<List<MockInterviewSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewSession(session: MockInterviewSession): Long

    @Update
    suspend fun updateInterviewSession(session: MockInterviewSession)

    @Query("SELECT * FROM mock_interview_message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getInterviewMessages(sessionId: Int): Flow<List<MockInterviewMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewMessage(message: MockInterviewMessage)

    // Career Coach
    @Query("SELECT * FROM career_coach_message ORDER BY timestamp ASC")
    fun getCoachMessages(): Flow<List<CareerCoachMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoachMessage(message: CareerCoachMessage)

    @Query("DELETE FROM career_coach_message")
    suspend fun clearCoachChat()

    // Applications Tracker
    @Query("SELECT * FROM job_application ORDER BY id DESC")
    fun getApplications(): Flow<List<JobApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: JobApplication)

    @Delete
    suspend fun deleteApplication(app: JobApplication)

    // Notifications
    @Query("SELECT * FROM app_notification ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE app_notification SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)
}
