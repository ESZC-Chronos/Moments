package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.Moment
import com.example.viewmodel.MomentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen(
    viewModel: MomentsViewModel,
    onMomentClick: (Int) -> Unit
) {
    val moments by viewModel.allMoments.collectAsState()
    var selectedPack by remember { mutableStateOf<String?>(null) }
    var isZoomedIn by remember { mutableStateOf(false) }
    
    val availablePacks = remember(moments) {
        moments.map { it.packName }.distinct()
    }
    
    val filteredMoments = remember(moments, selectedPack) {
        if (selectedPack == null) moments else moments.filter { it.packName == selectedPack }
    }

    val pagerState = rememberPagerState(pageCount = { filteredMoments.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CORE IDENTITY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            "Your Gallery", 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = { isZoomedIn = !isZoomedIn }) {
                        Icon(
                            imageVector = if (isZoomedIn) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = "Toggle Zoom"
                        )
                    }
                }
                
                if (availablePacks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedPack == null,
                                onClick = { 
                                    selectedPack = null 
                                    coroutineScope.launch { pagerState.scrollToPage(0) }
                                },
                                label = { Text("All") }
                            )
                        }
                        lazyItems(availablePacks) { pack ->
                            FilterChip(
                                selected = selectedPack == pack,
                                onClick = { 
                                    selectedPack = pack
                                    coroutineScope.launch { pagerState.scrollToPage(0) }
                                },
                                label = { Text(pack) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (filteredMoments.isNotEmpty()) {
                if (isZoomedIn) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        PolaroidCard(



                            moment = filteredMoments[page],
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            modifier = Modifier.clickable { onMomentClick(filteredMoments[page].id) }
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredMoments) { moment ->
                            PolaroidThumbnail(
    
    

                                moment = moment,
                                onFavoriteClick = { viewModel.toggleFavorite(it) },
                                modifier = Modifier.clickable { 
                                    coroutineScope.launch {
                                        val index = filteredMoments.indexOf(moment)
                                        if (index >= 0) {
                                            pagerState.scrollToPage(index)
                                            isZoomedIn = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No memories archived yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PolaroidCard(

    moment: Moment,
    onFavoriteClick: (Moment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 8.dp)) {
            Box {
                AsyncImage(
                    model = Uri.parse(moment.photoUri),
                    colorFilter = com.example.ui.components.ImageFilters.getFilter(moment.filterName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.LightGray)
                )
                IconButton(
                    onClick = { onFavoriteClick(moment) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (moment.isFavorite) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (moment.isFavorite) Color.Red else Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = moment.questTitle,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(moment.timestamp)),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
            )
        }
    }
}

@Composable
fun PolaroidThumbnail(
    
    moment: Moment,
    onFavoriteClick: (Moment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp).padding(bottom = 4.dp)) {
            Box {
                AsyncImage(
                    model = Uri.parse(moment.photoUri),
                    colorFilter = com.example.ui.components.ImageFilters.getFilter(moment.filterName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.LightGray)
                )
                IconButton(
                    onClick = { onFavoriteClick(moment) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                ) {
                    Icon(
                        imageVector = if (moment.isFavorite) androidx.compose.material.icons.Icons.Default.Favorite else androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (moment.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = moment.questTitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Black),
                maxLines = 1
            )
        }
    }
}
