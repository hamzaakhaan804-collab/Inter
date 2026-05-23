package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InternIqDao {
    // Interns
    @Query("SELECT * FROM interns")
    fun getAllInternsFlow(): Flow<List<Intern>>

    @Query("SELECT * FROM interns")
    suspend fun getAllInterns(): List<Intern>

    @Query("SELECT * FROM interns WHERE id = :id")
    fun getInternByIdFlow(id: String): Flow<Intern?>

    @Query("SELECT * FROM interns WHERE id = :id")
    suspend fun getInternById(id: String): Intern?

    @Query("SELECT * FROM interns WHERE status = 'idle'")
    suspend fun getIdleInterns(): List<Intern>

    @Query("SELECT * FROM interns WHERE status = 'at_risk'")
    suspend fun getAtRiskInterns(): List<Intern>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntern(intern: Intern)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterns(interns: List<Intern>)

    @Update
    suspend fun updateIntern(intern: Intern)

    @Delete
    suspend fun deleteIntern(intern: Intern)

    // Tasks
    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE internId = :internId")
    fun getTasksForInternFlow(internId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE internId = :internId")
    suspend fun getTasksForIntern(internId: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    // WorkLogs
    @Query("SELECT * FROM work_logs WHERE internId = :internId")
    fun getWorkLogsForInternFlow(internId: String): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE internId = :internId")
    suspend fun getWorkLogsForIntern(internId: String): List<WorkLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLogs(workLogs: List<WorkLog>)

    // AgentTraces
    @Query("SELECT * FROM agent_traces ORDER BY timestamp DESC")
    fun getAllTracesFlow(): Flow<List<AgentTrace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrace(trace: AgentTrace)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraces(traces: List<AgentTrace>)

    @Query("DELETE FROM agent_traces")
    suspend fun clearAllTraces()
}
