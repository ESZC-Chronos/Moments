package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PackManagerScreen
import com.example.ui.screens.PackScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.MomentsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MomentsApp(viewModel: MomentsViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val showBottomBar = currentRoute in listOf("home", "archive", "packs", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onCameraClick = { questId, packName ->
                        navController.navigate("camera/${questId}?packName=$packName")
                    }
                )
            }
            
            composable("archive") {
                ArchiveScreen(
                    viewModel = viewModel,
                    onMomentClick = { momentId ->
                        navController.navigate("memory/$momentId")
                    }
                )
            }
            
            composable("packs") {
                PackManagerScreen(viewModel = viewModel)
            }
            
            composable("profile") {
                val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    com.journeyapps.barcodescanner.ScanContract()
                ) { result ->
                    if (result.contents != null) {
                        // Handle QR result (Friend's profile JSON)
                        viewModel.addFriendFromQr(result.contents)
                    }
                }
                
                ProfileScreen(
                    viewModel = viewModel,
                    onScanClick = {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Scan a friend's Profile QR")
                        options.setCameraId(0)
                        options.setBeepEnabled(false)
                        options.setBarcodeImageEnabled(true)
                        scanLauncher.launch(options)
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
            
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            
            composable("pack/{packName}") { backStackEntry ->
                val packName = backStackEntry.arguments?.getString("packName") ?: ""
                PackScreen(
                    packName = packName,
                    viewModel = viewModel,
                    onBack = { navController.navigateUp() },
                    onQuestClick = { quest ->
                        navController.navigate("camera/${quest.id}?packName=$packName")
                    }
                )
            }
            
            composable("camera/{questId}?packName={packName}") { backStackEntry ->
                val questId = backStackEntry.arguments?.getString("questId") ?: ""
                val packName = backStackEntry.arguments?.getString("packName") ?: ""
                
                val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
                
                LaunchedEffect(Unit) {
                    if (!cameraPermissionState.status.isGranted) {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                
                if (cameraPermissionState.status.isGranted) {
                    CameraScreen(
                        onBack = { navController.navigateUp() },
                        onPhotoTaken = { uri, filterName ->
                            viewModel.saveMomentById(questId, packName, uri.toString(), filterName)
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Camera permission required.")
                    }
                }
            }
            
            composable("memory/{momentId}") { backStackEntry ->
                val momentId = backStackEntry.arguments?.getString("momentId")?.toIntOrNull() ?: 0
                MemoryScreen(
                    viewModel = viewModel,
                    momentId = momentId,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
