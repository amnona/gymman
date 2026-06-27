package com.example.gymtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gymtracker.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
  private val requestNotifPermission = registerForActivityResult(RequestPermission()) { granted ->
    // If not granted, we simply continue; notification permission can be requested again later when needed
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Request notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val hasPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
      if (!hasPerm) {
        // Optionally show rationale here
        requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
    enableEdgeToEdge()
    setContent {
      GymTrackerTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}
