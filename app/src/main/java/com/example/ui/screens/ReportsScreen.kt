package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    dao: InternIqDao,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val interns by dao.getAllInternsFlow().collectAsState(initial = emptyList())
    
    var isDownloadingPdf by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance & ROI Reports", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            // SECTION 1: SYSTEM ROI BASELINE COMPARISON
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM HIGHLIGHT PERFORMANCE MATRIX",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "A direct breakdown demonstrating how the InternIQ Supervisor Agent automates team efficiency over manual tracking workflows.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Table Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBlue.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Text(text = "METRICS", modifier = Modifier.weight(1.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text(text = "MANUAL LOGGING", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(text = "INTERNIQ AI", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }

                        // Table rows
                        ComparisonRow(metric = "Detection Speed", manual = "Weekly audits", agent = "24h instant scan")
                        ComparisonRow(metric = "Report Formulation", manual = "2 - 3 hr manual compiling", agent = "30s autonomous telemetry")
                        ComparisonRow(metric = "Contradictions", manual = "Undetected / overlooked", agent = "Automatic override alert")
                        ComparisonRow(metric = "Telemetry coverage", manual = "Fragmented / Stale notes", agent = "All 5 sources simultaneously")
                        ComparisonRow(metric = "Exception handling", manual = "None / silent loss", agent = "Automated retry + Escalation")
                    }
                }
            }

            // SECTION 2: SIMULATE WEEKLY ANALYSIS EXPORT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WEEKLY TELEMETRY LOGS COMPILER",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Generate and compile executive PDF briefs for all active remote divisions. These files include complete 12-field trace entries and contradiction indices suitable for Board reviews.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isDownloadingPdf = true
                                    delay(2000)
                                    isDownloadingPdf = false
                                    Toast.makeText(context, "Supervisor Board Executive PDF generated successfully!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isDownloadingPdf
                        ) {
                            if (isDownloadingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("COMPILING TRACE DATABASE...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EXPORT FULL PDF EXECUTIVE AUDIT REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Historical Division Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (interns.isEmpty()) {
                item {
                    Text("No division data compiled yet.", color = Color.LightGray, fontSize = 12.sp)
                }
            } else {
                items(interns) { intern ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${intern.name} (${intern.department})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Latest KPI score: ${intern.score}/100",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            val statusBoxColor = when (intern.status.lowercase()) {
                                "on_track" -> StatusGreen
                                "idle" -> StatusRed
                                else -> StatusAmber
                            }

                            Text(
                                text = "RERUN KPI OK",
                                color = statusBoxColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .border(1.dp, statusBoxColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(metric: String, manual: String, agent: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = metric, modifier = Modifier.weight(1.1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = manual, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Gray)
        Text(text = agent, modifier = Modifier.weight(1f), fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
    }
}
