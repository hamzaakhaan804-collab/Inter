package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    internId: String,
    dao: InternIqDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true
) {
    val internState by dao.getInternByIdFlow(internId).collectAsState(initial = null)
    val tasksState by dao.getTasksForInternFlow(internId).collectAsState(initial = emptyList())
    val logsState by dao.getWorkLogsForInternFlow(internId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intern Analytical Roster", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        val intern = internState
        if (intern == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Scaffold
        }

        // Calculations
        val totalTasks = tasksState.size
        val doneTasks = tasksState.count { it.status.lowercase() == "done" }
        val completionRate = if (totalTasks > 0) (doneTasks * 100) / totalTasks else 0
        
        val overallHours = logsState.sumOf { it.sessionHours }
        val averageTaskSpeed = if (doneTasks > 0) String.format("%.1f", overallHours / doneTasks) else "0.0"
        
        // Find Today's Log Hours
        val todayStr = LocalDate.now().toString()
        val todayLog = logsState.find { it.date == todayStr }

        // Start -> End date calculations
        val startDate = try { LocalDate.parse(intern.startDate) } catch(e: Exception) { LocalDate.now().minusMonths(3) }
        val endDate = try { LocalDate.parse(intern.endDate) } catch(e: Exception) { LocalDate.now().plusMonths(3) }
        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1)
        val daysElapsed = ChronoUnit.DAYS.between(startDate, today).coerceIn(0, totalDays)
        val daysRemaining = (totalDays - daysElapsed).coerceAtLeast(0)
        val elapsedRatio = daysElapsed.toFloat() / totalDays.toFloat()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // SECTION 1: HEADER CARD
            item {
                ProfileHeaderSection(intern = intern)
            }

            // SECTION 2: DURATION METRIC
            item {
                DurationProgressSection(
                    startDate = intern.startDate,
                    endDate = intern.endDate,
                    daysRemaining = daysRemaining,
                    progress = elapsedRatio
                )
            }

            // SECTION 3: CLOCK IN STATUS
            item {
                TodayClockInStatusSection(todayLog = todayLog)
            }

            // SECTION 4: DETAILED PERFORMANCE SUB-SCORES
            item {
                PerformanceIndexWidget(
                    score = intern.score,
                    completionRate = completionRate,
                    avgSpeed = averageTaskSpeed,
                    consistency = if (intern.score > 70) 86 else 52,
                    trend = intern.trend
                )
            }

            // SECTION 5: SOURCE CREDIBILITY
            item {
                CredibilityRadarPanel(intern = intern)
            }

            // SECTION 6: AI INSIGHT DETAILED VIEW
            item {
                AiInsightCardSection(insightText = intern.aiInsight)
            }

            // SECTION 7: CONTRADICTION ALERT (Conditionally Visible)
            if (intern.status.lowercase() == "idle" && intern.id == "intern_ali") {
                item {
                    ContradictionBoxWidget(
                        title = "CONTRADICTION DETECTED: Task vs Clock",
                        flag = "CONTRADICTION_A_TASK_CLOCK",
                        desc = "Task 'Design Mockup' was submitted Done, but corresponding Clock Record registers 0.0 hrs today. Continuous Live Feed logged 26h idle.",
                        resolution = "Work Clock Record credibility index (0.90) overrides user-updated task log (0.80). Decision: Task placed 'Under Review', overall profile penalized."
                    )
                }
            } else if (intern.status.lowercase() == "at_risk" && intern.id == "intern_hamza") {
                item {
                    ContradictionBoxWidget(
                        title = "CONTRADICTION DETECTED: Dashboard Stale",
                        flag = "CONTRADICTION_B_STALE_DASHBOARD",
                        desc = "Static Dashboard register overall score 72 (Updated 3 days ago). However, continuous Real-time Activity feed registers 38h Idle.",
                        resolution = "Telemetry override applied. Real-time Activity Feed credibility index (0.95) overrides Static Dashboard metrics (0.60). Live adjusted score: 61."
                    )
                }
            }

            // SECTION 8: PREDICTED OUTCOMES (Visible only for at_risk/idle)
            if (intern.status.lowercase() == "idle" || intern.status.lowercase() == "at_risk") {
                item {
                    PredictionFuturesSection(
                        internName = intern.name,
                        deadlineToDate = intern.endDate,
                        futureA = "If ${intern.name} does not resume logging by tomorrow, Sprint delivery fails by an estimated 3.5 days. Overall confidence factor: 78%.",
                        futureB = "If mentor checks in today, re-assigns alternate tasks to redistribute, recovery prediction index succeeds with 82% confidence."
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(intern: Intern) {
    val initials = intern.name.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase()
    val statusColors = getStatusColorPalette(intern.status)

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColors.first,
                    border = BorderStroke(0.5.dp, statusColors.second)
                ) {
                    Text(
                        text = intern.status.replace("_", " ").uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = statusColors.second,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = intern.iid,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = PrimaryBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = intern.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = intern.headline,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(
                text = "Track Department: ${intern.department}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupervisorAccount,
                    contentDescription = "Mentor Name",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Assigned Mentor: ${intern.mentor}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DurationProgressSection(startDate: String, endDate: String, daysRemaining: Long, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "INTERNSHIP TIMELINE TRACK",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Start: $startDate", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = "$daysRemaining Days Remaining",
                    fontSize = 11.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "End: $endDate", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = PrimaryBlue,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun TodayClockInStatusSection(todayLog: WorkLog?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Clock status",
                    tint = if (todayLog != null) StatusGreen else StatusRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CLOCK LOG METRICS",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (todayLog != null) "Active Logging Status" else "Absent (Not clocked in)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (todayLog != null) StatusGreen else StatusRed
                    )
                }
            }
            if (todayLog != null) {
                Text(
                    text = "+${todayLog.sessionHours} hrs today",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = StatusGreen
                )
            }
        }
    }
}

@Composable
fun PerformanceIndexWidget(score: Int, completionRate: Int, avgSpeed: String, consistency: Int, trend: String) {
    val trendColor = if (trend.lowercase() == "up") StatusGreen else StatusRed
    val trendIcon = if (trend.lowercase() == "up") Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PERFORMANCE ANALYTICS INDEX",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = score.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = trendColor)
                    Text(text = "Overall Score", fontSize = 11.sp, color = Color.Gray)
                }
                
                Column(modifier = Modifier.width(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PerformanceValueRow(label = "Milestone Completions", value = "$completionRate%")
                    PerformanceValueRow(label = "Avg. Task Speed", value = "$avgSpeed hrs")
                    PerformanceValueRow(label = "Consistency Ratio", value = "$consistency%")
                }
            }
        }
    }
}

@Composable
fun PerformanceValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CredibilityRadarPanel(intern: Intern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TELEMETRY SOURCE CREDIBILITY INDEX",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CredibilityMeterRow(source = "Activity Feed", score = 0.95, freshStr = "Live stream active")
            CredibilityMeterRow(source = "Work Clock Logs", score = 0.90, freshStr = "Synced today")
            CredibilityMeterRow(source = "Tasks Metadata", score = 0.80, freshStr = "User updated")
            CredibilityMeterRow(
                source = "Performance Dashboard", 
                score = 0.60, 
                freshStr = if (intern.id == "intern_ali") "Stale" else "Calculated"
            )
            CredibilityMeterRow(source = "Mentor Feedbacks", score = 0.50, freshStr = "Subjective")
        }
    }
}

@Composable
fun CredibilityMeterRow(source: String, score: Double, freshStr: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = source,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(130.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = score.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(CircleShape),
            color = PrimaryBlue,
            trackColor = Color.LightGray.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "[${score}] $freshStr",
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AiInsightCardSection(insightText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PrimaryBlue),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Eye",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SUPERVISOR AI INSIGHT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = insightText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContradictionBoxWidget(title: String, flag: String, desc: String, resolution: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, StatusPurple),
        colors = CardDefaults.cardColors(containerColor = StatusPurple.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Conflict Warning",
                    tint = StatusPurple,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = StatusPurple
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trace Flag: $flag",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = StatusPurple
            )
            Divider(modifier = Modifier.padding(vertical = 10.dp), color = StatusPurple.copy(alpha = 0.2f))
            Text(
                text = "Resolution Action Chain:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Black
            )
            Text(
                text = resolution,
                fontSize = 11.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PredictionFuturesSection(internName: String, deadlineToDate: String, futureA: String, futureB: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AGENT PREDICTED OUTCOMES (ACTION SIMULATION)",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Future A
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.05f)),
                    border = BorderStroke(0.5.dp, StatusRed.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FUTURE A (No action)",
                            color = StatusRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = futureA,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Future B
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.05f)),
                    border = BorderStroke(0.5.dp, StatusGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FUTURE B (Remedied)",
                            color = StatusGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = futureB,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

fun getStatusColorPalette(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "on_track" -> Pair(StatusGreen.copy(alpha = 0.12f), StatusGreen)
        "idle" -> Pair(StatusRed.copy(alpha = 0.12f), StatusRed)
        "at_risk" -> Pair(StatusAmber.copy(alpha = 0.12f), StatusAmber)
        else -> Pair(StatusPurple.copy(alpha = 0.12f), StatusPurple)
    }
}
