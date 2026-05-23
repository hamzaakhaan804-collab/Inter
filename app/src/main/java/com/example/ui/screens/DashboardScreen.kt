package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.services.AgentService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dao: InternIqDao,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToAddIntern: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val interns by dao.getAllInternsFlow().collectAsState(initial = emptyList())
    val traces by dao.getAllTracesFlow().collectAsState(initial = emptyList())
    
    var isScanning by remember { mutableStateOf(false) }
    var scanStatusText by remember { mutableStateOf("") }
    
    // Stats Calculations
    val totalCount = interns.size
    val idleCount = interns.count { it.status.lowercase() == "idle" }
    val riskCount = interns.count { it.status.lowercase() == "at_risk" }
    val onTrackCount = interns.count { it.status.lowercase() == "on_track" }
    val onTrackPercentage = if (totalCount > 0) (onTrackCount * 100) / totalCount else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = PrimaryBlue,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "InternIQ Console",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isScanning = true
                                AgentService.runMonitoringCycle(context) { status ->
                                    scanStatusText = status
                                }
                                delay(800)
                                isScanning = false
                                Toast.makeText(context, "Scanning completed safely!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("scan_button"),
                        enabled = !isScanning
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan All source feeds",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddIntern,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("add_intern_fab")
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add New Remote Intern Profile"
                )
            }
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Overlay
                item {
                    AnimatedVisibility(
                        visible = isScanning,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusPurple.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, StatusPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = StatusPurple,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = scanStatusText,
                                    color = StatusPurple,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Stats Dashboard Header
                item {
                    StatsHeader(
                        total = totalCount,
                        idle = idleCount,
                        atRisk = riskCount,
                        onTrackPercentage = onTrackPercentage
                    )
                }

                // Header title
                item {
                    Text(
                        text = "Supervised Intern Roster",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (interns.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No interns added yet. Use FAB to create positive team profiles.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(interns, key = { it.id }) { intern ->
                        InternCard(
                            intern = intern,
                            onClick = { onNavigateToProfile(intern.id) }
                        )
                    }
                }

                // Agent Traces Summary Row
                item {
                    Text(
                        text = "Latest Agent Actions Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                val recentTraces = traces.take(5)
                if (recentTraces.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No execution history yet.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(recentTraces) { trace ->
                        TraceSummaryItem(trace = trace)
                    }
                    // Bottom breathing room
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun StatsHeader(total: Int, idle: Int, atRisk: Int, onTrackPercentage: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ORGANIZATION METRICS SUMMARY",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Supervised", value = total.toString(), modifier = Modifier.weight(1f))
                StatItem(label = "Idle Flags", value = idle.toString(), color = StatusRed, modifier = Modifier.weight(1f))
                StatItem(label = "At Risk", value = atRisk.toString(), color = StatusAmber, modifier = Modifier.weight(1f))
                StatItem(label = "On Track", value = "$onTrackPercentage%", color = StatusGreen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InternCard(intern: Intern, onClick: () -> Unit) {
    val initials = intern.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
    val badgeColors = getStatusBadgeColor(intern.status)
    val trendIcon = when (intern.trend.lowercase()) {
        "up" -> Icons.Default.ArrowUpward
        "down" -> Icons.Default.ArrowDownward
        else -> Icons.Default.TrendingFlat
    }
    val trendColor = when (intern.trend.lowercase()) {
        "up" -> StatusGreen
        "down" -> StatusRed
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("intern_card_${intern.id}"),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Avatar Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = intern.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${intern.department} • ${intern.iid}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Score + Trend
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = "Trend direction",
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = intern.score.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(text = "Score Index", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColors.first,
                    modifier = Modifier.wrapContentSize(),
                    border = BorderStroke(0.5.dp, badgeColors.second)
                ) {
                    Text(
                        text = intern.status.replace("_", " ").uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = badgeColors.second,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Weekly Track",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI summary note
            Card(
                colors = CardDefaults.cardColors(containerColor = PageBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Insight icon",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = intern.aiInsight,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TraceSummaryItem(trace: AgentTrace) {
    val actionColor = getStatusBadgeColor(
        when (trace.decision) {
            "ACTION_POSITIVE_NOTE" -> "on_track"
            "ACTION_IDLE_ALERT" -> "idle"
            "ACTION_DEADLINE_RISK" -> "at_risk"
            "ACTION_OVERLOAD_WARNING" -> "at_risk"
            else -> "neutral"
        }
    ).second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(actionColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trace.internName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trace.timestamp.split("T").firstOrNull() ?: "",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Decision: ${trace.decision}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = actionColor
                )
                Text(
                    text = trace.outcome,
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun getStatusBadgeColor(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "on_track" -> Pair(StatusGreen.copy(alpha = 0.15f), StatusGreen)
        "idle" -> Pair(StatusRed.copy(alpha = 0.15f), StatusRed)
        "at_risk" -> Pair(StatusAmber.copy(alpha = 0.15f), StatusAmber)
        else -> Pair(StatusPurple.copy(alpha = 0.15f), StatusPurple)
    }
}
