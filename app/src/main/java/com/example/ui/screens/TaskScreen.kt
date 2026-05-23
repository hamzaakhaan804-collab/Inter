package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
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
import com.example.services.AgentService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    dao: InternIqDao,
    modifier: Modifier = Modifier,
    isInternView: Boolean = false,
    internId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val interns by dao.getAllInternsFlow().collectAsState(initial = emptyList())
    var selectedInternId by remember { mutableStateOf(internId ?: "") }
    
    // Auto-select first intern if empty
    LaunchedEffect(interns) {
        if (isInternView && internId != null) {
            selectedInternId = internId
        } else if (selectedInternId.isEmpty() && interns.isNotEmpty()) {
            selectedInternId = interns.first().id
        }
    }

    val selectedIntern = interns.find { it.id == selectedInternId }
    val tasks by dao.getTasksForInternFlow(selectedInternId).collectAsState(initial = emptyList())
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("Med") }
    var newTaskDurationDays by remember { mutableStateOf("3") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Board Sync", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    if (!isInternView) {
                        IconButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("add_task_header_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Task", tint = PrimaryBlue)
                        }
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
            // Dropdown Selector for Interns
            if (!isInternView && interns.isNotEmpty()) {
                Text(
                    text = "Select Intern Profile to View board:",
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
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = PrimaryBlue)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Task List categories side by side
            val assignedTasks = tasks.filter { it.status.lowercase() == "assigned" }
            val pendingTasks = tasks.filter { it.status.lowercase() == "pending" }
            val completedTasks = tasks.filter { it.status.lowercase() == "done" }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Pending/Active
                if (pendingTasks.isNotEmpty() || assignedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "ACTIVE MILESTONES (${pendingTasks.size + assignedTasks.size})",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    items(assignedTasks + pendingTasks) { task ->
                        ActiveTaskCard(
                            task = task,
                            onComplete = {
                                coroutineScope.launch {
                                    // Complete task, calculate duration minutes
                                    val startInst = Instant.parse(task.assignedDate)
                                    val nowInst = Instant.now()
                                    val diffMinutes = ChronoUnit.MINUTES.between(startInst, nowInst).coerceAtLeast(1).toInt()
                                    
                                    val updatedTask = task.copy(
                                        status = "done",
                                        completionTime = nowInst.toString(),
                                        durationMinutes = diffMinutes
                                    )
                                    dao.updateTask(updatedTask)
                                    
                                    // Trigger Supervisor loop instantly to recalculate metrics
                                    selectedIntern?.let {
                                        AgentService.analyzeIntern(context, dao, it)
                                    }
                                    
                                    Toast.makeText(context, "Task completed and index updated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Section 2: Completed List
                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "COMPLETED MILESTONES (${completedTasks.size})",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    
                    items(completedTasks) { task ->
                        CompletedTaskCard(task = task)
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No milestones assigned to this board yet.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Assign Milestone to Board", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_task_input")
                    )

                    Text("Priority Level:", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Med", "High").forEach { pr ->
                            FilterChip(
                                selected = newTaskPriority == pr,
                                onClick = { newTaskPriority = pr },
                                label = { Text(pr) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newTaskDurationDays,
                        onValueChange = { newTaskDurationDays = it },
                        label = { Text("Days until Deadline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isEmpty()) {
                            Toast.makeText(context, "Please enter title!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            val days = newTaskDurationDays.toIntOrNull() ?: 3
                            val now = Instant.now()
                            val deadline = now.plus(days.toLong(), ChronoUnit.DAYS).toString()

                            val newTask = Task(
                                id = UUID.randomUUID().toString(),
                                internId = selectedInternId,
                                title = newTaskTitle,
                                assignedDate = now.toString(),
                                deadline = deadline,
                                status = "pending", // active pending
                                completionTime = null,
                                durationMinutes = 0,
                                priority = newTaskPriority
                            )
                            dao.insertTask(newTask)
                            
                            // Re-evaluate intern metrics
                            selectedIntern?.let {
                                AgentService.analyzeIntern(context, dao, it)
                            }
                            
                            showAddTaskDialog = false
                            newTaskTitle = ""
                            Toast.makeText(context, "Milestone assigned to list!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActiveTaskCard(task: Task, onComplete: () -> Unit) {
    val limitColor = when (task.priority.lowercase()) {
        "high" -> StatusRed
        "med" -> StatusAmber
        else -> PrimaryBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_active_${task.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = limitColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${task.priority.uppercase()} PRIORITY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = limitColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.deadline.split("T").firstOrNull() ?: "",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().testTag("complete_button_${task.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Intern Marks Done")
            }
        }
    }
}

@Composable
fun CompletedTaskCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val hoursTaken = String.format("%.1f", task.durationMinutes.toDouble() / 60.0)
                Text(
                    text = "Cycle time: $hoursTaken hrs • Resolved: ${task.completionTime?.split("T")?.firstOrNull()}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Done Icon",
                tint = StatusGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
