package com.example.gymtracker.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.ExerciseEntity
import com.example.gymtracker.ui.WorkoutViewModel
import com.example.gymtracker.ui.components.ExerciseImageThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    planId: Int,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val plan = plans.find { it.id == planId }

    var planExercises by remember { mutableStateOf(emptyList<ExerciseEntity>()) }
    var exercisesNotInPlan by remember { mutableStateOf(emptyList<ExerciseEntity>()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseNameInput by remember { mutableStateOf("") }
    var exerciseToRemove by remember { mutableStateOf<ExerciseEntity?>(null) }

    // Helper to refresh screen data
    fun refreshData() {
        planExercises = viewModel.getExercisesForPlan(planId)
        exercisesNotInPlan = viewModel.getExercisesNotInPlan(planId)
    }

    LaunchedEffect(planId) {
        refreshData()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Plan: ${plan?.name ?: ""}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise to Plan"
                )
            }
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
            if (planExercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
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
                            text = "No exercises in this plan!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the '+' button to add exercises from your existing exercises pool or create a new one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(planExercises, key = { it.id }) { exercise ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExerciseImageThumbnail(
                                    imagePath = exercise.picturePath,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercise.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        exerciseToRemove = exercise
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remove from Plan",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Exercise to Plan Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newExerciseNameInput = ""
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
                                value = newExerciseNameInput,
                                onValueChange = { newExerciseNameInput = it },
                                label = { Text("Exercise Name") },
                                placeholder = { Text("e.g., Squat") },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newExerciseNameInput.isNotBlank()) {
                                        viewModel.addNewExerciseToPlan(planId, newExerciseNameInput.trim())
                                        newExerciseNameInput = ""
                                        refreshData()
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
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = exercise.name,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                viewModel.addExerciseToPlan(planId, exercise.id)
                                                refreshData()
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
                        newExerciseNameInput = ""
                    }
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (exerciseToRemove != null) {
        val exercise = exerciseToRemove!!
        AlertDialog(
            onDismissRequest = { exerciseToRemove = null },
            title = {
                Text(
                    text = "Remove Exercise from Plan?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to remove \"${exercise.name}\" from this plan? This will not delete the exercise from your global database, only from this plan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeExerciseFromPlan(planId, exercise.id)
                        refreshData()
                        exerciseToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToRemove = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
