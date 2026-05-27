package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    // Database flows
    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()
    val roadmapSteps: Flow<List<RoadmapStep>> = appDao.getRoadmapSteps()
    val allFlashcards: Flow<List<Flashcard>> = appDao.getAllFlashcards()
    val quizAttempts: Flow<List<QuizAttempt>> = appDao.getQuizAttempts()
    val interviewSessions: Flow<List<MockInterviewSession>> = appDao.getInterviewSessions()
    val applications: Flow<List<JobApplication>> = appDao.getApplications()
    val notifications: Flow<List<AppNotification>> = appDao.getNotifications()

    fun interviewMessages(sessionId: Int): Flow<List<MockInterviewMessage>> {
        return appDao.getInterviewMessages(sessionId)
    }

    val coachMessages: Flow<List<CareerCoachMessage>> = appDao.getCoachMessages()

    // Database Writes
    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        appDao.insertUserProfile(profile)
    }

    suspend fun updateRoadmapStep(step: RoadmapStep) = withContext(Dispatchers.IO) {
        appDao.updateRoadmapStep(step)
    }

    suspend fun clearRoadmap() = withContext(Dispatchers.IO) {
        appDao.clearRoadmap()
    }

    suspend fun updateFlashcard(card: Flashcard) = withContext(Dispatchers.IO) {
        appDao.updateFlashcard(card)
    }

    suspend fun insertQuizAttempt(attempt: QuizAttempt) = withContext(Dispatchers.IO) {
        appDao.insertQuizAttempt(attempt)
    }

    suspend fun insertInterviewSession(session: MockInterviewSession): Long = withContext(Dispatchers.IO) {
        appDao.insertInterviewSession(session)
    }

    suspend fun updateInterviewSession(session: MockInterviewSession) = withContext(Dispatchers.IO) {
        appDao.updateInterviewSession(session)
    }

    suspend fun insertInterviewMessage(message: MockInterviewMessage) = withContext(Dispatchers.IO) {
        appDao.insertInterviewMessage(message)
    }

    suspend fun insertCoachMessage(message: CareerCoachMessage) = withContext(Dispatchers.IO) {
        appDao.insertCoachMessage(message)
    }

    suspend fun clearCoachChat() = withContext(Dispatchers.IO) {
        appDao.clearCoachChat()
    }

    suspend fun insertApplication(app: JobApplication) = withContext(Dispatchers.IO) {
        appDao.insertApplication(app)
    }

    suspend fun deleteApplication(app: JobApplication) = withContext(Dispatchers.IO) {
        appDao.deleteApplication(app)
    }

    suspend fun insertNotification(title: String, message: String) = withContext(Dispatchers.IO) {
        appDao.insertNotification(AppNotification(title = title, message = message))
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        appDao.markNotificationAsRead(id)
    }

    // Direct REST API calls helper
    private suspend fun callGemini(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext "APIKeyError: Please configure your Gemini API Key in the AI Studio Secrets panel."
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = systemInstruction?.let { GeminiContent(parts = listOf(GeminiPart(text = it))) }
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from Gemini"
        } catch (e: Exception) {
            Log.e("AppRepository", "Gemini API failure", e)
            "Error: ${e.message}"
        }
    }

    // Roadmap Generation
    suspend fun generatePersonalizedRoadmap(career: String, exp: String, duration: Int): List<RoadmapStep> {
        val prompt = """
            Create a highly tailored daily/weekly interview preparation roadmap for a candidates of experience level: $exp in the field: $career.
            The timeline is $duration days.
            Provide exactly 6 comprehensive milestones. Format each milestone using custom tags exactly like so:
            ##STEP##
            PHASE: [Milestone Scope (e.g. Foundation, Core Concepts, Advanced, Mock Interview)]
            TITLE: [A concise, impactful topic name]
            DESC: [A paragraph explaining critical things to study and concepts under this Milestone]
            RESOURCES: [Key books, practice links, or topics to learn. Max 100 letters]
            SUMMARY: [A cheat-sheet explanation or checklist summarizing the top formulas, tips, or paradigms for immediate revision. Max 200 letters]
            ##STEP##
            
            Do not write any introductory or outro text. Use only the ##STEP## markers exactly as requested.
        """.trimIndent()

        val system = "You are InterviewPilot AI's chief curriculum architect, dedicated to maximizing candidates' hireability."
        val response = callGemini(prompt, system)

        if (response.startsWith("APIKeyError") || response.startsWith("Error")) {
            // Return rich offline fallback
            return getFallbackRoadmap(career, duration)
        }

        val steps = parseRoadmapSteps(response)
        return if (steps.isNotEmpty()) {
            clearRoadmap()
            appDao.insertRoadmapSteps(steps)
            steps
        } else {
            val fallback = getFallbackRoadmap(career, duration)
            clearRoadmap()
            appDao.insertRoadmapSteps(fallback)
            fallback
        }
    }

    private fun parseRoadmapSteps(text: String): List<RoadmapStep> {
        val steps = mutableListOf<RoadmapStep>()
        val blocks = text.split("##STEP##")
        var order = 0
        for (block in blocks) {
            val trimmed = block.trim()
            if (trimmed.length < 30) continue // Skip empty slices

            var phase = "Foundation"
            var title = "Concept Study"
            var desc = ""
            var resources = ""
            var summary = ""

            trimmed.lines().forEach { line ->
                val cleaned = line.trim()
                when {
                    cleaned.startsWith("PHASE:") -> phase = cleaned.removePrefix("PHASE:").trim()
                    cleaned.startsWith("TITLE:") -> title = cleaned.removePrefix("TITLE:").trim()
                    cleaned.startsWith("DESC:") -> desc = cleaned.removePrefix("DESC:").trim()
                    cleaned.startsWith("RESOURCES:") -> resources = cleaned.removePrefix("RESOURCES:").trim()
                    cleaned.startsWith("SUMMARY:") -> summary = cleaned.removePrefix("SUMMARY:").trim()
                }
            }

            steps.add(
                RoadmapStep(
                    phase = phase,
                    title = title,
                    description = desc,
                    learningResources = resources,
                    summary = summary,
                    orderIndex = order++
                )
            )
        }
        return steps
    }

    private fun getFallbackRoadmap(career: String, duration: Int): List<RoadmapStep> {
        return listOf(
            RoadmapStep(
                phase = "Foundation",
                title = "Fundamental Fundamentals of $career",
                description = "Deep dive into initial paradigms, basics, terminology, and core structures. Learn about syntax, standard tools, and fundamental principles.",
                learningResources = "Book: Introduction to $career\nOnline: Free docs & blogs",
                summary = "Master the basics first. Practice standard terminologies, variable types, clean-code rules, and scope concepts.",
                status = "NOT_STARTED",
                orderIndex = 0
            ),
            RoadmapStep(
                phase = "Core Concepts",
                title = "Intermediate Mechanics & APIs",
                description = "Study essential operations, database queries, collection architectures, patterns, and dynamic application routines.",
                learningResources = "Online tutorials: $career intermediate walkthroughs",
                summary = "Examine time limits, libraries to optimize layouts, algorithms, and modular frameworks.",
                status = "NOT_STARTED",
                orderIndex = 1
            ),
            RoadmapStep(
                phase = "Intermediate Concepts",
                title = "Clean Design Patterns & Scaling",
                description = "Master asynchronous protocols, architectural decoupling, state machines, and SOLID principles in action.",
                learningResources = "Design Patterns handbook, refactoring guides",
                summary = "Prefer clean separation. Understand creational, structural, and behavioral design patterns.",
                status = "NOT_STARTED",
                orderIndex = 2
            ),
            RoadmapStep(
                phase = "Advanced Concepts",
                title = "Performance Optimization & Fault Tolerance",
                description = "Optimize system loops, cache architectures, concurrency primitives, and diagnostic tooling.",
                learningResources = "Advanced JVM tuning guides / Platform performance profiling",
                summary = "Look for slow DB queries, memory leaks, and render lag. Profile memory and battery consumption early.",
                status = "NOT_STARTED",
                orderIndex = 3
            ),
            RoadmapStep(
                phase = "Projects & Deep-dive",
                title = "Practical Implementation Portfolio",
                description = "Construct complete applications or write system services showcasing modern architecture, testing benchmarks, and dynamic handling.",
                learningResources = "Personal portfolio showcase / GitHub repos",
                summary = "Host your code clearly. Keep clean documentation in README, explain design tradeoffs confidently.",
                status = "NOT_STARTED",
                orderIndex = 4
            ),
            RoadmapStep(
                phase = "Mock Interviews & Launch",
                title = "Interview Drills & Final Readiness",
                description = "Perform continuous mock speech reviews, solve random coding tasks under time boxes, and refine behavioral answers.",
                learningResources = "InterviewPilot AI mock session tools",
                summary = "Rehearse using the STAR method (Situation, Task, Action, Result). State impact over actions.",
                status = "NOT_STARTED",
                orderIndex = 5
            )
        )
    }

    // Flashcards generation
    suspend fun generateFlashcards(career: String, count: Int): List<Flashcard> {
        val prompt = """
            Create $count interview flashcards for the field: $career.
            Format each card exactly like this:
            ##CARD##
            TYPE: [Concept / Definition / Scenario]
            TOPIC: [Subtopic, e.g. OOP, DB, Android]
            Q: [A clear, concise, direct question or scenario-based problem]
            A: [A short, accurate, high-yield answers suitable for immediate memorization. Max 150 letters]
            ##CARD##
            
            Do not output any additional conversational introductory or outro text.
        """.trimIndent()

        val response = callGemini(prompt, "You are a flashcard generator for academic and career excellence.")

        val cards = mutableListOf<Flashcard>()
        if (response.startsWith("APIKeyError") || response.startsWith("Error")) {
            return getFallbackFlashcards(career)
        }

        val blocks = response.split("##CARD##")
        for (block in blocks) {
            val trimmed = block.trim()
            if (trimmed.length < 20) continue

            var type = "Concept"
            var topic = career
            var q = ""
            var a = ""

            trimmed.lines().forEach { line ->
                val cleaned = line.trim()
                when {
                    cleaned.startsWith("TYPE:") -> type = cleaned.removePrefix("TYPE:").trim()
                    cleaned.startsWith("TOPIC:") -> topic = cleaned.removePrefix("TOPIC:").trim()
                    cleaned.startsWith("Q:") -> q = cleaned.removePrefix("Q:").trim()
                    cleaned.startsWith("A:") -> a = cleaned.removePrefix("A:").trim()
                }
            }

            if (q.isNotBlank() && a.isNotBlank()) {
                cards.add(Flashcard(type = type, topic = topic, question = q, answer = a))
            }
        }

        if (cards.isNotEmpty()) {
            appDao.insertFlashcards(cards)
        } else {
            val fallback = getFallbackFlashcards(career)
            appDao.insertFlashcards(fallback)
            return fallback
        }
        return cards
    }

    private fun getFallbackFlashcards(career: String): List<Flashcard> {
        return listOf(
            Flashcard(
                topic = "$career Core",
                question = "What is the primary trade-off between SQL and NoSQL?",
                answer = "SQL guarantees ACID compliance with relational structure; NoSQL scales horizontally with flexible document schemas.",
                type = "Concept"
            ),
            Flashcard(
                topic = "Architecture",
                question = "Explain SOLID principles in brief.",
                answer = "SOLID stands for: Single responsibility, Open/Closed, Liskov substitution, Interface segregation, and Dependency inversion.",
                type = "Definition"
            ),
            Flashcard(
                topic = "Systems",
                question = "How does caching speed up read-heavy applications?",
                answer = "By keeping copies of highly queried database records in active RAM (e.g., Redis/Memcached) to bypass heavy disk lookups.",
                type = "Scenario"
            ),
            Flashcard(
                topic = "$career OOP",
                question = "What is encapsulation and why is it useful?",
                answer = "Hiding internal object states and requiring all interaction through public methods. Protects object integrity and consistency.",
                type = "Concept"
            )
        )
    }

    // Quiz Questions generator
    suspend fun getMockQuestions(career: String, exp: String, count: Int): List<QuizQuestion> {
        val existing = appDao.getAllQuestions().filter { it.topic.equals(career, ignoreCase = true) }
        if (existing.size >= count) {
            return existing.take(count)
        }

        val prompt = """
            Generate exactly $count multiple-choice quiz questions for an interview with experience level: $exp in the field: $career.
            Format each question exactly like this:
            ##QUIZ##
            Q: [Clearly formulated question]
            A: [Option A]
            B: [Option B]
            C: [Option C]
            D: [Option D]
            CORRECT: [0 for A, 1 for B, 2 for C, or 3 for D]
            EXPLANATION: [A brief, helpful explanation why this key is correct]
            ##QUIZ##
            
            Strictly avoid chat headers or outros. Print only quiz units.
        """.trimIndent()

        val response = callGemini(prompt, "You are a specialized technical quiz builder.")
        val questions = mutableListOf<QuizQuestion>()

        if (!response.startsWith("APIKeyError") && !response.startsWith("Error")) {
            val blocks = response.split("##QUIZ##")
            for (block in blocks) {
                val trimmed = block.trim()
                if (trimmed.length < 30) continue

                var q = ""
                var a = ""
                var b = ""
                var c = ""
                var d = ""
                var correct = 0
                var explanation = ""

                trimmed.lines().forEach { line ->
                    val cleaned = line.trim()
                    when {
                        cleaned.startsWith("Q:") -> q = cleaned.removePrefix("Q:").trim()
                        cleaned.startsWith("A:") -> a = cleaned.removePrefix("A:").trim()
                        cleaned.startsWith("B:") -> b = cleaned.removePrefix("B:").trim()
                        cleaned.startsWith("C:") -> c = cleaned.removePrefix("C:").trim()
                        cleaned.startsWith("D:") -> d = cleaned.removePrefix("D:").trim()
                        cleaned.startsWith("CORRECT:") -> {
                            correct = cleaned.removePrefix("CORRECT:").trim().toIntOrNull() ?: 0
                        }
                        cleaned.startsWith("EXPLANATION:") -> explanation = cleaned.removePrefix("EXPLANATION:").trim()
                    }
                }

                if (q.isNotBlank() && a.isNotBlank() && b.isNotBlank()) {
                    questions.add(
                        QuizQuestion(
                            topic = career,
                            question = q,
                            optionA = a,
                            optionB = b,
                            optionC = c,
                            optionD = d,
                            correctIndex = correct,
                            explanation = explanation
                        )
                    )
                }
            }
        }

        return if (questions.isNotEmpty()) {
            appDao.insertQuizQuestions(questions)
            questions
        } else {
            val fallback = getFallbackQuiz(career)
            appDao.insertQuizQuestions(fallback)
            fallback
        }
    }

    private fun getFallbackQuiz(career: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                topic = career,
                question = "Which design pattern is best suited to decouping operations from objects in a pipeline?",
                optionA = "Singleton Pattern",
                optionB = "Observer Pattern",
                optionC = "Strategy Pattern",
                optionD = "Builder Pattern",
                correctIndex = 2,
                explanation = "Strategy pattern defines a family of algorithms and encapsulating each one, letting you swap them transparently."
            ),
            QuizQuestion(
                topic = career,
                question = "What does a 404 HTTP Status Code represent?",
                optionA = "Unauthorized request access error",
                optionB = "The requested server resource was not found",
                optionC = "Internal database connection failure",
                optionD = "Successful server request completion",
                correctIndex = 1,
                explanation = "A 404 error represents that the client can communicate with the server, but the requested page or asset does not exist."
            ),
            QuizQuestion(
                topic = career,
                question = "What is the primary benefit of declaring index columns in a database table?",
                optionA = "Decreases disk size requirements",
                optionB = "Speeds up data retrieval queries",
                optionC = "Locks down secure user operations",
                optionD = "Guarantees absolute input data integrity",
                correctIndex = 1,
                explanation = "Indexation forms lookup indexes allowing queries to quickly search rows without executing a full-table scan."
            )
        )
    }

    // AI Career Coach
    suspend fun getCoachMessageResponse(prompt: String, history: List<CareerCoachMessage>): String {
        // Construct standard context history of last 10 messages for speed and accuracy
        val historyContext = history.takeLast(10).joinToString("\n") {
            "${it.sender}: ${it.text}"
        }

        val fullPrompt = """
            The candidate wants guidance, career counseling, custom resume preparation strategy, or concept explanation.
            Here is the conversation history:
            $historyContext
            USER: $prompt
            
            Please reply as an inspiring, empathetic, and sharp career coach. Limit your response to 4 concise, high-impact bullet points or 1-2 powerful paragraphs.
        """.trimIndent()

        val system = "You are InterviewPilot AI Career Coach. Help candidates navigate resume issues, mock anxiety, negotiation, and concept comprehension elegantly."
        val outResponse = callGemini(fullPrompt, system)
        if (outResponse.startsWith("APIKeyError")) {
            return "Hi there! I would love to explain this, but your Gemini API Key is missing. You can configure it in the Secrets panel on Google AI Studio to unlock full AI conversational capabilities!"
        }
        return outResponse
    }

    // Mock Interview AI Dialogues
    suspend fun getMockInterviewAIQuestion(role: String, type: String, history: List<MockInterviewMessage>): String {
        val historyContext = history.takeLast(8).joinToString("\n") {
            "${it.sender}: ${it.text}"
        }

        val prompt = """
            You are conducting a $type interview with a candidate seeking a $role position.
            Your task is to ask a single, targeted interview question.
            IF the conversation is just beginning (no history): ask an introducing introductory question or situational opener.
            ELSE (there is history): evaluate the candidate's last answer, acknowledge briefly, and present the next logical technical or behavior-focused deep dive question.
            
            Here is the conversation so far:
            $historyContext
            
            Provide only your next question. Do not add metadata or robotic labels like 'AI Interviewer:'. Keep it direct and natural.
        """.trimIndent()

        val system = "You are a precise, senior tech interviewer at a top company. Be professional, direct, and conversational. Do not output anything other than your interview response."
        val out = callGemini(prompt, system)
        if (out.startsWith("APIKeyError")) {
            return "Welcome! Let's start the mock interview. Currently, your AI API Key is offline in the workspace. We will run a locally simulated, top-tier mock session. Question: 'Could you describe the most robust system or project you have compiled, and some key technical challenges you overcame?'"
        }
        return out
    }

    // Evaluate Interview
    suspend fun evaluateMockInterviewSession(history: List<MockInterviewMessage>): Pair<Int, String> {
        if (history.size < 2) {
            return Pair(50, "The interview was too brief. Prepare and type answers to questions to request a full report.")
        }
        val content = history.joinToString("\n") { "${it.sender}: ${it.text}" }

        val prompt = """
            Evaluate the following interview script:
            $content
            
            Analyze:
            1. Technical precision of answers.
            2. Communication clarity.
            3. Weak areas detected.
            4. Improvement plan.
            
            Output exact scoring format:
            SCORE: [An integer from 10 to 95 based on technical merit]
            FEEDBACK:
            [A structured feedback with sections: 'Technical Merit', 'Communication', 'Weak Areas Identified', 'Recommended Rehearsals']
            
            Limit your feedback to 200 words max, keep it professional and encouraging!
        """.trimIndent()

        val system = "You are an Elite Interview Reviewer. Calculate exact scores and outline developmental areas."
        val res = callGemini(prompt, system)

        if (res.startsWith("APIKeyError") || res.startsWith("Error")) {
            return Pair(75, "Mock session completed successfully. (Simulated evaluation: Communications: Excellent (85%), Technical Clarity: Progressing (72%). Weak topics: Optimization. Recommended strategy: complete Roadmap step 3 & 4.)")
        }

        var score = 70
        var feedback = res

        try {
            res.lines().forEach { line ->
                val cleaned = line.trim()
                if (cleaned.startsWith("SCORE:")) {
                    score = cleaned.removePrefix("SCORE:").trim().replace("%", "").toIntOrNull() ?: 70
                }
            }
            feedback = res.substringAfter("FEEDBACK:").trim()
            if (feedback.isBlank()) feedback = res
        } catch (e: Exception) {
            // Unparsed
        }

        return Pair(score, feedback)
    }

    // Resume Analysis
    suspend fun analyzeResumeText(resumeText: String): Pair<Int, String> {
        val prompt = """
            Analyze this candidate resume:
            $resumeText
            
            Conduct an ATS evaluation and check. Detect formatting gaps, missing keywords, structural quality, and provide an exact ATS Score.
            
            Format your response strictly like this:
            SCORE: [Integer from 0 to 100]
            RECS:
            - [Rec 1]
            - [Rec 2]
            - [Rec 3]
            
            Keep your recommendations precise and under 150 words.
        """.trimIndent()

        val response = callGemini(prompt, "You are a certified senior recruiter and ATS Resume optimization bot.")

        if (response.startsWith("APIKeyError") || response.startsWith("Error")) {
            return Pair(65, "1. Format section headers clearly.\n2. Incorporate action verbs like 'Architected', 'Spearheaded'.\n3. Add dedicated 'Skills' keywords matching the job catalog.")
        }

        var score = 60
        var recs = response

        try {
            response.lines().forEach { line ->
                val cl = line.trim()
                if (cl.startsWith("SCORE:")) {
                    score = cl.removePrefix("SCORE:").trim().replace("%", "").toIntOrNull() ?: 60
                }
            }
            recs = response.substringAfter("RECS:").trim()
            if (recs.isBlank()) recs = response
        } catch (e: Exception) {
            // Ignore parse errors
        }

        return Pair(score, recs)
    }
}
