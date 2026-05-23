package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object Tasks : Screen()
    object Attendance : Screen()
    object Traces : Screen()
    object Reports : Screen()
    object Settings : Screen()
    object AddIntern : Screen()
    data class Profile(val internId: String) : Screen()
    
    // Intern Flow Screens
    data class InternTasks(val internId: String) : Screen()
    data class InternHours(val internId: String) : Screen()
    data class InternProfile(val internId: String) : Screen()
    data class InternSettings(val internId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve SQLite database instance
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.internIqDao()

        setContent {
            MyApplicationTheme {
                // Initialize seeds inside the local database safely
                LaunchedEffect(Unit) {
                    DataSeeder.seedDatabaseIfEmpty(applicationContext)
                }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                var authenticatedRole by remember { mutableStateOf<String?>(null) } // "Admin" or "Intern"
                var authenticatedInternId by remember { mutableStateOf<String?>(null) }

                val showAdminBottomBar = authenticatedRole == "Admin" && currentScreen !is Screen.Profile && currentScreen != Screen.AddIntern
                val showInternBottomBar = authenticatedRole == "Intern"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showAdminBottomBar) {
                            NavigationBar(
                                containerColor = Color.White,
                                contentColor = PrimaryBlue
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { currentScreen = Screen.Dashboard },
                                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Tasks,
                                    onClick = { currentScreen = Screen.Tasks },
                                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "Milestones") },
                                    label = { Text("Tasks", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Attendance,
                                    onClick = { currentScreen = Screen.Attendance },
                                    icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "Attendance") },
                                    label = { Text("Log Hours", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Traces,
                                    onClick = { currentScreen = Screen.Traces },
                                    icon = { Icon(imageVector = Icons.Default.Timeline, contentDescription = "Trace Logs") },
                                    label = { Text("Reasoning", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Reports,
                                    onClick = { currentScreen = Screen.Reports },
                                    icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "ROI Reports") },
                                    label = { Text("ROI Matrix", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Settings,
                                    onClick = { currentScreen = Screen.Settings },
                                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        } else if (showInternBottomBar) {
                            val id = authenticatedInternId ?: ""
                            NavigationBar(
                                containerColor = Color.White,
                                contentColor = PrimaryBlue
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.InternTasks,
                                    onClick = { currentScreen = Screen.InternTasks(id) },
                                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "My Tasks") },
                                    label = { Text("My Tasks", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.InternHours,
                                    onClick = { currentScreen = Screen.InternHours(id) },
                                    icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "My Hours") },
                                    label = { Text("My Hours", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.InternProfile,
                                    onClick = { currentScreen = Screen.InternProfile(id) },
                                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "My Score") },
                                    label = { Text("My Score", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.InternSettings,
                                    onClick = { currentScreen = Screen.InternSettings(id) },
                                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                dao = dao,
                                onLoginSuccess = { role, internId ->
                                    authenticatedRole = role
                                    authenticatedInternId = internId
                                    if (role == "Admin") {
                                        currentScreen = Screen.Dashboard
                                    } else if (internId != null) {
                                        currentScreen = Screen.InternTasks(internId)
                                    }
                                },
                                modifier = modifier
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                dao = dao,
                                onNavigateToProfile = { internId ->
                                    currentScreen = Screen.Profile(internId)
                                },
                                onNavigateToAddIntern = {
                                    currentScreen = Screen.AddIntern
                                },
                                modifier = modifier
                            )
                        }
                        is Screen.Tasks -> {
                            TaskScreen(
                                dao = dao,
                                modifier = modifier
                            )
                        }
                        is Screen.Attendance -> {
                            HoursScreen(
                                dao = dao,
                                modifier = modifier
                            )
                        }
                        is Screen.Traces -> {
                            TraceScreen(
                                dao = dao,
                                modifier = modifier
                            )
                        }
                        is Screen.Reports -> {
                            ReportsScreen(
                                dao = dao,
                                modifier = modifier
                            )
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                userRole = "Admin",
                                userName = "Supervisory Administrator",
                                onLogout = {
                                    authenticatedRole = null
                                    authenticatedInternId = null
                                    currentScreen = Screen.Login
                                },
                                modifier = modifier
                            )
                        }
                        is Screen.AddIntern -> {
                            AddInternScreen(
                                dao = dao,
                                onSuccess = {
                                    currentScreen = Screen.Dashboard
                                },
                                modifier = modifier
                            )
                        }
                        is Screen.Profile -> {
                            ProfileScreen(
                                internId = screen.internId,
                                dao = dao,
                                onBack = {
                                    currentScreen = Screen.Dashboard
                                },
                                modifier = modifier
                            )
                        }
                        is Screen.InternTasks -> {
                            TaskScreen(
                                dao = dao,
                                modifier = modifier,
                                isInternView = true,
                                internId = screen.internId
                            )
                        }
                        is Screen.InternHours -> {
                            HoursScreen(
                                dao = dao,
                                modifier = modifier,
                                isInternView = true,
                                internId = screen.internId
                            )
                        }
                        is Screen.InternProfile -> {
                            ProfileScreen(
                                internId = screen.internId,
                                dao = dao,
                                onBack = {},
                                modifier = modifier,
                                showBackButton = false
                            )
                        }
                        is Screen.InternSettings -> {
                            val currentIntern by dao.getInternByIdFlow(screen.internId).collectAsState(initial = null)
                            SettingsScreen(
                                userRole = "Intern",
                                userName = currentIntern?.name ?: "Remote Intern",
                                onLogout = {
                                    authenticatedRole = null
                                    authenticatedInternId = null
                                    currentScreen = Screen.Login
                                },
                                modifier = modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

