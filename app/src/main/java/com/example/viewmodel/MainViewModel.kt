package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    // Onboarding Form States
    var onboardingStep = MutableStateFlow(0)
    var selectedField = MutableStateFlow("Software Development")
    var selectedExperience = MutableStateFlow("Fresher")
    var selectedCompanies = MutableStateFlow("Google, Microsoft, Amazon")
    var selectedDuration = MutableStateFlow(30)

    // Generator States
    var generationProgress = MutableStateFlow(0.0f)
    var generationStatus = MutableStateFlow("")

    // Screen State: "SPLASH", "ONBOARDING", "HOME_DASHBOARD", "COACH_CHAT", "INTERVIEW_LOBBY", "ACTIVE_MOCK", "QUIZ_LOBBY", "ACTIVE_QUIZ", "FLASHCARDS", "APP_TRACKER", "NOTIFICATIONS"
    var currentScreen = MutableStateFlow("SPLASH")

    // Database Observables
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val roadmapSteps: StateFlow<List<RoadmapStep>> = repository.roadmapSteps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFlashcards: StateFlow<List<Flashcard>> = repository.allFlashcards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizAttempts: StateFlow<List<QuizAttempt>> = repository.quizAttempts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val interviewSessions: StateFlow<List<MockInterviewSession>> = repository.interviewSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val applications: StateFlow<List<JobApplication>> = repository.applications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<AppNotification>> = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val coachMessages: StateFlow<List<CareerCoachMessage>> = repository.coachMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Mock Interview States
    var activeInterviewSession = MutableStateFlow<MockInterviewSession?>(null)
    var activeInterviewMessages = MutableStateFlow<List<MockInterviewMessage>>(emptyList())
    var isInterviewAILoading = MutableStateFlow(false)
    var inputMessageText = MutableStateFlow("")

    // Active Quiz States
    var activeQuizTopic = MutableStateFlow("")
    var activeQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    var currentQuizQuestionIndex = MutableStateFlow(0)
    var selectedQuizOption = MutableStateFlow<Int?>(null) // null, 0, 1, 2, 3
    var quizSubmitted = MutableStateFlow(false)
    var quizScore = MutableStateFlow(0)
    var isQuizLoading = MutableStateFlow(false)

    // Career Coach States
    var inputCoachText = MutableStateFlow("")
    var isCoachMessageLoading = MutableStateFlow(false)

    // Resume Analyzer States
    var inputResumeText = MutableStateFlow("")
    var isAnalyzingResume = MutableStateFlow(false)

    // Application Tracker addition state
    var appCompanyInput = MutableStateFlow("")
    var appRoleInput = MutableStateFlow("")
    var appStatusInput = MutableStateFlow("Applied")
    var appNotesInput = MutableStateFlow("")

    init {
        // Automatically check if user completed onboarding
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile == null) {
                    // Create first default empty user profile
                    repository.saveUserProfile(UserProfile())
                } else if (profile.hasCompletedOnboarding && currentScreen.value == "SPLASH") {
                    // If onboarding was already completed, splash runs then homescreen
                    launchSplashScreen { currentScreen.value = "HOME_DASHBOARD" }
                } else if (!profile.hasCompletedOnboarding && currentScreen.value == "SPLASH") {
                    launchSplashScreen { currentScreen.value = "ONBOARDING" }
                }
            }
        }
    }

    private fun launchSplashScreen(onComplete: () -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            onComplete()
        }
    }

    // Onboarding Trigger
    fun startOnboarding() {
        onboardingStep.value = 1
    }

    fun nextOnboardingStep() {
        if (onboardingStep.value < 5) {
            onboardingStep.value += 1
        } else {
            // Generate Roadmap!
            onboardingStep.value = 5 // Screen of generation progress
            generatePlanAndFinishOnboarding()
        }
    }

    fun prevOnboardingStep() {
        if (onboardingStep.value > 0) {
            onboardingStep.value -= 1
        }
    }

    private fun generatePlanAndFinishOnboarding() {
        viewModelScope.launch {
            generationProgress.value = 0.1f
            generationStatus.value = "Connecting to Chief Curriculum Architect..."
            kotlinx.coroutines.delay(600)

            generationProgress.value = 0.3f
            generationStatus.value = "Assembling daily prep schedules..."

            try {
                // Call Gemini to generate roadmap and download resources
                repository.generatePersonalizedRoadmap(
                    career = selectedField.value,
                    exp = selectedExperience.value,
                    duration = selectedDuration.value
                )
                generationProgress.value = 0.6f
                generationStatus.value = "Creating spaced repetition flashcards..."

                // Pre-generate custom flashcards matching the career choice!
                repository.generateFlashcards(selectedField.value, 6)

                generationProgress.value = 0.8f
                generationStatus.value = "Loading target company mock assessments..."
                repository.getMockQuestions(selectedField.value, selectedExperience.value, 3)

                generationProgress.value = 1.0f
                generationStatus.value = "Profile generated successfully! Launching Pilot Operating System..."
                kotlinx.coroutines.delay(800)

                // Save Completed state
                val existing = userProfile.value ?: UserProfile()
                val updatedProfile = existing.copy(
                    hasCompletedOnboarding = true,
                    targetCareer = selectedField.value,
                    experienceLevel = selectedExperience.value,
                    targetCompanies = selectedCompanies.value,
                    prepDurationDays = selectedDuration.value,
                    xpScore = 150, // Initial onboarding bonus XP!
                    currentStreak = 1
                )
                repository.saveUserProfile(updatedProfile)

                repository.insertNotification(
                    "Welcome to InterviewPilot AI!",
                    "Your study roadmap for ${selectedField.value} is compiled. Prepare, practice, and let's get hired."
                )

                currentScreen.value = "HOME_DASHBOARD"
            } catch (e: Exception) {
                generationStatus.value = "Completed with local offline parameters: ${e.localizedMessage}"
                kotlinx.coroutines.delay(1000)
                val existing = userProfile.value ?: UserProfile()
                repository.saveUserProfile(
                    existing.copy(
                        hasCompletedOnboarding = true,
                        targetCareer = selectedField.value,
                        experienceLevel = selectedExperience.value,
                        targetCompanies = selectedCompanies.value,
                        prepDurationDays = selectedDuration.value,
                        xpScore = 100,
                        currentStreak = 1
                    )
                )
                currentScreen.value = "HOME_DASHBOARD"
            }
        }
    }

    // Add XP points (Gamification)
    fun addXp(amount: Int) {
        viewModelScope.launch {
            val prof = userProfile.value ?: return@launch
            val updated = prof.copy(xpScore = prof.xpScore + amount)
            repository.saveUserProfile(updated)
        }
    }

    // Update study streak daily
    fun checkStreakAndUpdate() {
        viewModelScope.launch {
            val prof = userProfile.value ?: return@launch
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toLong()
            if (prof.lastGoalUpdateDay == 0L) {
                repository.saveUserProfile(prof.copy(lastGoalUpdateDay = todayDate, currentStreak = 1))
            } else if (todayDate > prof.lastGoalUpdateDay) {
                val difference = todayDate - prof.lastGoalUpdateDay
                if (difference == 1L) {
                    val newStreak = prof.currentStreak + 1
                    repository.saveUserProfile(prof.copy(lastGoalUpdateDay = todayDate, currentStreak = newStreak))
                    repository.insertNotification("Streak Unlocked! \uD83D\uDD25", "You are on a $newStreak-Day study streak. Keep studying!")
                    addXp(30)
                } else if (difference > 1L) {
                    // Streak reset
                    repository.saveUserProfile(prof.copy(lastGoalUpdateDay = todayDate, currentStreak = 1))
                }
            }
        }
    }

    // Roadmap Step Progress updates
    fun updateRoadmapStepStatus(step: RoadmapStep, newStatus: String) {
        viewModelScope.launch {
            val updated = step.copy(status = newStatus)
            repository.updateRoadmapStep(updated)
            addXp(15) // Gamified XP for advancing milestones!

            val stepIndex = roadmapSteps.value.indexOfFirst { it.id == step.id }
            val total = roadmapSteps.value.size
            if (total > 0 && newStatus == "MASTERED") {
                repository.insertNotification(
                    "Milestone Mastered \uD83C\uDFC6",
                    "Congratulations on mastering: '${step.title}'! Your interview readiness score has scaled."
                )
            }
            checkStreakAndUpdate()
        }
    }

    // Flashcard interaction
    fun bookmarkFlashcard(card: Flashcard) {
        viewModelScope.launch {
            val updated = card.copy(isBookmarked = !card.isBookmarked)
            repository.updateFlashcard(updated)
        }
    }

    fun updateFlashcardMastery(card: Flashcard, level: Int) {
        viewModelScope.launch {
            val updated = card.copy(masteryLevel = level)
            repository.updateFlashcard(updated)
            if (level == 2) {
                addXp(10)
            }
        }
    }

    fun generateNewFlashcards() {
        viewModelScope.launch {
            val career = userProfile.value?.targetCareer ?: "Software Development"
            try {
                repository.generateFlashcards(career, 5)
                repository.insertNotification("New Flashcards Loaded", "Check out new conceptual cards customized for $career.")
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Quiz Navigation & execution
    fun startPracticeQuiz(topic: String) {
        viewModelScope.launch {
            isQuizLoading.value = true
            currentScreen.value = "ACTIVE_QUIZ"
            activeQuizTopic.value = topic
            currentQuizQuestionIndex.value = 0
            selectedQuizOption.value = null
            quizSubmitted.value = false
            quizScore.value = 0

            val list = repository.getMockQuestions(topic, userProfile.value?.experienceLevel ?: "Fresher", 4)
            activeQuizQuestions.value = list
            isQuizLoading.value = false
        }
    }

    fun selectQuizAnswer(optionIndex: Int) {
        if (!quizSubmitted.value) {
            selectedQuizOption.value = optionIndex
        }
    }

    fun submitQuizQuestion() {
        val questionsList = activeQuizQuestions.value
        val currentIndex = currentQuizQuestionIndex.value
        val selectedIdx = selectedQuizOption.value ?: return

        quizSubmitted.value = true
        if (selectedIdx == questionsList[currentIndex].correctIndex) {
            quizScore.value += 1
            addXp(10)
        }
    }

    fun nextQuizQuestion() {
        val total = activeQuizQuestions.value.size
        val currentIndex = currentQuizQuestionIndex.value
        if (currentIndex < total - 1) {
            currentQuizQuestionIndex.value += 1
            selectedQuizOption.value = null
            quizSubmitted.value = false
        } else {
            // Quiz completed!
            viewModelScope.launch {
                val finalScore = quizScore.value
                val totalQuestions = activeQuizQuestions.value.size
                repository.insertQuizAttempt(
                    QuizAttempt(topic = activeQuizTopic.value, score = finalScore, totalQuestions = totalQuestions)
                )
                repository.insertNotification(
                    "Quiz Finished \uD83D\uDCDD",
                    "Completed the $activeQuizTopic quiz! Score: $finalScore/$totalQuestions questions answered correctly."
                )
                addXp(40) // End quiz completion reward XP
                currentScreen.value = "QUIZ_LOBBY"
            }
        }
    }

    // AI Career Coach Prompt Flow
    fun sendCoachMessage() {
        val prompt = inputCoachText.value.trim()
        if (prompt.isBlank() || isCoachMessageLoading.value) return

        viewModelScope.launch {
            inputCoachText.value = ""
            isCoachMessageLoading.value = true

            // Insert User message in DB
            val userMsg = CareerCoachMessage(sender = "USER", text = prompt)
            repository.insertCoachMessage(userMsg)

            val hist = coachMessages.value
            // Call API
            val response = repository.getCoachMessageResponse(prompt, hist)
            val aiMsg = CareerCoachMessage(sender = "AI", text = response)
            repository.insertCoachMessage(aiMsg)

            isCoachMessageLoading.value = false
            addXp(15)
            checkStreakAndUpdate()
        }
    }

    fun clearCoachChat() {
        viewModelScope.launch {
            repository.clearCoachChat()
        }
    }

    // Mock Interview Operations
    fun openActiveInterviewLobby() {
        currentScreen.value = "INTERVIEW_LOBBY"
    }

    fun launchMockInterview(type: String) {
        val role = userProfile.value?.targetCareer ?: "Software Development"
        val difficulty = when (userProfile.value?.experienceLevel) {
            "Student", "Fresher" -> "Beginner"
            "1 Year", "2 Years" -> "Intermediate"
            else -> "Advanced"
        }

        viewModelScope.launch {
            isInterviewAILoading.value = true
            currentScreen.value = "ACTIVE_MOCK"

            val initialSession = MockInterviewSession(
                role = role,
                difficulty = difficulty,
                type = type
            )
            val sessionId = repository.insertInterviewSession(initialSession).toInt()
            val activeSession = initialSession.copy(id = sessionId)
            activeInterviewSession.value = activeSession
            activeInterviewMessages.value = emptyList()

            // Fetch first AI question
            val promptFirstQuestion = repository.getMockInterviewAIQuestion(role, type, emptyList())
            val firstMsg = MockInterviewMessage(sessionId = sessionId, sender = "AI", text = promptFirstQuestion)
            repository.insertInterviewMessage(firstMsg)

            // Dynamic stream observer
            repository.interviewMessages(sessionId).collect { list ->
                activeInterviewMessages.value = list
            }
        }
        isInterviewAILoading.value = false
    }

    fun sendInterviewAnswer() {
        val answer = inputMessageText.value.trim()
        val session = activeInterviewSession.value ?: return
        if (answer.isBlank() || isInterviewAILoading.value) return

        viewModelScope.launch {
            inputMessageText.value = ""
            isInterviewAILoading.value = true

            // Insert user dialog unit
            val userMsg = MockInterviewMessage(sessionId = session.id, sender = "USER", text = answer)
            repository.insertInterviewMessage(userMsg)

            val currentHistory = activeInterviewMessages.value
            // Request next dynamic item from AI
            val nextAIQuestion = repository.getMockInterviewAIQuestion(session.role, session.type, currentHistory)
            val aiMsg = MockInterviewMessage(sessionId = session.id, sender = "AI", text = nextAIQuestion)
            repository.insertInterviewMessage(aiMsg)

            isInterviewAILoading.value = false
            addXp(20)
        }
    }

    fun completeInterviewAndAnalyze() {
        val session = activeInterviewSession.value ?: return
        if (isInterviewAILoading.value) return

        viewModelScope.launch {
            isInterviewAILoading.value = true
            val history = activeInterviewMessages.value

            val (score, feedback) = repository.evaluateMockInterviewSession(history)

            val completedSession = session.copy(
                isCompleted = true,
                overallScore = score,
                feedback = feedback
            )
            repository.updateInterviewSession(completedSession)

            // Alert Notification
            repository.insertNotification(
                "Interview Review Ready \uD83D\uDCCB",
                "Your simulated mock round for '${session.type}' has been calculated. Score achieved: $score%. Read complete recommendations."
            )

            addXp(60) // Extra completion multiplier
            checkStreakAndUpdate()

            activeInterviewSession.value = completedSession
            isInterviewAILoading.value = false
            currentScreen.value = "INTERVIEW_LOBBY"
        }
    }

    // Resume evaluation
    fun evaluateResumeATS() {
        val resume = inputResumeText.value.trim()
        if (resume.isBlank() || isAnalyzingResume.value) return

        viewModelScope.launch {
            isAnalyzingResume.value = true
            val (score, feedback) = repository.analyzeResumeText(resume)

            val existing = userProfile.value ?: UserProfile()
            val updatedProf = existing.copy(
                resumeContent = resume,
                resumeScore = score,
                resumeFeedback = feedback
            )
            repository.saveUserProfile(updatedProf)

            repository.insertNotification(
                "Resume Review Completed 📄",
                "ATS compliance analyzer executed! Score: $score%. Recommendations are logged in Profile analytics."
            )

            addXp(50)
            isAnalyzingResume.value = false
        }
    }

    // Application Tracking
    fun addJobApplication() {
        val company = appCompanyInput.value.trim()
        val role = appRoleInput.value.trim()
        val status = appStatusInput.value
        val notes = appNotesInput.value.trim()

        if (company.isBlank() || role.isBlank()) return

        viewModelScope.launch {
            val date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date())
            val app = JobApplication(
                company = company,
                role = role,
                status = status,
                dateString = date,
                notes = notes
            )
            repository.insertApplication(app)
            addXp(15)

            // Clear inputs
            appCompanyInput.value = ""
            appRoleInput.value = ""
            appStatusInput.value = "Applied"
            appNotesInput.value = ""

            repository.insertNotification("Application Logged 💼", "Added prospective application for $role at $company.")
        }
    }

    fun removeJobApplication(app: JobApplication) {
        viewModelScope.launch {
            repository.deleteApplication(app)
        }
    }

    // Readiness score calculation on demand
    // Formula based on topics completed (RoadmapStep status MASTERED), Quiz attempts, Mock scores, and resume rating.
    fun getCalculatedReadinessScore(): Int {
        val steps = roadmapSteps.value
        val completedSteps = steps.count { it.status == "MASTERED" }
        val factorRoadmap = if (steps.isNotEmpty()) (completedSteps.toFloat() / steps.size) * 45 else 10f

        val quizList = quizAttempts.value
        val avgQuiz = if (quizList.isNotEmpty()) {
            quizList.map { (it.score.toFloat() / it.totalQuestions) * 100 }.average().toFloat()
        } else {
            50f
        }
        val factorQuiz = (avgQuiz / 100) * 20

        val interviews = interviewSessions.value.filter { it.isCompleted }
        val avgInterview = if (interviews.isNotEmpty()) {
            interviews.map { it.overallScore }.average().toFloat()
        } else {
            50f
        }
        val factorInterview = (avgInterview / 100) * 25

        val resumeScoreVal = userProfile.value?.resumeScore?.toFloat() ?: 50f
        val factorResume = (resumeScoreVal / 100) * 10

        val finalScore = (factorRoadmap + factorQuiz + factorInterview + factorResume).toInt()
        return finalScore.coerceIn(5, 100)
    }

    fun getReadinessText(score: Int): Pair<String, String> {
        return when {
            score <= 40 -> Pair("Needs Improvement", "Focus on completing fundamental roadmap stages and attempting quizzes.")
            score <= 70 -> Pair("Progressing", "You're making nice strides. Prepare core concepts and request Resume reviews!")
            score <= 85 -> Pair("Nearly Ready", "Excellent standing! Rehearse with the AI Mock interviewer to finalize confidence.")
            else -> Pair("Interview Ready", "Outstanding! Your profile is highly compliant. Good luck on candidate queues!")
        }
    }
}

// ViewModel Factory to bypass Hilt dependency injectors
class MainViewModelFactory(private val application: Application, private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
