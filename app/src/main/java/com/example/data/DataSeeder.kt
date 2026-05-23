package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

object DataSeeder {
    suspend fun seedDatabaseIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.internIqDao()

        // Check if DB is already seeded
        val existingInterns = dao.getAllInterns()
        if (existingInterns.isNotEmpty()) {
            return@withContext
        }

        // Seed 1: Ali Hassan
        val ali = Intern(
            id = "intern_ali",
            name = "Ali Hassan",
            iid = "IID-2024-07",
            department = "UI Design",
            headline = "UI/UX Design Intern",
            mentor = "Salman Khan",
            startDate = "2026-01-15",
            endDate = "2026-07-15",
            score = 38,
            status = "idle",
            trend = "down",
            aiInsight = "Idle 26h detected. CONTRADICTION_A flagged: task marked done but 0 clock hours. Supervisor escalated. Recalculated score: 38.",
            lastActivity = "2026-05-22T14:40:00Z",
            email = "ali@interniq.com",
            password = "847" + "291" // Placed as string 847291
        )

        // Seed 2: Sara Malik
        val sara = Intern(
            id = "intern_sara",
            name = "Sara Malik",
            iid = "IID-2024-08",
            department = "Development",
            headline = "Frontend Design Intern",
            mentor = "Hamza Khan",
            startDate = "2026-02-01",
            endDate = "2026-08-01",
            score = 88,
            status = "on_track",
            trend = "up",
            aiInsight = "All sources consistent. Completion 75%. Weekly report generated. Performing excellently.",
            lastActivity = "2026-05-23T14:40:00Z",
            email = "sara@interniq.com",
            password = "631" + "452" // Placed as string 631452
        )

        // Seed 3: Hamza Rao
        val hamza = Intern(
            id = "intern_hamza",
            name = "Hamza Rao",
            iid = "IID-2024-09",
            department = "Content",
            headline = "Content Creator & Copywriter",
            mentor = "Salman Khan",
            startDate = "2026-01-20",
            endDate = "2026-07-20",
            score = 61,
            status = "at_risk",
            trend = "down",
            aiInsight = "Dashboard stale (3d). Overridden by live feed, score recalculated 72->61. Deadline risk HIGH. Workload overload warning (4 pending).",
            lastActivity = "2026-05-22T02:40:00Z",
            email = "hamza@interniq.com",
            password = "295" + "847" // Placed as string 295847
        )

        dao.insertInterns(listOf(ali, sara, hamza))

        // Tasks for Ali (8 total: 3 done, 5 pending)
        val aliTasks = listOf(
            Task("ali_t1", "intern_ali", "Design Landing Page Header", "2026-05-10T09:00:00Z", "2026-05-12T18:00:00Z", "done", "2026-05-12T15:30:00Z", 1830, "High"),
            Task("ali_t2", "intern_ali", "Review Brand Typography Guidelines", "2026-05-12T09:00:00Z", "2026-05-14T18:00:00Z", "done", "2026-05-14T12:00:00Z", 1620, "Med"),
            Task("ali_t3", "intern_ali", "Design Mockup", "2026-05-22T09:00:00Z", "2026-05-25T18:00:00Z", "done", "2026-05-22T17:00:00Z", 0, "High"), // Contradiction: duration 0, and clock says 0 hours.
            Task("ali_t4", "intern_ali", "User Flow Diagrams for Checkout", "2026-05-15T09:00:00Z", "2026-05-20T18:00:00Z", "pending", null, 0, "High"),
            Task("ali_t5", "intern_ali", "Wireframe Mobile Dashboard", "2026-05-16T09:00:00Z", "2026-05-22T18:00:00Z", "pending", null, 0, "Med"),
            Task("ali_t6", "intern_ali", "Export Vector Assets to Developer Shared Drive", "2026-05-18T09:00:00Z", "2026-05-21T18:00:00Z", "pending", null, 0, "Low"),
            Task("ali_t7", "intern_ali", "Prepare Feedback Slide Deck", "2026-05-19T09:00:00Z", "2026-05-24T18:00:00Z", "pending", null, 0, "Med"),
            Task("ali_t8", "intern_ali", "Design Dark Mode Colors", "2026-05-20T09:00:00Z", "2026-05-25T18:00:00Z", "pending", null, 0, "High")
        )

        // Tasks for Sara (8 total: 6 done, 2 remaining)
        val saraTasks = listOf(
            Task("sara_t1", "intern_sara", "Setup Project Repository & CI/CD", "2026-05-01T09:00:00Z", "2026-05-03T18:00:00Z", "done", "2026-05-03T11:00:00Z", 1560, "High"),
            Task("sara_t2", "intern_sara", "Configure Firebase Auth and Database Rules", "2026-05-03T09:00:00Z", "2026-05-05T18:00:00Z", "done", "2026-05-05T14:00:00Z", 1740, "High"),
            Task("sara_t3", "intern_sara", "Implement Dashboard Shell Layout", "2026-05-05T09:00:00Z", "2026-05-08T18:00:00Z", "done", "2026-05-08T16:00:00Z", 2340, "Med"),
            Task("sara_t4", "intern_sara", "Build Settings and Profile Sections", "2026-05-08T09:00:00Z", "2026-05-11T18:00:00Z", "done", "2026-05-10T15:00:00Z", 1800, "Low"),
            Task("sara_t5", "intern_sara", "Integrate Custom Charts via Vico", "2026-05-11T09:00:00Z", "2026-05-15T18:00:00Z", "done", "2026-05-14T12:00:00Z", 2340, "High"),
            Task("sara_t6", "intern_sara", "Add Edge-To-Edge and Keyboard Insets", "2026-05-15T09:00:00Z", "2026-05-18T18:00:00Z", "done", "2026-05-18T10:00:00Z", 1500, "Med"),
            Task("sara_t7", "intern_sara", "Refactor State Management & Safe Args", "2026-05-21T09:00:00Z", "2026-05-24T18:00:00Z", "pending", null, 0, "High"),
            Task("sara_t8", "intern_sara", "Bugfix: Overlapping Dialogs in Settings", "2026-05-22T09:00:00Z", "2026-05-26T18:00:00Z", "pending", null, 0, "Low")
        )

        // Tasks for Hamza (7 total: 3 done, 4 pending)
        val hamzaTasks = listOf(
            Task("hamza_t1", "intern_hamza", "Write Draft Launch Blog Post", "2026-05-10T09:00:00Z", "2026-05-12T18:00:00Z", "done", "2026-05-12T15:00:00Z", 1800, "High"),
            Task("hamza_t2", "intern_hamza", "Compose Twitter Thread for Partner Integration", "2026-05-13T09:00:00Z", "2026-05-15T18:00:00Z", "done", "2026-05-15T10:00:00Z", 1500, "Med"),
            Task("hamza_t3", "intern_hamza", "Prepare Copy for Newsletter Blast", "2026-05-15T09:00:00Z", "2026-05-18T18:00:00Z", "done", "2026-05-18T11:00:00Z", 1560, "Low"),
            Task("hamza_t4", "intern_hamza", "Create Campaign Marketing Strategy Document", "2026-05-18T09:00:00Z", "2026-05-22T18:00:00Z", "pending", null, 0, "High"),
            Task("hamza_t5", "intern_hamza", "Plan and Pitch Podcast Script Topics", "2026-05-19T09:00:00Z", "2026-05-23T18:00:00Z", "pending", null, 0, "High"),
            Task("hamza_t6", "intern_hamza", "Review Proof Copy of Executive Summary", "2026-05-20T09:00:00Z", "2026-05-24T18:00:00Z", "pending", null, 0, "Low"),
            Task("hamza_t7", "intern_hamza", "Write 5 Custom Emails for Ad Outreaches", "2026-05-21T09:00:00Z", "2026-05-25T18:00:00Z", "pending", null, 0, "Med")
        )

        dao.insertTasks(aliTasks)
        dao.insertTasks(saraTasks)
        dao.insertTasks(hamzaTasks)

        // Work Logs for Ali (Clock record empty for 2 consecutive days, others exist)
        val aliLogs = listOf(
            WorkLog("al_wk1", "intern_ali", "2026-05-18", "2026-05-18T09:00:00Z", "2026-05-18T15:00:00Z", 6.0),
            WorkLog("al_wk2", "intern_ali", "2026-05-19", "2026-05-19T09:12:00Z", "2026-05-19T17:12:00Z", 8.0),
            WorkLog("al_wk3", "intern_ali", "2026-05-20", "2026-05-20T08:55:00Z", "2026-05-20T14:55:00Z", 6.0)
            // 21st and 22nd have no entries! This simulates absent/0-clock-hours.
        )

        // Work Logs for Sara (Regular clock hours, updated daily)
        val saraLogs = listOf(
            WorkLog("sa_wk1", "intern_sara", "2026-05-18", "2026-05-18T09:00:00Z", "2026-05-18T17:00:00Z", 8.0),
            WorkLog("sa_wk2", "intern_sara", "2026-05-19", "2026-05-19T09:05:00Z", "2026-05-19T17:35:00Z", 8.5),
            WorkLog("sa_wk3", "intern_sara", "2026-05-20", "2026-05-20T08:52:00Z", "2026-05-20T17:02:00Z", 8.1),
            WorkLog("sa_wk4", "intern_sara", "2026-05-21", "2026-05-21T09:15:00Z", "2026-05-21T18:15:00Z", 9.0),
            WorkLog("sa_wk5", "intern_sara", "2026-05-22", "2026-05-22T08:58:00Z", "2026-05-22T17:28:00Z", 8.5)
        )

        // Work Logs for Hamza (Some logging, some missing)
        val hamzaLogs = listOf(
            WorkLog("ha_wk1", "intern_hamza", "2026-05-18", "2026-05-18T10:00:00Z", "2026-05-18T14:00:00Z", 4.0),
            WorkLog("ha_wk2", "intern_hamza", "2026-05-19", "2026-05-19T09:30:00Z", "2026-05-19T13:30:00Z", 4.0),
            WorkLog("ha_wk3", "intern_hamza", "2026-05-20", "2026-05-20T11:00:00Z", "2026-05-20T15:00:00Z", 4.0)
        )

        dao.insertWorkLogs(aliLogs)
        dao.insertWorkLogs(saraLogs)
        dao.insertWorkLogs(hamzaLogs)

        // Seed 3 pre-built AgentTraces (Ali, Sara, Hamza)
        val aliTrace = AgentTrace(
            timestamp = "2026-05-22T14:40:02Z",
            internName = "Ali Hassan",
            internIid = "IID-2024-07",
            source = "Work Clock Record (0.90) & Activity Feed (0.95)",
            observation = "Ali marked task 'Design Mockup' as Done, but Work Clock Record shows 0 hours logged that day. Also, the Real-time Activity Feed indicates no events for 26 hours.",
            contradiction = "CONTRADICTION_A_TASK_CLOCK detected. Clock Record credibility (0.90) overrides Task Log (0.80). Decision made to flag task under review and penalty assigned.",
            reasoning = "Intern's signals conflict deeply. High-confidence Clock record shows zero effort while Task list claims complete. Feed shows 26h idle. Penalty triggers ACTION_IDLE_ALERT.",
            constraint = "Checked frequency limit: Last alert sent N/A. Checked 90%+ completion: False (Ali is 37%). Checked Stage: Early Stage (Days: 127/181). Urgency normal.",
            decision = "ACTION_IDLE_ALERT",
            chain = "1. Cross-validate idle signal on all 5 sources | 2. Send push notification to mentor Salman Khan | 3. NOTIFICATION FAILED (FCM API Error) -> RETRY 1 FAILED -> ESCALATED TO SUPERVISOR | 4. Recalculate performance score with idle penalty (42 -> 38) | 5. Update intern status to IDLE (FLAGGED) in system records.",
            toolUsed = "Firestore, Gemini API, Supervisor Notification Service",
            failure = "FCM_NOTIFICATION_API_ERROR -> RETRY 1 FAILED -> INSTANTLY ESCALATED ALERT TO SUPERVISOR MAIN FEED",
            outcome = "Dashboard updated with IDLE (FLAGGED) badge, performance score reduced to 38, supervisor notified directly.",
            cost = "2 LLM calls, Est: 0.003 USD | Latency: 2540ms"
        )

        val saraTrace = AgentTrace(
            timestamp = "2026-05-23T10:00:15Z",
            internName = "Sara Malik",
            internIid = "IID-2024-08",
            source = "All Consistent (1.00)",
            observation = "Sara has completed 6 of her 8 assigned tasks. Activity feed shows recent work logs 2 hours ago. Score 88, upward momentum. Mentor feedback: 'Sara is proactive and excellent'.",
            contradiction = "No contradiction detected",
            reasoning = "Completion rate at 75% with highly positive mentor sentiment (sentiment weight: 0.50). Next reporting checkpoint reached (7 days since last weekly check). Triggers report action.",
            constraint = "Weekly check-in trigger condition met (8 days elapsed). All variables healthy.",
            decision = "ACTION_WEEKLY_REPORT",
            chain = "1. Parse and compile all 5 data feeds | 2. Invoke Gemini Pro API with curated performance metrics | 3. Generate structured performance analysis report | 4. Send encouragement message to Sara Malik | 5. Archive report in supervisor files.",
            toolUsed = "Gemini API, Firestore Datastore",
            failure = "Execution successful",
            outcome = "Weekly intelligence analysis stored in reports collection, performance insight note rendered on user card.",
            cost = "1 LLM call, Est: 0.0015 USD | Latency: 1820ms"
        )

        val hamzaTrace = AgentTrace(
            timestamp = "2026-05-22T02:40:05Z",
            internName = "Hamza Rao",
            internIid = "IID-2024-09",
            source = "Performance Dashboard (0.60) vs Activity Feed (0.95)",
            observation = "Dashboard static score shows 72 (Updated 3 days ago). However, live Activity Feed lists 38 hours since last event (idle). Task log shows 4 of 7 tasks pending.",
            contradiction = "CONTRADICTION_B_STALE_DASHBOARD detected. Activity feed (0.95) overrides dashboard (0.60). Static score flagged as stale, recalculated live value = 61.",
            reasoning = "Stale metrics misreported intern's performance. Live calculations adjust score to 61. With 30% tasks done and only 35% time remaining, deadline risk is high.",
            constraint = "Days remaining percentage (35%) falls below risk limit (40%). Checked stage: final 20% weight penalty (Not reached).",
            decision = "ACTION_DEADLINE_RISK",
            chain = "1. Recalculate core score based on actual live data (72 -> 61) | 2. Detect 4 overloaded tasks, trigger workload redistribution Proposal | 3. REDISTRIBUTION CONFLICT (Task assigned already elsewhere) -> ROLLBACK -> Propose alternate task | 4. Generate Future-A (Failure) and Future-B (Remedied) predictions.",
            toolUsed = "Firestore, Gemini API Reasoning Engine",
            failure = "TASK_REDISTRIBUTION_CONFLICT -> ACTION ROLLBACK -> ALTERNATIVE PLAN COMMITTED",
            outcome = "Intern card updated to AT RISK, simulated futures of Ali's performance generated, task list flagged for supervisor routing.",
            cost = "2 LLM calls, Est: 0.003 USD | Latency: 3120ms"
        )

        dao.insertTrace(aliTrace)
        dao.insertTrace(saraTrace)
        dao.insertTrace(hamzaTrace)
    }
}
