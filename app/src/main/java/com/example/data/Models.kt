package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val hasCompletedOnboarding: Boolean = false,
    val targetCareer: String = "",
    val experienceLevel: String = "",
    val targetCompanies: String = "", // Comma-separated
    val prepDurationDays: Int = 30,
    val currentStreak: Int = 1,
    val lastGoalUpdateDay: Long = 0,
    val xpScore: Int = 0,
    val resumeContent: String = "",
    val resumeScore: Int = 0,
    val resumeFeedback: String = ""
)

@Entity(tableName = "roadmap_step")
data class RoadmapStep(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phase: String, // Foundation, Core, etc.
    val title: String,
    val description: String,
    val learningResources: String = "",
    val summary: String = "",
    val notes: String = "",
    val status: String = "NOT_STARTED", // NOT_STARTED, LEARNING, PRACTICING, MASTERED
    val orderIndex: Int = 0
)

@Entity(tableName = "flashcard")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val question: String,
    val answer: String,
    val type: String = "Concept", // Concept, Definition, Scenario
    val isBookmarked: Boolean = false,
    val masteryLevel: Int = 0 // 0 = New, 1 = Learning, 2 = Mastered
)

@Entity(tableName = "quiz_question")
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0, 1, 2, 3
    val explanation: String
)

@Entity(tableName = "quiz_attempt")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_interview_session")
data class MockInterviewSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val type: String, // Technical, Behavioral, Managerial, Final
    val isCompleted: Boolean = false,
    val overallScore: Int = 0,
    val feedback: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_interview_message")
data class MockInterviewMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "career_coach_message")
data class CareerCoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_application")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company: String,
    val role: String,
    val status: String = "Applied", // Applied, Interview Scheduled, Offer, Rejected
    val dateString: String = "",
    val notes: String = ""
)

@Entity(tableName = "app_notification")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
