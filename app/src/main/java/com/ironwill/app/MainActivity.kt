package com.ironwill.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080A0D)
private val Panel = Color(0xFF141920)
private val Raised = Color(0xFF1B222B)
private val Orange = Color(0xFFFF8A24)
private val Muted = Color(0xFF939EAA)
private val White = Color(0xFFF5F7FA)
private val Green = Color(0xFF36D399)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IronWillApp() }
    }
}

private enum class Tab(val label: String) { Home("Home"), Missions("Missions"), Coach("AI Coach"), Insights("Insights"), Profile("Profile") }

@Composable
fun IronWillApp() {
    var tab by remember { mutableStateOf(Tab.Home) }
    var showWorkout by remember { mutableStateOf(false) }
    var workoutRunning by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("English") }
    var personality by remember { mutableStateOf("Scientific Coach") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Orange, onPrimary = Bg,
        onBackground = White, onSurface = White, secondary = Muted
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (showWorkout) {
                WorkoutScreen(
                    running = workoutRunning,
                    onToggle = { workoutRunning = !workoutRunning },
                    onFinish = { showWorkout = false; workoutRunning = false }
                )
            } else {
                Scaffold(
                    containerColor = Bg,
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF0E1218), contentColor = White) {
                            Tab.entries.forEach { item ->
                                val icon = when (item) {
                                    Tab.Home -> Icons.Default.Dashboard
                                    Tab.Missions -> Icons.Default.Flag
                                    Tab.Coach -> Icons.Default.AutoAwesome
                                    Tab.Insights -> Icons.Default.QueryStats
                                    Tab.Profile -> Icons.Default.Person
                                }
                                NavigationBarItem(
                                    selected = tab == item,
                                    onClick = { tab = item },
                                    icon = { Icon(icon, contentDescription = item.label) },
                                    label = { Text(item.label, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Orange, selectedTextColor = Orange,
                                        indicatorColor = Raised, unselectedIconColor = Muted, unselectedTextColor = Muted
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            Tab.Home -> HomeScreen(onStart = { showWorkout = true }, onMissions = { tab = Tab.Missions })
                            Tab.Missions -> MissionsScreen(onStart = { showWorkout = true })
                            Tab.Coach -> CoachScreen(personality, { personality = it }, language, { language = it })
                            Tab.Insights -> InsightsScreen()
                            Tab.Profile -> ProfileScreen(language, { language = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("IRON WILL", color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.4.sp)
            Text(title, color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Box(Modifier.size(42.dp).clip(CircleShape).background(Raised), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = White)
        }
    }
}

@Composable
private fun HomeScreen(onStart: () -> Unit, onMissions: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Command Center", "TUESDAY  ·  YOUR NEXT REP STARTS NOW") }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(26.dp)).background(Panel).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DAILY MOVEMENT", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
                    Surface(color = Color(0xFF282019), shape = CircleShape) { Text("NOT CONNECTED", color = Orange, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 14.dp)) {
                    Box(Modifier.size(158.dp), contentAlignment = Alignment.Center) {
                        ActivityRing(0f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("—", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("STEPS", fontSize = 10.sp, color = Muted, letterSpacing = 1.5.sp)
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text("10,000", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("daily step target", color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(14.dp))
                        Text("Connect Health Connect to show your real activity.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
                Divider(color = Raised)
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("ACTIVE KCAL", "—")
                    Metric("DISTANCE", "—")
                    Metric("MOVE TIME", "—")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TODAY'S PRIORITY", color = Orange, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
                    Text("Build the habit. Then build the body.", color = White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp).clip(RoundedCornerShape(22.dp)).background(Raised).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FitnessCenter, tint = Orange, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("STRENGTH SESSION", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("35–45 MIN", color = Muted, fontSize = 10.sp)
                }
                Text("Controlled full-body foundation", color = White, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
                Text("A conservative session template. Review readiness and pain before starting.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("START SESSION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("COACH INTEL", color = Muted, fontSize = 11.sp, letterSpacing = 1.8.sp, modifier = Modifier.weight(1f))
                Text("LIVE GUIDANCE", color = Green, fontSize = 9.sp)
            }
            Row(Modifier.padding(horizontal = 18.dp).clip(RoundedCornerShape(20.dp)).background(Panel).padding(16.dp)) {
                Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF302219)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, tint = Orange, contentDescription = "Coach")
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Discipline is a system, not a mood.", color = White, fontWeight = FontWeight.SemiBold)
                    Text("Start with today's check-in. Your plan should adapt to sleep, energy, soreness and pain.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
        item {
            Text("MISSION CONTROL  →", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
                modifier = Modifier.clickable { onMissions() }.padding(22.dp))
        }
    }
}

@Composable
private fun ActivityRing(progress: Float) {
    val animated by animateFloatAsState(progress, label = "ring")
    Canvas(Modifier.fillMaxSize()) {
        val stroke = 13.dp.toPx()
        drawArc(Raised, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round), size = Size(size.width, size.height))
        if (animated > 0f) drawArc(Orange, -90f, 360f * animated, false, style = Stroke(stroke, cap = StrokeCap.Round), size = Size(size.width, size.height))
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun MissionsScreen(onStart: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Mission Control", "SMALL ACTIONS. COMPOUNDED DAILY.") }
        val missions = listOf(
            Triple("Morning movement", "10 min · Easy", "Prime joints and raise alertness"),
            Triple("Walking target", "Build through the day", "Accumulate comfortable movement"),
            Triple("Strength foundation", "35–45 min · Moderate", "Technique-first full-body training"),
            Triple("Mobility reset", "8 min · Easy", "Restore comfortable range of motion")
        )
        items(missions.size) { index ->
            val m = missions[index]
            Column(Modifier.padding(horizontal = 18.dp, vertical = 7.dp).clip(RoundedCornerShape(20.dp)).background(Panel).padding(17.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("0${index + 1}", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(m.first, color = White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(m.second, color = Muted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, tint = Muted, contentDescription = null)
                }
                Text(m.third, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 32.dp, top = 8.dp))
                if (index == 2) {
                    TextButton(onClick = onStart, modifier = Modifier.align(Alignment.End)) { Text("START SESSION", color = Orange, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun CoachScreen(personality: String, onPersonality: (String) -> Unit, language: String, onLanguage: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("AI Coach Studio", "YOUR COACHING STYLE. YOUR RULES.") }
        item {
            Column(Modifier.padding(horizontal = 18.dp).clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
                Box(Modifier.size(62.dp).clip(CircleShape).background(Color(0xFF302219)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, tint = Orange, modifier = Modifier.size(30.dp), contentDescription = "AI core")
                }
                Text("Coach interface preview", color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                Text("This build contains local coaching guidance. A live LLM connection is not configured yet.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            Text("PERSONALITY PROFILE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 8.dp))
            listOf("Military Commander", "Brutally Honest", "Scientific Coach", "Friendly Companion", "Stoic Mentor", "Recovery Coach").forEach { name ->
                Row(Modifier.fillMaxWidth().clickable { onPersonality(name) }.padding(horizontal = 22.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = if (personality == name) Orange else White, modifier = Modifier.weight(1f))
                    if (personality == name) Icon(Icons.Default.CheckCircle, tint = Orange, contentDescription = "Selected")
                }
            }
        }
        item {
            Text("LANGUAGE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(22.dp))
            Row(Modifier.padding(horizontal = 18.dp)) {
                listOf("English", "తెలుగు").forEach { lang ->
                    FilterChip(selected = language == lang, onClick = { onLanguage(lang) }, label = { Text(lang) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
        item {
            OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Ask your coach") }, modifier = Modifier.fillMaxWidth().padding(18.dp), minLines = 2)
            Button(onClick = { prompt = "" }, modifier = Modifier.padding(horizontal = 18.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("CLEAR MESSAGE", color = Bg) }
        }
    }
}

@Composable
private fun InsightsScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Health Intelligence", "MEASURE WHAT MATTERS. TRUST THE DATA.") }
        item {
            Column(Modifier.padding(18.dp).clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
                Text("YOUR BASELINE", color = Orange, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Text("No health data connected", color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                Text("Charts will appear after a supported health data source is connected. No sample values are presented as your personal measurements.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 7.dp))
            }
        }
        listOf("Daily activity", "Weekly steps", "Weight progression", "Sleep trend", "Training consistency").forEach { label ->
            item {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = White, modifier = Modifier.weight(1f))
                    Text("AWAITING DATA", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(language: String, onLanguage: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { Header("System Profile", "CONFIGURATION & DATA SOURCES") }
        item {
            Column(Modifier.padding(18.dp).clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
                Text("HEALTH DATA", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text("Health Connect integration is not active in this first build.", color = White, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
                Text("Step counts, sleep and weight remain blank until a real data source is connected.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
        item {
            Text("PREFERENCES", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(22.dp))
            Text("Language: $language", color = White, modifier = Modifier.padding(horizontal = 22.dp))
            Row(Modifier.padding(18.dp)) {
                listOf("English", "తెలుగు").forEach { lang -> FilterChip(selected = language == lang, onClick = { onLanguage(lang) }, label = { Text(lang) }, modifier = Modifier.padding(end = 8.dp)) }
            }
        }
        item {
            Text("IRON WILL · VERSION 1.0.0", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(22.dp))
        }
    }
}

@Composable
private fun WorkoutScreen(running: Boolean, onToggle: () -> Unit, onFinish: () -> Unit) {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        while (running) { kotlinx.coroutines.delay(1000); seconds++ }
    }
    Column(Modifier.fillMaxSize().background(Bg).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFinish) { Icon(Icons.Default.Close, contentDescription = "Exit workout", tint = White) }
            Text("ACTIVE MISSION", color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        Spacer(Modifier.height(55.dp))
        Text("STRENGTH FOUNDATION", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Text("Session timer", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Text(String.format("%02d:%02d", seconds / 60, seconds % 60), color = Orange, fontSize = 64.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
        Text(if (running) "SESSION IN PROGRESS" else "SESSION PAUSED", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(50.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(20.dp)) {
            Text("TRAINING NOTE", color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Prioritize pain-free range and controlled repetitions. Stop if you experience sharp pain, instability, dizziness or unusual breathlessness.", color = White, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 10.dp))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onToggle, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(16.dp)) {
            Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
            Text(if (running) "PAUSE SESSION" else "RESUME SESSION", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onFinish, modifier = Modifier.padding(top = 8.dp)) { Text("FINISH SESSION", color = Muted) }
    }
}
