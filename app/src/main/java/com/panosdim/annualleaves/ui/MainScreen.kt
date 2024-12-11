package com.panosdim.annualleaves.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.panosdim.annualleaves.selectedTab
import com.panosdim.annualleaves.utils.TabNames
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val tabs = enumValues<TabNames>().map { it.tabName }
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    when (pagerState.currentPage) {
        0 -> selectedTab = TabNames.ANNUAL_LEAVES
        1 -> selectedTab = TabNames.PARENTAL_LEAVES
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    icon = {
                        when (index) {
                            0 -> Icon(
                                imageVector = Icons.Default.BeachAccess,
                                contentDescription = null
                            )

                            1 -> Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> AnnualScreen()
                1 -> ParentalScreen()
            }
        }
    }
}