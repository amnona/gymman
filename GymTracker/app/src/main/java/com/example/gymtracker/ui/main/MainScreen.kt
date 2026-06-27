package com.example.gymtracker.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.gymtracker.Details
import com.example.gymtracker.Stats
import com.example.gymtracker.PlansList
import com.example.gymtracker.data.ExerciseEntity
import com.example.gymtracker.ui.WorkoutViewModel
import com.example.gymtracker.ui.components.ExerciseImageThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPlanDropdown by remember { mutableStateOf(false) }
    var exerciseNameInput by remember { mutableStateOf("") }

    var exercisesNotInPlan by remember { mutableStateOf(emptyList<ExerciseEntity>()) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var localSavePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activePlan, showAddDialog) {
        if (showAddDialog && activePlan != null) {
            exercisesNotInPlan = viewModel.getExercisesNotInPlan(activePlan!!.id)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        TextButton(
                            onClick = { showPlanDropdown = true },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activePlan?.name ?: "All Exercises",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Plan",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showPlanDropdown,
                            onDismissRequest = { showPlanDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Exercises") },
                                onClick = {
                                    viewModel.setActivePlan(null)
                                    showPlanDropdown = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = null
                                    )
                                }
                            )
                            if (plans.isNotEmpty()) {
                                HorizontalDivider()
                                plans.forEach { plan ->
                                    DropdownMenuItem(
                                        text = { Text(plan.name) },
                                        onClick = {
                                            viewModel.setActivePlan(plan)
                                            showPlanDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = if (activePlan?.id == plan.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    )
                                }
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Manage Plans...") },
                                onClick = {
                                    showPlanDropdown = false
                                    onItemClick(PlansList)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                actions = {
                    // Export current plan button (appears when a plan is active)
                    val planExercises = remember(activePlan) {
                        activePlan?.let { viewModel.getExercisesForPlan(it.id) } ?: emptyList()
                    }
                    val context = LocalContext.current
                    if (activePlan != null && planExercises.isNotEmpty()) {
                        IconButton(onClick = { showExportOptionsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Plan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onItemClick(Stats) }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "View Stats",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (exercises.any { it.isDone }) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Session",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            CountdownTimerBottomBar(
                viewModel = viewModel,
                onAddClick = { showAddDialog = true }
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
            Column(modifier = Modifier.fillMaxSize()) {

                if (exercises.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                text = if (activePlan != null) "No exercises in this plan!" else "No exercises yet!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (activePlan != null) {
                                    "Tap the '+' button below to add a new exercise to this plan, or manage the plan to add existing ones."
                                } else {
                                    "Tap the '+' button below to add your first gym exercise."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = exercises,
                            key = { _, exercise -> exercise.id }
                        ) { index, exercise ->
                            ExerciseRowItem(
                                exercise = exercise,
                                isFirst = index == 0,
                                isLast = index == exercises.lastIndex,
                                onClick = { onItemClick(Details(exercise.id)) },
                                onDelete = { viewModel.deleteExercise(exercise) },
                                onMoveUp = { viewModel.moveExercise(exercise, moveUp = true) },
                                onMoveDown = { viewModel.moveExercise(exercise, moveUp = false) },
                                onToggleDone = {
                                    viewModel.updateExerciseDetails(
                                        exercise = exercise,
                                        weight = exercise.lastWeight,
                                        repeats = exercise.lastRepeats,
                                        sets = exercise.lastSets,
                                        isDone = !exercise.isDone
                                    )
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Exercise Dialog
    if (showAddDialog) {
        val plan = activePlan
        if (plan != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    exerciseNameInput = ""
                },
                title = {
                    Text(
                        text = "Add Exercise to Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Create brand new exercise section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Create Brand New Exercise",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = exerciseNameInput,
                                    onValueChange = { exerciseNameInput = it },
                                    label = { Text("Exercise Name") },
                                    placeholder = { Text("e.g., Squat") },
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (exerciseNameInput.isNotBlank()) {
                                            viewModel.addExercise(exerciseNameInput.trim())
                                            exerciseNameInput = ""
                                            showAddDialog = false
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }

                        HorizontalDivider()

                        // Select from existing exercises section
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Select Existing Exercise",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (exercisesNotInPlan.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No other exercises available.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(exercisesNotInPlan, key = { it.id }) { exercise ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            ExerciseImageThumbnail(
                                                imagePath = exercise.picturePath,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = exercise.name,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.addExerciseToPlan(plan.id, exercise.id)
                                                    showAddDialog = false
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    contentDescription = "Add existing exercise",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            exerciseNameInput = ""
                        }
                    ) {
                        Text("Close")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    exerciseNameInput = ""
                },
                title = {
                    Text(
                        text = "Add Exercise",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = exerciseNameInput,
                        onValueChange = { exerciseNameInput = it },
                        label = { Text("Exercise Name") },
                        placeholder = { Text("e.g., Bench Press") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (exerciseNameInput.isNotBlank()) {
                                viewModel.addExercise(exerciseNameInput.trim())
                                showAddDialog = false
                                exerciseNameInput = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            exerciseNameInput = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    // Reset Session Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Gym Session?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will clear the completion checkboxes for all exercises and move them back to their original ordering.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSession()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Export Plan Options Dialog
    if (showExportOptionsDialog) {
        val plan = activePlan
        val exercisesForPlan = plan?.let { viewModel.getExercisesForPlan(it.id) } ?: emptyList()
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showExportOptionsDialog = false },
            title = {
                Text(
                    text = "Export Plan \"${plan?.name ?: ""}\"",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Would you like to share this plan as a file with other apps or save it locally on this device?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportOptionsDialog = false
                        plan?.let { exportAndSharePlan(context, it.name, exercisesForPlan) }
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
                        val path = plan?.let { exportAndSavePlanLocally(context, it.name, exercisesForPlan) }
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
                Text("Your plan has been saved to:\n\n$localSavePath")
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

private fun exportAndSharePlan(context: android.content.Context, planName: String, exercises: List<ExerciseEntity>) {
    try {
        val tsvBuilder = StringBuilder()
        tsvBuilder.append("Exercise\tWeight (kg)\tSets\tRepeats\n")
        exercises.forEach { ex ->
            tsvBuilder.append("${ex.name}\t${ex.lastWeight}\t${ex.lastSets}\t${ex.lastRepeats}\n")
        }

        val fileName = "plan_${planName.replace("\\s+".toRegex(), "_").lowercase(Locale.getDefault())}.tsv"
        val cacheFile = java.io.File(context.cacheDir, fileName)
        cacheFile.writeText(tsvBuilder.toString())

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/tab-separated-values"
            putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Plan: $planName")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = android.content.Intent.createChooser(intent, "Share Plan")
        chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun exportAndSavePlanLocally(context: android.content.Context, planName: String, exercises: List<ExerciseEntity>): String {
    try {
        val tsvBuilder = StringBuilder()
        tsvBuilder.append("Exercise\tWeight (kg)\tSets\tRepeats\n")
        exercises.forEach { ex ->
            tsvBuilder.append("${ex.name}\t${ex.lastWeight}\t${ex.lastSets}\t${ex.lastRepeats}\n")
        }

        val fileName = "plan_${planName.replace("\\s+".toRegex(), "_").lowercase(Locale.getDefault())}.tsv"
        val localFile = java.io.File(context.getExternalFilesDir(null), fileName)
        localFile.writeText(tsvBuilder.toString())
        return localFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return "Error saving file: ${e.localizedMessage}"
    }
}

@Composable
fun ExerciseRowItem(
    exercise: ExerciseEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant color transitions for completed items
    val animatedBgColor by animateColorAsState(
        targetValue = if (exercise.isDone) {
            Color(0xFFE8F5E9) // Very light emerald green
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        },
        animationSpec = tween(durationMillis = 400),
        label = "bgColorTransition"
    )

    val textColor = if (exercise.isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (exercise.isDone) TextDecoration.LineThrough else TextDecoration.None

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark / Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (exercise.isDone) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer
                    )
                    .clickable { onToggleDone() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (exercise.isDone) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                    contentDescription = if (exercise.isDone) "Mark Undone" else "Mark Done",
                    tint = if (exercise.isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            ExerciseImageThumbnail(
                imagePath = exercise.picturePath,
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info (Exercise Name + Last stats)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    textDecoration = textDecoration
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val lastStatsText = if (exercise.lastWeight > 0.0 || exercise.lastSets > 0 || exercise.lastRepeats > 0) {
                    "${exercise.lastSets} sets × ${exercise.lastRepeats} reps @ ${exercise.lastWeight} kg"
                } else {
                    "No log recorded yet"
                }
                
                Text(
                    text = lastStatsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (exercise.isDone) Color(0xFF558B2F) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls Column: Reorder and Delete Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Reorder controls
                Column {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst && !exercise.isDone,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (isFirst || exercise.isDone) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast && !exercise.isDone,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (isLast || exercise.isDone) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Delete Exercise Action
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exercise",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownTimerBottomBar(
    viewModel: WorkoutViewModel,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val countdownTime by viewModel.countdownTime.collectAsStateWithLifecycle()
    val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()

    val displayTime = if (countdownTime >= 0) {
        countdownTime.toString()
    } else {
        "-${(-countdownTime).toString()}"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (timerRunning) {
                        viewModel.stopCountdownTimer(context)
                    } else {
                        viewModel.startCountdownTimer(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (timerRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = displayTime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
