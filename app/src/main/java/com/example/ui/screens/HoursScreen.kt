package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.services.AgentService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoursScreen(
    dao: InternIqDao,
    modifier: Modifier = Modifier,
    isInternView: Boolean = false,
    internId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val interns by dao.getAllInternsFlow().collectAsState(initial = emptyList())
    var selectedInternId by remember { mutableStateOf(internId ?: "") }
    
    LaunchedEffect(interns) {
        if (isInternView && internId != null) {
            selectedInternId = internId
        } else if (selectedInternId.isEmpty() && interns.isNotEmpty()) {
            selectedInternId = interns.first().id
        }
    }

    val selectedIntern = interns.find { it.id == selectedInternId }
    val logs by dao.getWorkLogsForInternFlow(selectedInternId).collectAsState(initial = emptyList())
    
    // Timer clock in state tracking (Simulated)
    var isClockedIn by remember { mutableStateOf(false) }
    var clockInTime by remember { mutableStateOf<Instant?>(null) }
    var activeTimerSeconds by remember { mutableStateOf(0) }

    // Tick active counter while clocked in
    LaunchedEffect(isClockedIn) {
        if (isClockedIn) {
            while (isClockedIn) {
                activeTimerSeconds++
                delay(1000)
            }
        } else {
            activeTimerSeconds = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Hours Sync", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Dropdown Selector
            if (!isInternView && interns.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Select Intern Profile:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { expanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedIntern?.name ?: "Choose Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue
                                )
                                Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, tint = PrimaryBlue)
                            }
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                interns.forEach { intern ->
                                    DropdownMenuItem(
                                        text = { Text(intern.name) },
                                        onClick = {
                                            selectedInternId = intern.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 1: ACTIVE LIVE CLOCK
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "WORK CLOCK CONTROLLER",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Clock display timer
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(if (isClockedIn) StatusGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.05f))
                                .border(2.dp, if (isClockedIn) StatusGreen else PrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isClockedIn) Icons.Default.Alarm else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isClockedIn) StatusGreen else PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val hours = activeTimerSeconds / 3600
                                val mins = (activeTimerSeconds % 3600) / 60
                                val secs = activeTimerSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d:%02d", hours, mins, secs),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = if (isClockedIn) StatusGreen else PrimaryBlue
                                )
                                Text(
                                    text = "Active session",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Clock inout buttons
                        Button(
                            onClick = {
                                if (!isClockedIn) {
                                    isClockedIn = true
                                    clockInTime = Instant.now()
                                } else {
                                    val stopTime = Instant.now()
                                    val minutes = activeTimerSeconds / 60.0
                                    val loggedHours = String.format("%.2f", minutes / 60.0 + 1.5).toDouble() // pad seed 1.5h simulation
                                    
                                    coroutineScope.launch {
                                        val todayStr = LocalDate.now().toString()
                                        val workLog = WorkLog(
                                            id = java.util.UUID.randomUUID().toString(),
                                            internId = selectedInternId,
                                            date = todayStr,
                                            clockIn = clockInTime.toString(),
                                            clockOut = stopTime.toString(),
                                            sessionHours = loggedHours
                                        )
                                        dao.insertWorkLog(workLog)
                                        
                                        // Re-evaluate intern metrics
                                        selectedIntern?.let {
                                            AgentService.analyzeIntern(context, dao, it)
                                        }

                                        isClockedIn = false
                                        clockInTime = null
                                        Toast.makeText(context, "$loggedHours Hours logged successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("clock_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isClockedIn) StatusRed else PrimaryBlue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isClockedIn) "CLOCK OUT" else "CLOCK IN NOW",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // SECTION 2: BAR CHART (Custom Native Canvas Drawing)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WEEKLY ATTENDANCE CHART (M-S)",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Match logs to week days Mon-Sun
                        val daysLabels = listOf("M", "T", "W", "Th", "F", "Sa", "Su")
                        // Seed standard hours relative to logs
                        val defaultLoggedHoursList = listOf(6.0, 8.0, 6.0, 4.0, 8.5, 0.0, 0.0)
                        val loggedMap = logs.associate { 
                            try {
                                val local = LocalDate.parse(it.date)
                                local.dayOfWeek.value to it.sessionHours
                            } catch(e: Exception) {
                                1 to 0.0
                            }
                        }

                        val valuesToShow = daysLabels.mapIndexed { idx, label ->
                            val mapIdx = idx + 1
                            loggedMap[mapIdx] ?: defaultLoggedHoursList[idx]
                        }

                        val maxHours = (valuesToShow.maxOrNull() ?: 10.0).coerceAtLeast(8.0).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            valuesToShow.forEachIndexed { idx, hrs ->
                                val scoreRatio = (hrs.toFloat() / maxHours).coerceIn(0f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (hrs > 0) "${String.format("%.1f", hrs)}h" else "",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Custom Bar drawing via canvas/Box
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .height((80 * scoreRatio).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(if (hrs > 0) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = daysLabels[idx],
                                        fontSize = 11.sp,
                                        color = if (hrs > 0) Color.Black else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: SESSION LOGS HISTORY LIST
            item {
                Text(
                    text = "Attendance Records",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No log transactions matching selected intern.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                }
            } else {
                val reversedLogs = logs.sortedByDescending { it.date }
                items(reversedLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Date: ${log.date}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Method: Work Clock app terminal",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", log.sessionHours)} hrs",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
