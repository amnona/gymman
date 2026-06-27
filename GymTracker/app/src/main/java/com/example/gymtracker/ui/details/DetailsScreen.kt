package com.example.gymtracker.ui.details

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gymtracker.ui.WorkoutViewModel
import com.example.gymtracker.ui.components.ExerciseImageThumbnail
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    exerciseId: Int,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onViewHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
     val exercises by viewModel.exercises.collectAsState()
     val exercise = exercises.find { it.id == exerciseId }
     val context = LocalContext.current
     var showPictureOptions by remember { mutableStateOf(false) }
     var showRenameDialog by remember { mutableStateOf(false) }
     var renameText by remember { mutableStateOf("") }

     val picturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = copyExerciseImageToInternalStorage(context, uri, exerciseId)
            if (savedPath != null && exercise != null) {
                viewModel.updateExercisePicture(exercise, savedPath)
            }
        }
    }

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Input States
    var weightText by remember { mutableStateOf(if (exercise.lastWeight > 0) exercise.lastWeight.toString() else "") }
    var repeatsText by remember { mutableStateOf(if (exercise.lastRepeats > 0) exercise.lastRepeats.toString() else "") }
    var setsText by remember { mutableStateOf(if (exercise.lastSets > 0) exercise.lastSets.toString() else "") }
    var isDone by remember { mutableStateOf(exercise.isDone) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                 actions = {
                     IconButton(onClick = {
                         showRenameDialog = true
                         renameText = exercise.name
                     }) {
                         Icon(
                             imageVector = Icons.Default.Edit,
                             contentDescription = "Rename Exercise",
                             tint = MaterialTheme.colorScheme.primary
                         )
                     }
                     IconButton(onClick = { onViewHistory(exercise.name) }) {
                         Icon(
                             imageVector = Icons.Default.BarChart,
                             contentDescription = "View History",
                             tint = MaterialTheme.colorScheme.primary
                         )
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
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header card with premium gym theme
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last Workout Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { onViewHistory(exercise.name) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View History", fontSize = 14.sp)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryItem(
                                label = "Weight",
                                value = if (exercise.lastWeight > 0) "${exercise.lastWeight} kg" else "--"
                            )
                            SummaryItem(
                                label = "Sets",
                                value = if (exercise.lastSets > 0) "${exercise.lastSets}" else "--"
                            )
                            SummaryItem(
                                label = "Reps",
                                value = if (exercise.lastRepeats > 0) "${exercise.lastRepeats}" else "--"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ExerciseImageThumbnail(
                        imagePath = exercise.picturePath,
                        modifier = Modifier
                            .size(128.dp)
                            .clickable { showPictureOptions = true }
                    )
                }

                if (showPictureOptions) {
                    AlertDialog(
                        onDismissRequest = { showPictureOptions = false },
                        title = {
                            Text(
                                text = "Exercise Picture",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = if (exercise.picturePath.isNullOrBlank()) {
                                    "Choose a picture for this exercise."
                                } else {
                                    "You can replace the current picture or remove it."
                                }
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPictureOptions = false
                                    picturePickerLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (exercise.picturePath.isNullOrBlank()) "Upload" else "Replace")
                            }
                        },
                        dismissButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!exercise.picturePath.isNullOrBlank()) {
                                    TextButton(
                                        onClick = {
                                            viewModel.removeExercisePicture(exercise)
                                            showPictureOptions = false
                                        }
                                    ) {
                                        Text("Remove")
                                    }
                                }
                                TextButton(onClick = { showPictureOptions = false }) {
                                    Text("Cancel")
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (showRenameDialog) {
                    AlertDialog(
                        onDismissRequest = { showRenameDialog = false },
                        title = {
                            Text(
                                text = "Rename Exercise",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            OutlinedTextField(
                                value = renameText,
                                onValueChange = { renameText = it },
                                label = { Text("New Name") },
                                placeholder = { Text(exercise.name) },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (renameText.isNotBlank() && renameText != exercise.name) {
                                        viewModel.renameExercise(exercise, renameText.trim())
                                    }
                                    showRenameDialog = false
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Rename")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRenameDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                 // Weight Input Field
                DetailInputField(
                    label = "Weight (kg)",
                    value = weightText,
                    onValueChange = { weightText = it },
                    keyboardType = KeyboardType.Decimal,
                    icon = Icons.Default.FitnessCenter
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailInputField(
                        label = "Sets",
                        value = setsText,
                        onValueChange = { setsText = it },
                        keyboardType = KeyboardType.Number,
                        icon = Icons.Default.FormatListNumbered,
                        modifier = Modifier.weight(1f)
                    )

                    DetailInputField(
                        label = "Reps / Set",
                        value = repeatsText,
                        onValueChange = { repeatsText = it },
                        keyboardType = KeyboardType.Number,
                        icon = Icons.Default.Repeat,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Completion Checkbox Row
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isDone = !isDone },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) {
                            Color(0xFFE8F5E9) // Very light green background in light mode
                        } else {
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Completed today",
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mark this exercise as completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDone) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { isDone = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2E7D32),
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val weight = weightText.toDoubleOrNull() ?: 0.0
                        val repeats = repeatsText.toIntOrNull() ?: 0
                        val sets = setsText.toIntOrNull() ?: 0

                        viewModel.updateExerciseDetails(
                            exercise = exercise,
                            weight = weight,
                            repeats = repeats,
                            sets = sets,
                            isDone = isDone
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text("Complete & Log Exercise", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Text("Save Gym Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DetailInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

private fun copyExerciseImageToInternalStorage(
    context: Context,
    sourceUri: Uri,
    exerciseId: Int
): String? {
    return try {
        val mimeType = context.contentResolver.getType(sourceUri)
        val extension = when (mimeType) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/heic" -> ".heic"
            "image/heif" -> ".heif"
            else -> ".jpg"
        }

        val imageDir = File(context.filesDir, "exercise_images").apply { mkdirs() }
        val imageFile = File(imageDir, "exercise_${exerciseId}_${System.currentTimeMillis()}$extension")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
        } ?: return null

        imageFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

