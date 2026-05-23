package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interns")
data class Intern(
    @PrimaryKey val id: String,
    val name: String,
    val iid: String,
    val department: String,
    val headline: String,
    val mentor: String,
    val startDate: String,
    val endDate: String,
    val score: Int,
    val status: String, // "on_track", "idle", "at_risk"
    val trend: String,  // "up", "flat", "down"
    val aiInsight: String,
    val lastActivity: String,
    val email: String = "",
    val password: String = ""
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val internId: String,
    val title: String,
    val assignedDate: String,
    val deadline: String,
    val status: String, // "assigned", "pending", "done", "remaining"
    val completionTime: String?,
    val durationMinutes: Int,
    val priority: String // "High", "Med", "Low"
)

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey val id: String,
    val internId: String,
    val date: String, // yyyy-MM-dd
    val clockIn: String?,
    val clockOut: String?,
    val sessionHours: Double
)

@Entity(tableName = "agent_traces")
data class AgentTrace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: String,
    val internName: String,
    val internIid: String,
    val source: String,
    val observation: String,
    val contradiction: String,
    val reasoning: String,
    val constraint: String,
    val decision: String,
    val chain: String, // steps joined together
    val toolUsed: String,
    val failure: String,
    val outcome: String,
    val cost: String
)
