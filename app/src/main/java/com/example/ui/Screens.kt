package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListState as CustomLazyListState // just in case
import com.example.data.*
import com.example.viewmodel.MainViewModel

@Composable
fun AppNavigationContainer(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "SPLASH" -> SplashScreen()
                "ONBOARDING" -> OnboardingScreen(viewModel)
                "HOME_DASHBOARD", "ROADMAP", "PRACTICE", "COACH_CHAT", "PROFILE_ANALYTICS" -> {
                    MainAppLayout(viewModel, screen)
                }
                "ACTIVE_MOCK" -> ActiveMockInterviewScreen(viewModel)
                "ACTIVE_QUIZ" -> ActiveQuizScreen(viewModel)
                else -> SplashScreen()
            }
        }
    }
}

// -------------------------------------------------------------
// 1. SPLASH SCREEN
// -------------------------------------------------------------
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")
    val rocketOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EasyInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RocketFloat"
    )

    var textVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        textVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF2563EB))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-fidelity graphic: Target with Launching Rocket
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        // Drawing subtle target ring
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = size.minDimension / 2.3f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.25f),
                            radius = size.minDimension / 3.5f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                    }
                    .offset(y = rocketOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Launching Rocket Icon",
                    tint = Color(0xFF10B981), // Success Green
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "InterviewPilot AI",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prepare • Practice • Get Hired",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private val EasyInOut = Easing { fraction ->
    val t = fraction * 2.0f
    if (t < 1.0f) 0.5f * t * t else -0.5f * ((t - 1.0f) * (t - 3.0f) - 1.0f)
}

// -------------------------------------------------------------
// 2. ONBOARDING SEQUENCE
// -------------------------------------------------------------
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Theme background
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress tracker
            if (step in 1..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevOnboardingStep() },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back icon",
                            tint = Color.White
                        )
                    }
                    LinearProgressIndicator(
                        progress = { step / 4f },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Step $step/4",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                    )
                }
            }

            // Central layout dynamically populated by active wizard step
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    0 -> OnboardingWelcomePart(viewModel)
                    1 -> OnboardingChooseCareer(viewModel)
                    2 -> OnboardingChooseExperience(viewModel)
                    3 -> OnboardingEnterTargetCompanies(viewModel)
                    4 -> OnboardingDurationSelector(viewModel)
                    5 -> OnboardingGeneratingSplash(viewModel)
                }
            }

            // Navigation Bar Controls
            if (step in 1..4) {
                Button(
                    onClick = { viewModel.nextOnboardingStep() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == 4) "Generate Plan \uD83D\uDE80" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingWelcomePart(viewModel: MainViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = "Welcome Icon",
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome to InterviewPilot AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your complete AI-powered interview preparation companion.\nLearn, practice, track, and land your dream job.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = { viewModel.startOnboarding() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp)
                .testTag("start_onboarding_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Let's Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun OnboardingChooseCareer(viewModel: MainViewModel) {
    val selected by viewModel.selectedField.collectAsStateWithLifecycle()
    val fields = listOf(
        "Software Development",
        "Data Science",
        "Product Management",
        "Marketing",
        "Sales",
        "Finance",
        "HR",
        "Operations",
        "Design",
        "Analytics"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Career Field",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We will generate custom curriculums matching this category.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fields) { field ->
                val active = selected == field
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedField.value = field }
                        .testTag("career_field_${field.replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFF1E293B)
                    ),
                    border = BorderStroke(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) Color(0xFF2563EB) else Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = field, fontWeight = FontWeight.SemiBold, color = Color.White)
                        if (active) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Checked", tint = Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingChooseExperience(viewModel: MainViewModel) {
    val selected by viewModel.selectedExperience.collectAsStateWithLifecycle()
    val options = listOf("Student", "Fresher", "1 Year", "2 Years", "3+ Years")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Experience Level",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Helps our AI calibrates the difficulty scores correctly.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
        )
        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { option ->
            val active = selected == option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.selectedExperience.value = option },
                colors = CardDefaults.cardColors(
                    containerColor = if (active) Color(0xFF7C3AED).copy(alpha = 0.2f) else Color(0xFF1E293B)
                ),
                border = BorderStroke(
                    width = if (active) 2.dp else 1.dp,
                    color = if (active) Color(0xFF7C3AED) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = option, fontWeight = FontWeight.SemiBold, color = Color.White)
                    if (active) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active option", tint = Color(0xFF7C3AED))
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingEnterTargetCompanies(viewModel: MainViewModel) {
    val inputVal by viewModel.selectedCompanies.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Target Companies",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "List prominent companies you target (comma-separated). We'll pre-load matching assessment topics.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputVal,
            onValueChange = { viewModel.selectedCompanies.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("target_companies_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFF334155)
            ),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Google, Microsoft, Amazon, etc.", color = Color(0xFF6B7280)) }
        )
    }
}

@Composable
fun OnboardingDurationSelector(viewModel: MainViewModel) {
    val activeDuration by viewModel.selectedDuration.collectAsStateWithLifecycle()
    val timelines = listOf(
        Pair(7, "7 Days (Crash Course)"),
        Pair(15, "15 Days (Intense Bootcamp)"),
        Pair(30, "30 Days (Standard Pace)"),
        Pair(60, "60 Days (Comprehensive)"),
        Pair(90, "90 Days (Deep Development)")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Preparation Duration",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select study timeline window for roadmap chunking.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
        )
        Spacer(modifier = Modifier.height(24.dp))

        timelines.forEach { (days, label) ->
            val active = activeDuration == days
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.selectedDuration.value = days },
                colors = CardDefaults.cardColors(
                    containerColor = if (active) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFF1E293B)
                ),
                border = BorderStroke(
                    width = if (active) 2.dp else 1.dp,
                    color = if (active) Color(0xFF2563EB) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White)
                    if (active) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active option", tint = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingGeneratingSplash(viewModel: MainViewModel) {
    val progress by viewModel.generationProgress.collectAsStateWithLifecycle()
    val statusText by viewModel.generationStatus.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = "Launch Loader",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Compiling Your AI InterviewPilot...",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFF2563EB),
            trackColor = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// 3. MAIN TABBED SCREEN CONTAINER
// -------------------------------------------------------------
@Composable
fun MainAppLayout(viewModel: MainViewModel, activeTab: String) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF6B7280)
            ) {
                val items = listOf(
                    Triple("HOME_DASHBOARD", "Home", Icons.Default.Home),
                    Triple("ROADMAP", "Roadmap", Icons.Default.CompassCalibration),
                    Triple("PRACTICE", "Practice", Icons.Default.TaskAlt),
                    Triple("COACH_CHAT", "AI Coach", Icons.Default.SupportAgent),
                    Triple("PROFILE_ANALYTICS", "Profile", Icons.Default.AccountCircle)
                )

                items.forEach { (tabId, label, icon) ->
                    val selected = activeTab == tabId
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.currentScreen.value = tabId },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$label Tab Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(text = label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = Color(0xFF2563EB)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (activeTab) {
                "HOME_DASHBOARD" -> HomeDashboardScreen(viewModel)
                "ROADMAP" -> RoadmapScreen(viewModel)
                "PRACTICE" -> PracticeLobbyScreen(viewModel)
                "COACH_CHAT" -> AICoachChatScreen(viewModel)
                "PROFILE_ANALYTICS" -> ProfileAnalyticsScreen(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. HOME DASHBOARD SCREEN
// -------------------------------------------------------------
@Composable
fun HomeDashboardScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val steps by viewModel.roadmapSteps.collectAsStateWithLifecycle()
    val apps by viewModel.applications.collectAsStateWithLifecycle()
    val readScore = viewModel.getCalculatedReadinessScore()
    val (status, desc) = viewModel.getReadinessText(readScore)

    LaunchedEffect(Unit) {
        viewModel.checkStreakAndUpdate()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello Pilot \uD83D\uDC4B",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, color = Color(0xFF94A3B8))
                    )
                    Text(
                        text = profile?.targetCareer ?: "Target Track",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                }

                // XP Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = "Active XP", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                    Text(
                        text = "${profile?.xpScore ?: 0} XP",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Streak Card & General Readiness Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "\uD83D\uDD25", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Streak", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${profile?.currentStreak ?: 1} Days",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                        )
                    }
                }

                // Core level Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val xp = profile?.xpScore ?: 0
                    val (level, desc) = when {
                        xp < 250 -> Pair("Level 1", "Beginner")
                        xp < 600 -> Pair("Level 2", "Learner")
                        xp < 1200 -> Pair("Level 3", "Practitioner")
                        xp < 2000 -> Pair("Level 4", "Advanced")
                        else -> Pair("Level 5", "Interview Pilot")
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Level Icon", tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pilot Rank", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = level, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(text = desc, fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Readiness Score Progress Guard Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Interview Readiness Score", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    "Needs Improvement" -> Color(0xFFEF4444)
                                    "Progressing" -> Color(0xFFF59E0B)
                                    "Nearly Ready" -> Color(0xFF7C3AED)
                                    else -> Color(0xFF10B981)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, fontSize = 12.sp, color = Color(0xFF6B7280))
                    }

                    // Circle score indicator
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .drawBehind {
                                drawCircle(color = Color(0xFF334155), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx()))
                                drawArc(
                                    color = if (readScore > 75) Color(0xFF10B981) else if (readScore > 40) Color(0xFFF59E0B) else Color(0xFFEF4444),
                                    startAngle = -90f,
                                    sweepAngle = (readScore / 100f) * 360f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$readScore%", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                    }
                }
            }
        }

        // Quicks Launcher Quick Panel
        item {
            Column {
                Text(text = "Upcoming Activities", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Daily Coach launcher
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.currentScreen.value = "COACH_CHAT" },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Interviews launch", tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "AI Career Coach", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Lobby interview launcher
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.currentScreen.value = "PRACTICE" },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = "Quizzes launch", tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "AI Interviewer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Active Roadmap Steps Progress List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Personalized Active Roadmap", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                TextButton(onClick = { viewModel.currentScreen.value = "ROADMAP" }) {
                    Text(text = "See All", color = Color(0xFF2563EB))
                }
            }
        }

        if (steps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Loading generated roadmap steps...", color = Color(0xFF6B7280))
                    }
                }
            }
        } else {
            items(steps.take(2)) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.phase + " Milestone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (step.status) {
                                            "MASTERED" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            "LEARNING" -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                            else -> Color(0xFF6B7280).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val tintStyle = when (step.status) {
                                    "MASTERED" -> Color(0xFF10B981)
                                    "LEARNING" -> Color(0xFF2563EB)
                                    else -> Color(0xFF94A3B8)
                                }
                                Text(text = step.status, color = tintStyle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = step.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ROADMAP SCREEN (TIMELINE VIEW)
// -------------------------------------------------------------
@Composable
fun RoadmapScreen(viewModel: MainViewModel) {
    val steps by viewModel.roadmapSteps.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Your Personalized Preparation Journey", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF94A3B8)))
                Text(text = "Study Roadmap", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
            }
            IconButton(
                onClick = { viewModel.generateNewFlashcards() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regen plan", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (steps.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(steps) { step ->
                    RoadmapStepItem(step = step, onStatusChange = { newStatus ->
                        viewModel.updateRoadmapStepStatus(step, newStatus)
                    })
                }
            }
        }
    }
}

@Composable
fun RoadmapStepItem(step: RoadmapStep, onStatusChange: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Milestone Phase: " + step.phase,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = step.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }

                // Checkbox status cycle icon indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (step.status) {
                                "MASTERED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                "LEARNING" -> Color(0xFF2563EB).copy(alpha = 0.2f)
                                else -> Color(0xFF334155)
                            }
                        )
                        .clickable {
                            val cycle = when (step.status) {
                                "NOT_STARTED" -> "LEARNING"
                                "LEARNING" -> "MASTERED"
                                else -> "NOT_STARTED"
                            }
                            onStatusChange(cycle)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val statusIcon = when (step.status) {
                        "MASTERED" -> Icons.Default.Check
                        "LEARNING" -> Icons.Default.PendingActions
                        else -> Icons.Default.RadioButtonUnchecked
                    }
                    val statusTint = when (step.status) {
                        "MASTERED" -> Color(0xFF10B981)
                        "LEARNING" -> Color(0xFF2563EB)
                        else -> Color(0xFF94A3B8)
                    }
                    Icon(imageVector = statusIcon, contentDescription = "Cycle status", tint = statusTint, modifier = Modifier.size(20.dp))
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Task Overview", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = step.description, fontSize = 13.sp, color = Color(0xFF94A3B8), lineHeight = 18.sp)

                    if (step.learningResources.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Recommended Studies", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = step.learningResources, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }

                    if (step.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "AI Chef Cheat-Sheet summary:", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = step.summary, fontSize = 12.sp, color = Color(0xFFD1D5DB), lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. PRACTICE LOBBY SCREEN (MOCK, FLASHCARD, QUIZZES)
// -------------------------------------------------------------
@Composable
fun PracticeLobbyScreen(viewModel: MainViewModel) {
    var activeSubTab by remember { mutableStateOf("MOCK") } // MOCK, FLASH, QUIZ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Preparation Operating Room", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF94A3B8)))
        Text(text = "Interactive Practice Lobby", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
        Spacer(modifier = Modifier.height(16.dp))

        // Segmented options bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(4.dp)
        ) {
            val tabs = listOf(
                Pair("MOCK", "Mock Interviews"),
                Pair("FLASH", "Flashcards"),
                Pair("QUIZ", "Assessments")
            )

            tabs.forEach { (id, label) ->
                val selected = activeSubTab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF2563EB) else Color.Transparent)
                        .clickable { activeSubTab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                "MOCK" -> InteractiveMockHub(viewModel)
                "FLASH" -> FlashcardsDeckView(viewModel)
                "QUIZ" -> AssessmentsHub(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB A: MOCK INTERVIEW LOBBY
// -------------------------------------------------------------
@Composable
fun InteractiveMockHub(viewModel: MainViewModel) {
    val historySessions by viewModel.interviewSessions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Initiate New Interview Simulation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AI conducts dynamic evaluation rounds. Speak or type answers. Results includes scoring & STAR analysis.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val rounds = listOf(
                    Triple("Technical Round", "Evaluate advanced architectures & specs.", Icons.Default.Code),
                    Triple("Behavioral Round", "Calibrates interpersonal & STAR formats.", Icons.Default.Psychology),
                    Triple("Managerial Round", "Cross-checks resolution and conflicts.", Icons.Default.Handshake),
                    Triple("Final Assessment", "Comprehensive general final rounds.", Icons.Default.School)
                )

                rounds.forEach { item ->
                    val type = item.first
                    val description = item.second
                    val icon = item.third
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.launchMockInterview(type) }
                            .testTag("launch_mock_${type.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = type, tint = Color(0xFF7C3AED))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = type, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = description, fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Active launcher", tint = Color.White)
                        }
                    }
                }
            }
        }

        Text(text = "Completed Sessions History", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

        if (historySessions.none { it.isCompleted }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No interview history found. Boot up your first round!", color = Color(0xFF6B7280), fontSize = 12.sp)
                }
            }
        } else {
            historySessions.filter { it.isCompleted }.forEach { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    var openReport by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = session.type, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = session.role, fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (session.overallScore >= 75) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${session.overallScore}% Ready",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (session.overallScore >= 75) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }

                        if (openReport) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Feedback Report", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = session.feedback, fontSize = 12.sp, color = Color(0xFFD1D5DB), lineHeight = 16.sp)
                        }

                        TextButton(
                            onClick = { openReport = !openReport },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = if (openReport) "Hide Report" else "View Report", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB B: FLASHCARDS DECK VIEW
// -------------------------------------------------------------
@Composable
fun FlashcardsDeckView(viewModel: MainViewModel) {
    val cards by viewModel.allFlashcards.collectAsStateWithLifecycle()
    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (cards.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No customized conceptual cards generated.", color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { viewModel.generateNewFlashcards() }) {
                        Text("Regenerate Flashcards")
                    }
                }
            }
        } else {
            val card = cards.getOrNull(currentCardIndex % cards.size)
            if (card != null) {
                // Deck Progress count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card ${ (currentCardIndex % cards.size) + 1 } of ${cards.size}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    IconButton(onClick = { viewModel.bookmarkFlashcard(card) }) {
                        Icon(
                            imageVector = if (card.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (card.isBookmarked) Color(0xFF2563EB) else Color.White
                        )
                    }
                }

                // Cards Box container with flipping graphics
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                    label = "CardRotation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped }
                        .testTag("interactive_flashcard"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) Color(0xFF1E293B) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Correct text rotation to prevent mirror styling on flip
                        val messageVal = if (rotation > 90f) card.answer else card.question
                        val textHeading = if (rotation > 90f) "ANSWER" else card.type.uppercase()
                        val textHeadingColor = if (rotation > 90f) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f)

                        Column(
                            modifier = Modifier
                                .graphicsLayer {
                                    if (rotation > 90f) rotationY = 180f
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = textHeading,
                                fontWeight = FontWeight.Bold,
                                color = textHeadingColor,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = messageVal,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (rotation > 90f) "Tap to see question" else "Tap card to flip answer",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        }
                    }
                }

                // Action panel (Swipe/Bookmarks indicators)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isFlipped = false
                            currentCardIndex = if (currentCardIndex > 0) currentCardIndex - 1 else cards.size - 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = "Prev")
                        Text("Previous")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.updateFlashcardMastery(card, 1)
                                isFlipped = false
                                currentCardIndex += 1
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Weak Card", tint = Color(0xFFEF4444))
                        }

                        IconButton(
                            onClick = {
                                viewModel.updateFlashcardMastery(card, 2)
                                isFlipped = false
                                currentCardIndex += 1
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Mastered Card", tint = Color(0xFF10B981))
                        }
                    }

                    Button(
                        onClick = {
                            isFlipped = false
                            currentCardIndex = (currentCardIndex + 1) % cards.size
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next")
                        Icon(imageVector = Icons.Default.NavigateNext, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB C: ASSESSMENTS / QUIZZES HUBS
// -------------------------------------------------------------
@Composable
fun AssessmentsHub(viewModel: MainViewModel) {
    val attempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val careerField = viewModel.selectedField.collectAsStateWithLifecycle().value
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Launch Assessment Exam",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select matching topic and respond multiple choice questions under pressure.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val exams = listOf(
                    Pair(careerField, "Topic Quiz covering fundamental $careerField scenarios."),
                    Pair("System Design", "Advanced high-level scaling and cached memory."),
                    Pair("Behavioral Patterns", "STAR formulation templates, leadership, conflict resolution.")
                )

                exams.forEach { (topic, description) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.startPracticeQuiz(topic) }
                            .testTag("launch_quiz_${topic.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Quiz, contentDescription = topic, tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = topic, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = description, fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = "Start quiz", tint = Color.White)
                        }
                    }
                }
            }
        }

        Text(text = "Assessment Accomplishments", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

        if (attempts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No assessments recorded yet. Study material and prompt your first test!", color = Color(0xFF6B7280), fontSize = 12.sp)
                }
            }
        } else {
            attempts.forEach { attempt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = attempt.topic, fontWeight = FontWeight.Bold, color = Color.White)
                            val scorePercent = (attempt.score.toFloat() / attempt.totalQuestions) * 100
                            Text(
                                text = "Score: ${attempt.score}/${attempt.totalQuestions} (${scorePercent.toInt()}%)",
                                color = if (scorePercent >= 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Trophy icon representing mastery
                        if (attempt.score == attempt.totalQuestions) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = Color(0xFFF59E0B))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. ACTIVE QUIZ GAME COMPONENT SCREEN
// -------------------------------------------------------------
@Composable
fun ActiveQuizScreen(viewModel: MainViewModel) {
    val topic by viewModel.activeQuizTopic.collectAsStateWithLifecycle()
    val questions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val cursorIndex by viewModel.currentQuizQuestionIndex.collectAsStateWithLifecycle()
    val activeSelection by viewModel.selectedQuizOption.collectAsStateWithLifecycle()
    val isSubmitted by viewModel.quizSubmitted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isQuizLoading.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        if (isLoading || questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Reticulating questions from AI system...", color = Color.White)
                }
            }
        } else {
            val q = questions[cursorIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Topic index
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quiz: $topic", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text(
                        text = "Question ${cursorIndex + 1} of ${questions.size}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                // Assessment question body
                Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                    LinearProgressIndicator(
                        progress = { (cursorIndex + 1) / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFF1E293B)
                    )

                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                    options.forEachIndexed { idx, option ->
                        val isPicked = activeSelection == idx
                        val isCorrectKey = q.correctIndex == idx

                        val boxColor = when {
                            isSubmitted && isCorrectKey -> Color(0xFF10B981).copy(alpha = 0.2f)
                            isSubmitted && isPicked && !isCorrectKey -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            isPicked -> Color(0xFF2563EB).copy(alpha = 0.2f)
                            else -> Color(0xFF1E293B)
                        }

                        val borderColor = when {
                            isSubmitted && isCorrectKey -> Color(0xFF10B981)
                            isSubmitted && isPicked && !isCorrectKey -> Color(0xFFEF4444)
                            isPicked -> Color(0xFF2563EB)
                            else -> Color(0xFF334155)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { viewModel.selectQuizAnswer(idx) }
                                .testTag("quiz_option_$idx"),
                            colors = CardDefaults.cardColors(containerColor = boxColor),
                            border = BorderStroke(1.5.dp, borderColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = option, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
                                if (isSubmitted && isCorrectKey) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Correct", tint = Color(0xFF10B981))
                                } else if (isSubmitted && isPicked && !isCorrectKey) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSubmitted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Explanation Icon", tint = Color(0xFF7C3AED))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "AI Review Explains:", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 12.sp)
                                    Text(text = q.explanation, fontSize = 11.sp, color = Color(0xFFD1D5DB), lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                // Controls panels
                if (!isSubmitted) {
                    Button(
                        onClick = { viewModel.submitQuizQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        enabled = activeSelection != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Submit Selection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (cursorIndex == questions.size - 1) "Finish Quiz \uD83C\uDFC1" else "Next Question",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. ACTIVE MOCK INTERVIEW CHAT (AI INTERVIEWER)
// -------------------------------------------------------------
@Composable
fun ActiveMockInterviewScreen(viewModel: MainViewModel) {
    val session by viewModel.activeInterviewSession.collectAsStateWithLifecycle()
    val messages by viewModel.activeInterviewMessages.collectAsStateWithLifecycle()
    val isAILoading by viewModel.isInterviewAILoading.collectAsStateWithLifecycle()
    val typingInput by viewModel.inputMessageText.collectAsStateWithLifecycle()

    val listState = rememberLayoutState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollTo(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.currentScreen.value = "PRACTICE" }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Exit active interview", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "AI Interfacing Simulator", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
                        Text(
                            text = (session?.type ?: "Mock session"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.completeInterviewAndAnalyze() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Finish & Grade", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                }
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Conversational stream box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (messages.isEmpty() && isAILoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Interviewer booting dialougue...", color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState.lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            )
                                        )
                                        .background(if (isUser) Color(0xFF2563EB) else Color(0xFF1E293B))
                                        .padding(14.dp)
                                        .widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (isAILoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(12.dp)
                                    ) {
                                        Text(text = "Interviewer is evaluating response...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input fields panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typingInput,
                    onValueChange = { viewModel.inputMessageText.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mock_session_input_text"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text(text = "Type your comprehensive answer here...", color = Color(0xFF6B7280), fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        viewModel.sendInterviewAnswer()
                        keyboardController?.hide()
                    })
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        viewModel.sendInterviewAnswer()
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .size(46.dp),
                    enabled = typingInput.isNotBlank() && !isAILoading
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

// Custom layout state for scrolling behavior
@Composable
fun rememberLayoutState(): LayoutState {
    val state = rememberLazyListState()
    return remember(state) { LayoutState(state) }
}

class LayoutState(val lazyListState: LazyListState) {
    suspend fun animateScrollTo(index: Int) {
        lazyListState.animateScrollToItem(index)
    }
}

// -------------------------------------------------------------
// 9. AI CAREER COACH CHAT SCREEN
// -------------------------------------------------------------
@Composable
fun AICoachChatScreen(viewModel: MainViewModel) {
    val messages by viewModel.coachMessages.collectAsStateWithLifecycle()
    val isCoachLoading by viewModel.isCoachMessageLoading.collectAsStateWithLifecycle()
    val coachInput by viewModel.inputCoachText.collectAsStateWithLifecycle()

    val listState = rememberLayoutState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollTo(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Career & STAR Strategy guidance", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF94A3B8)))
                Text(text = "AI Career Coach", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
            }

            IconButton(onClick = { viewModel.clearCoachChat() }) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear coach log", tint = Color(0xFFEF4444))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Bubble area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (messages.isEmpty()) {
                // Coach Intro Card
                Card(
                    modifier = Modifier.align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "Coach Avatar", tint = Color(0xFF2563EB), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "How can I accelerate your success?",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask me to explain tough technical topics, check your salary negotiation logic, review STAR behavioral structures, or recommend custom preparation techniques!",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState.lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "USER"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 16.dp
                                        )
                                    )
                                    .background(if (isUser) Color(0xFF7C3AED) else Color(0xFF1E293B))
                                    .padding(14.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (isCoachLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "Coach is formulating advice...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Console Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = coachInput,
                onValueChange = { viewModel.inputCoachText.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("coach_input_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text(text = "Query the Pilot AI Coach here...", color = Color(0xFF6B7280), fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.sendCoachMessage()
                    keyboardController?.hide()
                })
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    viewModel.sendCoachMessage()
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
                    .size(46.dp),
                enabled = coachInput.isNotBlank() && !isCoachLoading
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send Coach Message", tint = Color.White)
            }
        }
    }
}

// -------------------------------------------------------------
// 10. PROFILE, ATS & APPLICATION TRACKER SCREEN
// -------------------------------------------------------------
@Composable
fun ProfileAnalyticsScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val apps by viewModel.applications.collectAsStateWithLifecycle()
    val isATSAnalyzing by viewModel.isAnalyzingResume.collectAsStateWithLifecycle()
    val resumeInputText by viewModel.inputResumeText.collectAsStateWithLifecycle()

    var activeProfileSubTab by remember { mutableStateOf("TRACKER") } // TRACKER, ATS, PROFILE
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header info
        Text(text = "Job Success control room", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF94A3B8)))
        Text(text = "Profile Companion", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))

        // Tab selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(4.dp)
        ) {
            val tabsProfile = listOf(
                Pair("TRACKER", "Job Tracker"),
                Pair("ATS", "ATS Resume"),
                Pair("PROFILE", "Overview")
            )

            tabsProfile.forEach { (id, label) ->
                val selected = activeProfileSubTab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF7C3AED) else Color.Transparent)
                        .clickable { activeProfileSubTab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Display
        when (activeProfileSubTab) {
            "TRACKER" -> JobTrackerWidget(viewModel, apps)
            "ATS" -> ResumeATSWidget(viewModel, profile, isATSAnalyzing, resumeInputText)
            "PROFILE" -> CoreProfileWidget(viewModel, profile)
        }
    }
}

// JOB APPLICATION TRACKER SUB-SECTION
@Composable
fun JobTrackerWidget(viewModel: MainViewModel, apps: List<JobApplication>) {
    val company by viewModel.appCompanyInput.collectAsStateWithLifecycle()
    val role by viewModel.appRoleInput.collectAsStateWithLifecycle()
    val status by viewModel.appStatusInput.collectAsStateWithLifecycle()
    val notes by viewModel.appNotesInput.collectAsStateWithLifecycle()

    var showForm by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hiring Funnel Applications", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) Color(0xFFEF4444) else Color(0xFF2563EB))
            ) {
                Text(text = if (showForm) "Close Form" else "Log Target Application", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Add Application Target", fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = company,
                        onValueChange = { viewModel.appCompanyInput.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("app_company_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        placeholder = { Text("Company Name (e.g. Google)", color = Color.Gray, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { viewModel.appRoleInput.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("app_role_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        placeholder = { Text("Position Role (e.g. Software Engineer)", color = Color.Gray, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.appNotesInput.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        placeholder = { Text("General notes (e.g. references, timeline)", color = Color.Gray, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Stage select Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val stages = listOf("Applied", "Interviews", "Offer", "Rejected")
                        stages.forEach { stage ->
                            val activeVal = status == stage
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeVal) Color(0xFF2563EB) else Color(0xFF0F172A))
                                    .clickable { viewModel.appStatusInput.value = stage }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = stage, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.addJobApplication()
                            showForm = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Add target", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List Applications
        if (apps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Your Funnel is currently empty. Log job targets to monitor callbacks effectively!",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            apps.forEach { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = app.company, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(text = app.role, fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (app.status) {
                                                "Offer" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                "Rejected" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                "Interviews" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                else -> Color(0xFF2563EB).copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = app.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (app.status) {
                                            "Offer" -> Color(0xFF10B981)
                                            "Rejected" -> Color(0xFFEF4444)
                                            "Interviews" -> Color(0xFFF59E0B)
                                            else -> Color(0xFF2563EB)
                                        }
                                    )
                                }

                                IconButton(onClick = { viewModel.removeJobApplication(app) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete App", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (app.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Notes: " + app.notes, fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}

// RESUME ATS SUITE SUB-SECTION
@Composable
fun ResumeATSWidget(viewModel: MainViewModel, profile: UserProfile?, isAnalyzing: Boolean, resumeText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "AI ATS Resume Analyzer", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Paste text contents of your professional PDF/Word document below. Our ATS auditor scans keywords and calculates suitability metrics dynamically.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = resumeText,
                    onValueChange = { viewModel.inputResumeText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("resume_input_text_field"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    placeholder = { Text("Paste professional resume text here...", color = Color.Gray, fontSize = 12.sp) }
                )

                Button(
                    onClick = { viewModel.evaluateResumeATS() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    enabled = resumeText.isNotBlank() && !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.FactCheck, contentDescription = "ATS audit", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Scan ATS Compliance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (profile != null && profile.resumeScore > 0) {
            Text(text = "ATS Scanner Diagnostic Verdict", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Compliance Score achieved:", fontWeight = FontWeight.Bold, color = Color.White)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "${profile.resumeScore}% Approved", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = Color(0xFF334155))

                    Text(text = "ATS Recommendations:", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 13.sp)
                    Text(text = profile.resumeFeedback, color = Color(0xFFE2E8F0), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

// PROFILE CONGO OVERVIEW SUB-SECTION
@Composable
fun CoreProfileWidget(viewModel: MainViewModel, profile: UserProfile?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ContactPage, contentDescription = "Card layout", tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Curriculum Parameters", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Active Track:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(text = profile?.targetCareer ?: "None", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Experience level:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(text = profile?.experienceLevel ?: "None", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Assigned Timeline:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(text = "${profile?.prepDurationDays ?: 30} Days pacing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Rank metrics:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(text = "${profile?.xpScore ?: 0} XP Total", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Diagnostic information cards explaining systems features
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Interview Pilot Leveling Perks", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Accumulate XP by studying milestones (+15), completing quiz assessments (+40), conducting mock speech evaluation (+60), and scanning resume ATS metrics (+50). Unlock rank statuses as you prepare to launch your career!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
