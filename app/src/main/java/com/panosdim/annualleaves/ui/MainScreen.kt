package com.panosdim.annualleaves.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.annualleaves.LoginActivity
import com.panosdim.annualleaves.R
import com.panosdim.annualleaves.data.MainViewModel
import com.panosdim.annualleaves.paddingLarge
import com.panosdim.annualleaves.paddingSmall
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onComplete: BroadcastReceiver) {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()
    val today = LocalDate.now()
    val scope = rememberCoroutineScope()

    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val settingsSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    var selectedYear by remember { mutableIntStateOf(today.year) }
    var expandedYear by remember { mutableStateOf(false) }
    val optionsYear = viewModel.years().collectAsStateWithLifecycle(initialValue = emptyList())

    val totalAnnualLeaves =
        viewModel.getTotalAnnualLeaves().collectAsStateWithLifecycle(initialValue = 20)

    val leaves by viewModel.getLeaves(selectedYear.toString())
        .collectAsStateWithLifecycle(initialValue = null)

    leaves?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .systemBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    scope.launch { settingsSheetState.show() }
                }) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(id = R.string.settings)
                    )
                }

                FilledTonalButton(
                    onClick = {
                        viewModel.signOut()
                        context.unregisterReceiver(onComplete)
                        Firebase.auth.signOut()
                        (context as? Activity)?.finish()

                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(id = R.string.logout)
                    )
                }
            }

            // Show Leaves
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = paddingLarge, vertical = paddingLarge),
                state = listState
            ) {
                if (it.isNotEmpty()) {
                    it.iterator().forEachRemaining {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(it.from + " - " + it.until)
                                },
                                trailingContent = {
                                    Text(
                                        text = it.days.toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                },
                                modifier = Modifier.clickable {
                                    //TODO Show edit leave modal
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = stringResource(id = R.string.no_leaves),
                                modifier = Modifier

                            )
                            Text(
                                text = stringResource(id = R.string.no_leaves)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.size(paddingLarge))
            Card(
                modifier = Modifier
                    .padding(paddingLarge)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = paddingLarge),
                        text = stringResource(id = R.string.remaining_annual_leaves),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(paddingLarge),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(paddingSmall),
                            text = totalAnnualLeaves.value.toString(),
                            fontWeight = FontWeight.Bold
                        )
                        VerticalDivider(Modifier.padding(paddingSmall))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .weight(1f)
                                .padding(paddingSmall),
                            progress = { 1.0f },
                        )
                        Text(modifier = Modifier.padding(paddingSmall), text = "100%")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingLarge),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedYear,
                            onExpandedChange = { expandedYear = !expandedYear },
                        ) {
                            ElevatedFilterChip(
                                modifier = Modifier.menuAnchor(),
                                selected = false,
                                onClick = { },
                                label = { Text(selectedYear.toString()) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                            )
                            ExposedDropdownMenu(
                                expanded = expandedYear,
                                onDismissRequest = { expandedYear = false },
                            ) {
                                optionsYear.value.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.toString()) },
                                        onClick = {
                                            selectedYear = selectionOption
                                            expandedYear = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }

                        Button(onClick = {
                            // TODO: Open Add New Leave Modal
                        }) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                stringResource(id = R.string.new_leave)
                            )
                        }
                    }
                }
            }
        }
    } ?: run {
        ProgressBar()
    }

    SettingsSheet(bottomSheetState = settingsSheetState)
}