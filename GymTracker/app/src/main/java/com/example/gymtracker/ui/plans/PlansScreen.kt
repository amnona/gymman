package com.example.gymtracker.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.gymtracker.data.PlanEntity
import com.example.gymtracker.ui.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onEditPlan: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var planNameInput by remember { mutableStateOf("") }
    
    var planToDelete by remember { mutableStateOf<PlanEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Plans",
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
                    contentDescription = "Create Plan"
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
            if (plans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "No plans created yet!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Workout plans let you organize groups of exercises. Tap '+' to create your first plan.",
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
                    items(plans, key = { it.id }) { plan ->
                        val isActive = activePlan?.id == plan.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setActivePlan(plan)
                                    onBack()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plan.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isActive) {
                                        Text(
                                            text = "Active Plan",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { onEditPlan(plan.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Plan Exercises",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { planToDelete = plan }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Plan",
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
    }

    // Create Plan Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                planNameInput = ""
            },
            title = {
                Text(
                    text = "New Workout Plan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = planNameInput,
                    onValueChange = { planNameInput = it },
                    label = { Text("Plan Name") },
                    placeholder = { Text("e.g., Pull Day") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (planNameInput.isNotBlank()) {
                            viewModel.addPlan(planNameInput.trim())
                            showAddDialog = false
                            planNameInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        planNameInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Plan Confirmation Dialog
    if (planToDelete != null) {
        val plan = planToDelete!!
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = {
                Text(
                    text = "Delete Plan?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete the plan \"${plan.name}\"? Exercises belonging to the plan won't be deleted globally, but this plan configuration will be removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlan(plan)
                        planToDelete = null
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
                TextButton(onClick = { planToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
