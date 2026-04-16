package io.github.zxus.zxuslauncher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.zxus.zxuslauncher.ui.viewmodel.LauncherViewModel
import io.github.zxus.zxuslauncher.utils.LauncherUtils
import kotlinx.coroutines.launch

@Composable
fun LauncherPager(
    viewModel: LauncherViewModel,
    drawerViewModel: io.github.zxus.zxuslauncher.ui.viewmodel.AppDrawerViewModel,
    onOpenSettings: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Handle back button to return to Home screen (page 1)
    BackHandler(enabled = pagerState.currentPage != 1) {
        scope.launch {
            pagerState.animateScrollToPage(1)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> RSSFeedScreen()
            1 -> HomeScreen(
                viewModel = viewModel,
                onSwipeUp = { LauncherUtils.openWebSearch(context) },
                onSwipeDown = { LauncherUtils.expandNotifications(context) },
                onLongPress = onOpenSettings
            )
            2 -> AppDrawerScreen()
        }
    }
}
