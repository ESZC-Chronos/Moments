package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import com.example.viewmodel.MomentsViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

import java.util.UUID
import com.example.data.Quest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MomentsViewModel, onScanClick: () -> Unit, onSettingsClick: () -> Unit) {
    val username by viewModel.username.collectAsState()
    val installedPacks by viewModel.installedPacks.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val allMoments by viewModel.allMoments.collectAsState()
    var showCreatePackDialog by remember { mutableStateOf(false) }
    
    val favoriteMoments = remember(allMoments) {
        allMoments.filter { it.isFavorite }
    }
    
    // Create Profile JSON
    val profileJson = remember(username, installedPacks) {
        val json = JSONObject()
        json.put("name", username)
        json.put("packs", org.json.JSONArray(installedPacks))
        json.toString()
    }
    
    val qrBitmap = remember(profileJson) {
        generateQrCode(profileJson)
    }

    if (showCreatePackDialog) {
        CreatePackDialog(
            onDismiss = { showCreatePackDialog = false },
            onCreate = { name, quests ->
                viewModel.createCustomPack(name, quests)
                showCreatePackDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROFILE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            "Your Identity", 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp), 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$streak Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Current Streak", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "${installedPacks.size}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Active Packs", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            // Emblems Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Emblem(title = "7 Days", color = androidx.compose.ui.graphics.Color(0xFFCD7F32), achieved = streak >= 7) // Bronze
                    Emblem(title = "30 Days", color = androidx.compose.ui.graphics.Color(0xFFC0C0C0), achieved = streak >= 30) // Silver
                    Emblem(title = "100 Days", color = androidx.compose.ui.graphics.Color(0xFFFFD700), achieved = streak >= 100) // Gold
                }
            }
            
            // Favorites Section
            if (favoriteMoments.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Favorites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(favoriteMoments) { moment ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(120.dp)
                            ) {
                                AsyncImage(
                                    model = Uri.parse(moment.photoUri),
                                    colorFilter = com.example.ui.components.ImageFilters.getFilter(moment.filterName),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
            
            Button(
                onClick = { showCreatePackDialog = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Create Custom Pack")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Share Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Let others scan this QR to try your packs.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (qrBitmap != null) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Profile QR Code",
                        modifier = Modifier.size(200.dp).padding(16.dp)
                    )
                }
            }
            
            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Scan a Friend's Profile")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun Emblem(title: String, color: androidx.compose.ui.graphics.Color, achieved: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (achieved) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = title,
                tint = if (achieved) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, color = if (achieved) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun generateQrCode(text: String): Bitmap? {
    try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun CreatePackDialog(onDismiss: () -> Unit, onCreate: (String, List<Quest>) -> Unit) {
    var packName by remember { mutableStateOf("") }
    var questTitle by remember { mutableStateOf("") }
    var questDesc by remember { mutableStateOf("") }
    var quests by remember { mutableStateOf(listOf<Quest>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Pack") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = packName,
                    onValueChange = { packName = it },
                    label = { Text("Pack Name") },
                    singleLine = true
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Add Quest", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = questTitle,
                    onValueChange = { questTitle = it },
                    label = { Text("Quest Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = questDesc,
                    onValueChange = { questDesc = it },
                    label = { Text("Quest Description") },
                    maxLines = 3
                )
                Button(
                    onClick = {
                        if (questTitle.isNotBlank() && questDesc.isNotBlank()) {
                            quests = quests + Quest(
                                id = UUID.randomUUID().toString(),
                                title = questTitle,
                                description = questDesc,
                                difficulty = "easy"
                            )
                            questTitle = ""
                            questDesc = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add")
                }
                
                if (quests.isNotEmpty()) {
                    Text("${quests.size} quests added", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(packName, quests) },
                enabled = packName.isNotBlank() && quests.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
