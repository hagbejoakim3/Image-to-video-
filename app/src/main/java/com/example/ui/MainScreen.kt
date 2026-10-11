package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AvatarsGalleryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioCreateScreen
import com.example.ui.screens.VideoLibraryScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryBright
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class NavTab(val title: String, val icon: ImageVector) {
    object Studio : NavTab("Créer", Icons.Default.Videocam)
    object Avatars : NavTab("Avatars", Icons.Default.Face)
    object Library : NavTab("Mes Vidéos", Icons.Default.VideoLibrary)
    object Settings : NavTab("Paramètres", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    viewModel: StudioViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf<NavTab>(NavTab.Studio) }
    var activePlayerProjectId by remember { mutableStateOf<Long?>(null) }

    // If viewing player, back returns to Studio or Library
    if (activePlayerProjectId != null) {
        VideoPlayerScreen(
            projectId = activePlayerProjectId!!,
            viewModel = viewModel,
            onBack = {
                activePlayerProjectId = null
            },
            onEditInStudio = {
                activePlayerProjectId = null
                selectedTab = NavTab.Studio
            }
        )
        return
    }

    // Handle back button on sub-tabs
    if (selectedTab != NavTab.Studio) {
        BackHandler {
            selectedTab = NavTab.Studio
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    NavTab.Studio,
                    NavTab.Avatars,
                    NavTab.Library,
                    NavTab.Settings
                )

                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (selectedTab != tab) {
                                viewModel.stopSpeaking()
                                selectedTab = tab
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) StudioSecondary else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = StudioPrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = StudioBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudioBackground)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { currentTab ->
                when (currentTab) {
                    NavTab.Studio -> StudioCreateScreen(
                        viewModel = viewModel,
                        onNavigateToPlayer = { projectId ->
                            activePlayerProjectId = projectId
                        }
                    )
                    NavTab.Avatars -> AvatarsGalleryScreen(
                        viewModel = viewModel,
                        onAvatarSelected = {
                            selectedTab = NavTab.Studio
                        }
                    )
                    NavTab.Library -> VideoLibraryScreen(
                        viewModel = viewModel,
                        onOpenProject = { projectId ->
                            activePlayerProjectId = projectId
                        },
                        onNavigateToCreate = {
                            selectedTab = NavTab.Studio
                        }
                    )
                    NavTab.Settings -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
