package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onBackground,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            selectedTextColor = MaterialTheme.colorScheme.onBackground,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.ViewQuilt, contentDescription = "Home") },
            label = { Text("HOME", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            colors = colors
        )
        NavigationBarItem(
            selected = currentRoute == "archive",
            onClick = { onNavigate("archive") },
            icon = { Icon(Icons.Default.AutoAwesomeMotion, contentDescription = "Archive") },
            label = { Text("ARCHIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            colors = colors
        )
        NavigationBarItem(
            selected = currentRoute == "packs",
            onClick = { onNavigate("packs") },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = "Packs") },
            label = { Text("PACKS", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            colors = colors
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("PROFILE", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            colors = colors
        )
    }
}
