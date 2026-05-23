package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInternScreen(
    dao: InternIqDao,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var iid by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("UI Design") }
    var headline by remember { mutableStateOf("") }
    var mentor by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDateText by remember { mutableStateOf(LocalDate.now().plusMonths(6).toString()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    val departments = listOf("UI Design", "Development", "Content", "Marketing", "Operations")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Onboard Remote Intern", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "INTERN BIODATA",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Ali Hassan") },
                            modifier = Modifier.fillMaxWidth().testTag("add_name_field"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = iid,
                            onValueChange = { iid = it },
                            label = { Text("Intern ID (IID)") },
                            placeholder = { Text("e.g. IID-2024-10") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Department Selector
                        Column {
                            Text("Track Division / Department:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { expanded = true }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = department, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlue)
                                    Icon(imageVector = Icons.Default.BusinessCenter, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                }
                                
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    departments.forEach { dept ->
                                        DropdownMenuItem(text = { Text(dept) }, onClick = {
                                            department = dept
                                            expanded = false
                                        })
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = headline,
                            onValueChange = { headline = it },
                            label = { Text("Headline / Role Title") },
                            placeholder = { Text("e.g. UI/UX Creative Intern") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = mentor,
                            onValueChange = { mentor = it },
                            label = { Text("Supervisor / Assigned Mentor") },
                            placeholder = { Text("e.g. Salman Khan") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. ali@interniq.com") },
                            modifier = Modifier.fillMaxWidth().testTag("add_email_field"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password / Login Pin") },
                            placeholder = { Text("e.g. 847291") },
                            modifier = Modifier.fillMaxWidth().testTag("add_password_field"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "CONTRACT ESTIMATED PERIOD",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = startDateText,
                            onValueChange = { startDateText = it },
                            label = { Text("Start Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = endDateText,
                            onValueChange = { endDateText = it },
                            label = { Text("End Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (name.trim().isEmpty() || iid.trim().isEmpty() || mentor.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                            Toast.makeText(context, "All fields (including email and password) are required!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        coroutineScope.launch {
                            val newInternObj = Intern(
                                id = "intern_${UUID.randomUUID().toString().take(6)}",
                                name = name.trim(),
                                iid = iid.trim(),
                                department = department,
                                headline = headline.trim().ifEmpty { "Remote Professional" },
                                mentor = mentor.trim(),
                                startDate = startDateText,
                                endDate = endDateText,
                                score = 50, // default initialization
                                status = "on_track",
                                trend = "flat",
                                aiInsight = "New intern onboarded. First supervisory monitor telemetry session pending.",
                                lastActivity = Instant.now().toString(),
                                email = email.trim(),
                                password = password.trim()
                            )

                            // Add default tasks so the board is active
                            val startingTasks = listOf(
                                Task("${newInternObj.id}_t1", newInternObj.id, "Setup organization profile", Instant.now().toString(), LocalDate.now().plusDays(2).toString(), "pending", null, 0, "High"),
                                Task("${newInternObj.id}_t2", newInternObj.id, "Meet assigned mentor", Instant.now().plusSeconds(600).toString(), LocalDate.now().plusDays(5).toString(), "assigned", null, 0, "Med")
                            )

                            dao.insertIntern(newInternObj)
                            dao.insertTasks(startingTasks)

                            isSubmitting = false
                            Toast.makeText(context, "Intern $name onboarded successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_intern_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSubmitting
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CREATE PROFILE & COMMENCE MONITORING", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
