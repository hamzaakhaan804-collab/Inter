package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceScreen(
    dao: InternIqDao,
    modifier: Modifier = Modifier
) {
    val traces by dao.getAllTracesFlow().collectAsState(initial = emptyList())
    
    var internFilter by remember { mutableStateOf("All Interns") }
    var actionFilter by remember { mutableStateOf("All Actions") }
    
    // Unique list of names to filter
    val internNames = listOf("All Interns") + traces.map { it.internName }.distinct()
    val actions = listOf("All Actions", "ACTION_IDLE_ALERT", "ACTION_DEADLINE_RISK", "ACTION_OVERLOAD_WARNING", "ACTION_WEEKLY_REPORT")

    // Filter traces
    val filteredTraces = traces.filter {
        val nameMatch = internFilter == "All Interns" || it.internName == internFilter
        val actionMatch = actionFilter == "All Actions" || it.decision == actionFilter
        nameMatch && actionMatch
    }

    // Calculations for Bottom Summary
    val totalToday = traces.size
    val failuresRecovered = traces.count { it.failure.contains("ROLLBACK") || it.failure.contains("RETRY") || it.failure.contains("ESCALATE") }
    val contradictionsCount = traces.count { !it.contradiction.lowercase().contains("no contradiction") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agent Reasoning Traces", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Judges Diagnostic Console: Real-time loop inspection", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // FILTER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name Dropdown
                var nameExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                        .clickable { nameExpanded = true }
                        .padding(10.dp)
                ) {
                    Text(internFilter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    DropdownMenu(expanded = nameExpanded, onDismissRequest = { nameExpanded = false }) {
                        internNames.forEach { name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                internFilter = name
                                nameExpanded = false
                            })
                        }
                    }
                }

                // Action Dropdown
                var actionExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                        .clickable { actionExpanded = true }
                        .padding(10.dp)
                ) {
                    Text(actionFilter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    DropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                        actions.forEach { act ->
                            DropdownMenuItem(text = { Text(act.replace("ACTION_", "")) }, onClick = {
                                actionFilter = act
                                actionExpanded = false
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TRACES LIST
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTraces) { trace ->
                    TraceLogDetailCard(trace = trace)
                }

                if (filteredTraces.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No traces found matching search filters.", color = Color.Gray)
                        }
                    }
                }
            }

            // BOTTOM SUMMARY PANEL
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiagnosticStat(label = "Overall Decisions", value = totalToday.toString())
                    DiagnosticStat(label = "Failures Escaped", value = failuresRecovered.toString())
                    DiagnosticStat(label = "Contradictions Flagged", value = contradictionsCount.toString())
                }
            }
        }
    }
}

@Composable
fun DiagnosticStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Text(text = label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun TraceLogDetailCard(trace: AgentTrace) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val badgeColors = getStatusBadgeColorsForTrace(trace.decision, trace.failure)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("trace_card_${trace.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, badgeColors.second.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Collapsed row header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.7f)) {
                    Text(
                        text = "${trace.internName} (${trace.internIid})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Decision: ${trace.decision}",
                        color = badgeColors.second,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColors.first
                    ) {
                        Text(
                            text = if (trace.failure.contains("successful")) "SUCCESS" else if (trace.failure.contains("CONFLICT")) "CONFLICT" else "AUTORECOVERY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColors.second,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Outcome: ${trace.outcome}",
                fontSize = 11.sp,
                color = Color.DarkGray
            )

            // Dynamic Expandable Table Fields
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    TraceDetailRow(label = "Timestamp (ISO)", value = trace.timestamp)
                    TraceDetailRow(label = "Source Trigger", value = trace.source)
                    TraceDetailRow(label = "Observer Telemetry", value = trace.observation)
                    TraceDetailRow(label = "Contradiction Flag", value = trace.contradiction)
                    TraceDetailRow(label = "Reasoning Engine Matrix", value = trace.reasoning)
                    TraceDetailRow(label = "Check Constraint Gates", value = trace.constraint)
                    TraceDetailRow(label = "Action execution chain", value = trace.chain, isStepChain = true)
                    TraceDetailRow(label = "Tools Invoked", value = trace.toolUsed)
                    TraceDetailRow(label = "Failure / Recovery", value = trace.failure, isAlert = !trace.failure.contains("successful"))
                    TraceDetailRow(label = "Estimated Telemetry cost", value = trace.cost)
                }
            }
        }
    }
}

@Composable
fun TraceDetailRow(label: String, value: String, isAlert: Boolean = false, isStepChain: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        
        if (isStepChain) {
            val steps = value.split(" | ")
            steps.forEach { step ->
                Text(
                    text = "• $step",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 11.sp,
                color = if (isAlert) StatusRed else MaterialTheme.colorScheme.onSurface,
                lineHeight = 15.sp,
                fontWeight = if (isAlert) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

fun getStatusBadgeColorsForTrace(decision: String, failure: String): Pair<Color, Color> {
    return when {
        !failure.contains("successful") && (failure.contains("CONFLICT") || failure.contains("ESCALATED")) -> Pair(StatusRed.copy(alpha = 0.12f), StatusRed)
        !failure.contains("successful") && failure.contains("RETRY") -> Pair(StatusAmber.copy(alpha = 0.12f), StatusAmber)
        decision == "ACTION_WEEKLY_REPORT" -> Pair(StatusPurple.copy(alpha = 0.12f), StatusPurple)
        else -> Pair(StatusGreen.copy(alpha = 0.12f), StatusGreen)
    }
}
