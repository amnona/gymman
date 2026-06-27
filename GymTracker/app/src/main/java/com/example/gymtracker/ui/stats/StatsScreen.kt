package com.example.gymtracker.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.WorkoutLogEntity
import com.example.gymtracker.ui.WorkoutViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onExerciseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.workoutLogs.collectAsStateWithLifecycle()

    var sessionToDelete by remember { mutableStateOf<Long?>(null) }
    var logToDelete by remember { mutableStateOf<WorkoutLogEntity?>(null) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var localSavePath by remember { mutableStateOf<String?>(null) }

    // Group logs by session ID and sort sessions descending (newest first)
    val sessionsMap = remember(logs) {
        logs.groupBy { it.sessionId }
    }
    val sortedSessionIds = remember(sessionsMap) {
        sessionsMap.keys.sortedDescending()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout Sessions",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { showExportOptionsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export to CSV/TSV",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            if (sortedSessionIds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "No sessions logged yet!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Complete exercises and check them off to log your first session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedSessionIds) { sessionId ->
                        val sessionLogs = sessionsMap[sessionId] ?: emptyList()
                        SessionCard(
                            sessionId = sessionId,
                            logs = sessionLogs,
                            onExerciseClick = onExerciseClick,
                            onDeleteSession = { sessionToDelete = sessionId },
                            onDeleteLog = { logToDelete = it }
                        )
                    }
                }
            }
        }
    }

    // Delete Session Confirmation Dialog
    if (sessionToDelete != null) {
        val sessionId = sessionToDelete!!
        val formattedDate = remember(sessionId) {
            val date = Date(sessionId)
            val formatter = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
            formatter.format(date)
        }
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = "Delete Workout Session?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete this workout session from $formattedDate and all its logged exercises? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWorkoutSession(sessionId)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Log Entry Confirmation Dialog
    if (logToDelete != null) {
        val log = logToDelete!!
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = {
                Text(
                    text = "Delete Log Entry?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete the entry for \"${log.exerciseName}\" (${log.sets} sets × ${log.repeats} reps @ ${log.weight} kg)?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExerciseLog(log.id)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showExportOptionsDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showExportOptionsDialog = false },
            title = {
                Text(
                    text = "Export Workout History",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Would you like to share your workout history file with other apps or save it locally on this device?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportOptionsDialog = false
                        exportAndShareHistory(context, logs)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Share File")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportOptionsDialog = false
                        val path = exportAndSaveHistoryLocally(context, logs)
                        localSavePath = path
                    }
                ) {
                    Text("Save Locally")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (localSavePath != null) {
        AlertDialog(
            onDismissRequest = { localSavePath = null },
            title = {
                Text(
                    text = "File Saved Successfully",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your workout history has been saved to:\n\n$localSavePath")
            },
            confirmButton = {
                Button(
                    onClick = { localSavePath = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SessionCard(
    sessionId: Long,
    logs: List<WorkoutLogEntity>,
    onExerciseClick: (String) -> Unit,
    onDeleteSession: () -> Unit,
    onDeleteLog: (WorkoutLogEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = remember(sessionId) {
        val date = Date(sessionId)
        val formatter = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
        formatter.format(date)
    }

    val planName = remember(logs) {
        logs.firstOrNull { !it.planName.isNullOrEmpty() }?.planName
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val planText = if (!planName.isNullOrEmpty()) " • Plan: $planName" else ""
                    Text(
                        text = "${logs.size} exercise${if (logs.size == 1) "" else "s"} completed$planText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { onDeleteSession() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Session",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    logs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                                .clickable { onExerciseClick(log.exerciseName) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = log.exerciseName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${log.sets} sets × ${log.repeats} reps • ${log.weight} kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onExerciseClick(log.exerciseName) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "View History",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteLog(log) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Log Entry",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun exportAndShareHistory(context: android.content.Context, logs: List<WorkoutLogEntity>) {
    try {
        val tsvBuilder = java.lang.StringBuilder()
        tsvBuilder.append("Date/Time\tExercise\tWeight (kg)\tRepeats\tSets\tPlan Name\n")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        logs.forEach { log ->
            val dateStr = dateFormat.format(java.util.Date(log.timestamp))
            val plan = log.planName ?: ""
            tsvBuilder.append("$dateStr\t${log.exerciseName}\t${log.weight}\t${log.repeats}\t${log.sets}\t$plan\n")
        }

        val cacheFile = java.io.File(context.cacheDir, "workout_history.tsv")
        cacheFile.writeText(tsvBuilder.toString())

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/tab-separated-values"
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Gym Workout History")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = android.content.Intent.createChooser(intent, "Share Workout History")
        chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun exportAndSaveHistoryLocally(context: android.content.Context, logs: List<WorkoutLogEntity>): String {
    try {
        val tsvBuilder = java.lang.StringBuilder()
        tsvBuilder.append("Date/Time\tExercise\tWeight (kg)\tRepeats\tSets\tPlan Name\n")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        logs.forEach { log ->
            val dateStr = dateFormat.format(java.util.Date(log.timestamp))
            val plan = log.planName ?: ""
            tsvBuilder.append("$dateStr\t${log.exerciseName}\t${log.weight}\t${log.repeats}\t${log.sets}\t$plan\n")
        }

        val localFile = java.io.File(context.getExternalFilesDir(null), "workout_history.tsv")
        localFile.writeText(tsvBuilder.toString())
        return localFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return "Error saving file: ${e.localizedMessage}"
    }
}
