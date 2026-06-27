package com.example.gymtracker.ui.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.WorkoutLogEntity
import com.example.gymtracker.ui.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    exerciseName: String,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.workoutLogs.collectAsStateWithLifecycle()

    // Filter logs for this exercise and sort from oldest to newest (for chronological chart flow)
    val exerciseLogs = remember(logs, exerciseName) {
        logs.filter { it.exerciseName.lowercase() == exerciseName.lowercase() }
            .sortedBy { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$exerciseName History",
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
            if (exerciseLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "No history recorded!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Log this exercise in a session to start tracking your weight progression.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Chart section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Weight Progression (kg)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap on any bar to see performance details",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                ExerciseBarChart(logs = exerciseLogs)
                            }
                        }
                    }

                    // Logs list section
                    item {
                        Text(
                            text = "Workout Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    items(exerciseLogs.reversed()) { log ->
                        val formattedDate = remember(log.timestamp) {
                            val date = Date(log.timestamp)
                            val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                            formatter.format(date)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.weight} kg",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${log.sets} sets",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${log.repeats} reps/set",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun ExerciseBarChart(logs: List<WorkoutLogEntity>) {
    var selectedIndex by remember { mutableStateOf(-1) }
    
    // Animate the bar heights on load
    var triggerAnimation by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (triggerAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "barChartAnimation"
    )

    LaunchedEffect(key1 = logs) {
        triggerAnimation = true
    }

    val maxWeight = remember(logs) {
        val maxVal = logs.maxOfOrNull { it.weight } ?: 0.0
        if (maxVal > 0.0) maxVal * 1.25 else 50.0
    }

    val themeLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val themePrimaryColor = MaterialTheme.colorScheme.primary
    val themeSecondaryColor = MaterialTheme.colorScheme.secondary
    val themeOutlineVariant = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        // Floating detailed tooltip overlay if a bar is selected
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex in logs.indices) {
                val selectedLog = logs[selectedIndex]
                val dateStr = remember(selectedLog.timestamp) {
                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(selectedLog.timestamp))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "${selectedLog.weight} kg • ${selectedLog.sets}x${selectedLog.repeats}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "Tip: Tapping a bar reveals date & details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Canvas drawing
        val canvasHeight = 180.dp
        var itemWidths = remember { mutableStateListOf<Float>() }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
                .pointerInput(logs) {
                    detectTapGestures { offset ->
                        if (itemWidths.isNotEmpty()) {
                            // Find which bar contains the tapped x coordinate
                            val index = itemWidths.indexOfFirst { leftX ->
                                offset.x >= leftX && offset.x <= (leftX + (size.width / (logs.size * 1.5f)))
                            }
                            if (index != -1) {
                                selectedIndex = index
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Dimensions and Paddings
            val leftPadding = 55f
            val bottomPadding = 45f
            val chartWidth = width - leftPadding - 20f
            val chartHeight = height - bottomPadding - 10f

            // Draw Y-Axis levels and grid lines
            val levels = 4
            val gridPaint = android.graphics.Paint().apply {
                color = themeOutlineVariant.hashCode()
                strokeWidth = 1f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            val textPaint = android.graphics.Paint().apply {
                color = themeLabelColor.hashCode()
                textSize = 28f
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            for (i in 0 until levels) {
                val yFraction = i.toFloat() / (levels - 1)
                val yPos = chartHeight - (yFraction * chartHeight) + 10f
                val weightLabel = (yFraction * maxWeight).toInt()
                
                // Draw grid lines
                drawContext.canvas.nativeCanvas.drawLine(
                    leftPadding, yPos, width, yPos, gridPaint
                )
                // Draw Y-axis labels
                drawContext.canvas.nativeCanvas.drawText(
                    "$weightLabel kg", leftPadding - 10f, yPos + 10f, textPaint
                )
            }

            // Draw X-Axis Baseline
            drawLine(
                color = themeOutlineVariant,
                start = Offset(leftPadding, chartHeight + 10f),
                end = Offset(width, chartHeight + 10f),
                strokeWidth = 3f
            )

            // Draw Bars
            val barSpacingFactor = 1.5f
            val barCount = logs.size
            val slotWidth = chartWidth / barCount
            val barWidth = slotWidth / barSpacingFactor
            val spaceWidth = slotWidth - barWidth

            itemWidths.clear()

            logs.forEachIndexed { index, log ->
                val leftX = leftPadding + (index * slotWidth) + (spaceWidth / 2)
                itemWidths.add(leftX)

                val barHeightFraction = (log.weight / maxWeight).toFloat()
                val animatedBarHeight = chartHeight * barHeightFraction * animationProgress

                val topY = chartHeight - animatedBarHeight + 10f
                val bottomY = chartHeight + 10f

                val isSelected = index == selectedIndex
                val brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(Color(0xFF2E7D32), Color(0xFF81C784)) // Green gradient for active selection
                    } else {
                        listOf(themePrimaryColor, themeSecondaryColor.copy(alpha = 0.7f))
                    }
                )

                // Draw rounded bar
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(leftX, topY),
                    size = Size(barWidth, animatedBarHeight.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw border for selected bar
                if (isSelected) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(leftX - 2f, topY - 2f),
                        size = Size(barWidth + 4f, animatedBarHeight + 4f),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = 3f)
                    )
                }

                // Draw X-axis Date labels
                val dateStr = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(log.timestamp))
                val xLabelPaint = android.graphics.Paint().apply {
                    color = themeLabelColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    dateStr,
                    leftX + (barWidth / 2),
                    height - 10f,
                    xLabelPaint
                )
            }
        }
    }
}
