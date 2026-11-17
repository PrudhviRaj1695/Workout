package com.ironwill.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.PermissionController
import kotlinx.coroutines.launch

private val ReportOrange = Color(0xFFFF8A24)
private val ReportMuted = Color(0xFF939EAA)
private val ReportWhite = Color(0xFFF5F7FA)
private val ReportPanel = Color(0xFF141920)

@Composable
fun DailyReportScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var connected by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var providerAvailable by remember { mutableStateOf(true) }
    var report by remember { mutableStateOf<DailyHealthReport?>(null) }

    suspend fun refresh() {
        loading = true
        try {
            val client = HealthConnectClient.getOrCreate(context)
            connected = client.permissionController.getGrantedPermissions().containsAll(HealthConnectRepository.permissions)
            if (connected) report = HealthConnectRepository.loadToday(context)
        } catch (_: Exception) {
            providerAvailable = false
        } finally {
            loading = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grants ->
        connected = grants.containsAll(HealthConnectRepository.permissions)
        if (connected) scope.launch { refresh() }
    }

    LaunchedEffect(Unit) {
        providerAvailable = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        if (providerAvailable) refresh()
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp)) {
                Text("IRON WILL", color = ReportOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.4.sp)
                Text("Daily Health Report", color = ReportWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("REAL DATA · DAILY INTELLIGENCE", color = ReportMuted, fontSize = 11.sp)
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ReportPanel).padding(18.dp)) {
                Text("TODAY'S HEALTH BRIEF", color = ReportOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(report?.date?.toString() ?: "Health data connection", color = ReportWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                when {
                    !providerAvailable -> Text("Health Connect is not available. Install or update Health Connect, then try again.", color = ReportMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                    !connected -> {
                        Text("Connect once to read steps, distance, calories, sleep, weight and exercise records shared by your health apps.", color = ReportMuted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp))
                        Button(onClick = { permissionLauncher.launch(HealthConnectRepository.permissions) }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp), colors = ButtonDefaults.buttonColors(containerColor = ReportOrange)) {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                            Text("CONNECT HEALTH DATA", color = Color(0xFF080A0D), fontWeight = FontWeight.Bold)
                        }
                    }
                    loading -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                        CircularProgressIndicator(color = ReportOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Reading today's health records…", color = ReportMuted, modifier = Modifier.padding(start = 12.dp))
                    }
                    else -> {
                        Text("Generated from records available on this device. Unavailable fields are not estimated.", color = ReportMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                        TextButton(onClick = { scope.launch { refresh() } }, modifier = Modifier.align(Alignment.End)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = ReportOrange)
                            Text("REFRESH", color = ReportOrange)
                        }
                    }
                }
            }
        }
        if (report != null) {
            val r = report!!
            item { ReportSectionTitle("MOVEMENT") }
            item {
                Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetric("STEPS", r.steps?.toString() ?: "—", Modifier.weight(1f))
                    ReportMetric("DISTANCE", r.distanceKm?.let { String.format("%.2f km", it) } ?: "—", Modifier.weight(1f))
                    ReportMetric("ACTIVE KCAL", r.activeCalories?.let { String.format("%.0f", it) } ?: "—", Modifier.weight(1f))
                }
            }
            item { ReportSectionTitle("RECOVERY & TRAINING") }
            item {
                Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportMetric("SLEEP", r.sleepHours?.let { String.format("%.1f h", it) } ?: "—", Modifier.weight(1f))
                    ReportMetric("WORKOUTS", r.workouts?.toString() ?: "—", Modifier.weight(1f))
                    ReportMetric("WEIGHT", r.latestWeightKg?.let { String.format("%.1f kg", it) } ?: "—", Modifier.weight(1f))
                }
            }
            item {
                Column(Modifier.padding(18.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(ReportPanel).padding(17.dp)) {
                    Text("COACH'S DAILY READOUT", color = ReportOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    val movement = when {
                        r.steps == null -> "Movement: step data is unavailable. Check that your step source shares records with Health Connect."
                        r.steps >= 10000 -> "Movement: you reached the 10,000-step reference target. Maintain comfortable, consistent activity."
                        else -> "Movement: ${r.steps} steps recorded. If you feel well, add a short comfortable walk to build your daily total."
                    }
                    Text(movement, color = ReportWhite, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp))
                    val recovery = when {
                        r.sleepHours == null -> "Recovery: sleep records are unavailable. Add a sleep source to Health Connect."
                        r.sleepHours < 7.0 -> "Recovery: recorded sleep was below 7 hours. Consider an earlier wind-down and reduce training intensity if you feel fatigued."
                        else -> "Recovery: recorded sleep was ${String.format("%.1f", r.sleepHours)} hours. Compare this with how rested you feel."
                    }
                    Text(recovery, color = ReportWhite, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp))
                    Text(
                        if (r.workouts == null) "Training: exercise records are unavailable." else "Training: ${r.workouts} exercise session(s) recorded today.",
                        color = ReportWhite, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp)
                    )
                    if (r.missingSources.isNotEmpty()) {
                        Text("DATA GAPS: ${r.missingSources.distinct().joinToString()}", color = Color(0xFFFFB547), fontSize = 10.sp, modifier = Modifier.padding(top = 14.dp))
                    }
                }
            }
        }
        item {
            Text("REPORT LIMITATIONS", color = ReportMuted, fontSize = 10.sp, letterSpacing = 1.4.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp))
            Text("This is a wellness summary, not a medical diagnosis. Health Connect only returns records shared by connected apps and devices. Missing fields mean no usable record was available.", color = ReportMuted, fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 22.dp))
        }
    }
}

@Composable
private fun ReportSectionTitle(title: String) {
    Text(title, color = ReportOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(start = 22.dp, top = 20.dp, bottom = 8.dp))
}

@Composable
private fun ReportMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(ReportPanel).padding(12.dp)) {
        Text(label, color = ReportMuted, fontSize = 9.sp, letterSpacing = 0.8.sp)
        Text(value, color = ReportWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
    }
}
