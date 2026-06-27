package com.example.gymtracker

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gymtracker.ui.WorkoutViewModel
import com.example.gymtracker.ui.WorkoutViewModelFactory
import com.example.gymtracker.ui.details.DetailsScreen
import com.example.gymtracker.ui.main.MainScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)
  
  val context = LocalContext.current.applicationContext as GymTrackerApplication
  val viewModel: WorkoutViewModel = viewModel(
    factory = WorkoutViewModelFactory(context.repository)
  )

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            viewModel = viewModel,
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<Details> { detailsKey ->
          DetailsScreen(
            exerciseId = detailsKey.exerciseId,
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            onViewHistory = { exerciseName -> backStack.add(History(exerciseName)) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<Stats> {
          com.example.gymtracker.ui.stats.StatsScreen(
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            onExerciseClick = { exerciseName -> backStack.add(History(exerciseName)) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<History> { historyKey ->
          com.example.gymtracker.ui.history.HistoryScreen(
            exerciseName = historyKey.exerciseName,
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<PlansList> {
          com.example.gymtracker.ui.plans.PlansScreen(
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            onEditPlan = { planId -> backStack.add(EditPlan(planId)) },
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
        entry<EditPlan> { editPlanKey ->
          com.example.gymtracker.ui.plans.EditPlanScreen(
            planId = editPlanKey.planId,
            viewModel = viewModel,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding().padding(16.dp)
          )
        }
      },
  )
}

