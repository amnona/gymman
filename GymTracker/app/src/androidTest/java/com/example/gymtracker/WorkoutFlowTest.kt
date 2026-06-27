package com.example.gymtracker

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as GymTrackerApplication
        context.repository.clearDatabase()
    }

    @Test
    fun testCompleteGymActivityFlow() {
        // 1. App starts, check empty state
        composeTestRule.onNodeWithText("No exercises yet!").assertExists()

        // 2. Add first exercise: Bench Press
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Bench Press")
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify Bench Press is listed
        composeTestRule.onNodeWithText("Bench Press").assertExists()

        // 3. Add second exercise: Squat
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Squat")
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify Squat is listed
        composeTestRule.onNodeWithText("Squat").assertExists()

        // 4. Click on Bench Press to open Details screen
        composeTestRule.onNodeWithText("Bench Press").performClick()

        // Verify Details screen is open
        composeTestRule.onNodeWithText("Log Today's Performance").assertExists()

        // 5. Input performance: 3 sets of 10 reps at 60 kg
        composeTestRule.onNodeWithText("Weight (kg)").performTextInput("60")
        composeTestRule.onNodeWithText("Number of Sets").performTextInput("3")
        composeTestRule.onNodeWithText("Reps per Set").performTextInput("10")

        // 6. Check "Completed today"
        composeTestRule.onNodeWithText("Completed today").performClick()

        // 7. Click save
        composeTestRule.onNodeWithText("Complete & Log Exercise").performClick()

        // 8. Verify we are back on main screen and stats are updated
        composeTestRule.onNodeWithText("3 sets × 10 reps @ 60.0 kg").assertExists()

        // 9. Re-open Bench Press details to check if pre-filled
        composeTestRule.onNodeWithText("Bench Press").performClick()
        composeTestRule.onNodeWithText("60.0").assertExists()
        composeTestRule.onAllNodesWithText("3").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("10").assertCountEquals(2)

        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }

    @Test
    fun testPlansAndStatsDeletionFlow() {
        // App starts, check empty state
        composeTestRule.onNodeWithText("No exercises yet!").assertExists()

        // 1. Open Plan Selector and go to Manage Plans
        composeTestRule.onNodeWithText("All Exercises").performClick()
        composeTestRule.onNodeWithText("Manage Plans...").performClick()

        // Verify we are on Manage Plans screen
        composeTestRule.onNodeWithText("Manage Plans").assertExists()
        composeTestRule.onNodeWithText("No plans created yet!").assertExists()

        // 2. Create Plan: Leg Day
        composeTestRule.onNodeWithContentDescription("Create Plan").performClick()
        composeTestRule.onNodeWithText("Plan Name").performTextInput("Leg Day")
        composeTestRule.onNodeWithText("Create").performClick()

        // Verify Leg Day is listed
        composeTestRule.onNodeWithText("Leg Day").assertExists()

        // 3. Select Leg Day plan (it will activate and return to main screen)
        composeTestRule.onNodeWithText("Leg Day").performClick()

        // Verify active plan is Leg Day on main screen
        composeTestRule.onNodeWithText("Leg Day").assertExists()
        composeTestRule.onNodeWithText("No exercises in this plan!").assertExists()

        // 4. Add exercise to Leg Day plan
        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
        composeTestRule.onNodeWithText("Exercise Name").performTextInput("Squat")
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify Squat is listed
        composeTestRule.onNodeWithText("Squat").assertExists()

        // 5. Open details of Squat
        composeTestRule.onNodeWithText("Squat").performClick()

        // Log performance: 3 sets of 8 reps @ 100 kg
        composeTestRule.onNodeWithText("Weight (kg)").performTextInput("100")
        composeTestRule.onNodeWithText("Number of Sets").performTextInput("3")
        composeTestRule.onNodeWithText("Reps per Set").performTextInput("8")
        composeTestRule.onNodeWithText("Completed today").performClick()
        composeTestRule.onNodeWithText("Complete & Log Exercise").performClick()

        // Verify back on main screen and exercise is completed
        composeTestRule.onNodeWithText("3 sets × 8 reps @ 100.0 kg").assertExists()

        // 6. Go to Stats Screen
        composeTestRule.onNodeWithContentDescription("View Stats").performClick()

        // Verify session lists plan name Leg Day
        composeTestRule.onNodeWithText("1 exercise completed • Plan: Leg Day").assertExists()

        // 7. Delete the session
        composeTestRule.onNodeWithContentDescription("Delete Session").performClick()
        // Confirmation dialog: click Delete
        composeTestRule.onNodeWithText("Delete").performClick()

        // Verify empty state on Stats screen
        composeTestRule.onNodeWithText("No sessions logged yet!").assertExists()

        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // 8. Go to Manage Plans
        composeTestRule.onNodeWithText("Leg Day").performClick()
        composeTestRule.onNodeWithText("Manage Plans...").performClick()

        // 9. Click Edit on Leg Day plan
        composeTestRule.onNodeWithContentDescription("Edit Plan Exercises").performClick()

        // Verify Squat is listed in plan
        composeTestRule.onNodeWithText("Squat").assertExists()

        // Click Remove on Squat
        composeTestRule.onNodeWithContentDescription("Remove from Plan").performClick()

        // Verify confirmation dialog and click Remove
        composeTestRule.onNodeWithText("Remove").performClick()

        // Verify Squat is removed and plan empty state is shown
        composeTestRule.onNodeWithText("No exercises in this plan!").assertExists()

        // Go back to Plans list
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        // Go back to main screen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }
}
