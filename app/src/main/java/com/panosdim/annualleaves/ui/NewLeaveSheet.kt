package com.panosdim.annualleaves.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.annualleaves.R
import com.panosdim.annualleaves.data.MainViewModel
import com.panosdim.annualleaves.models.Leave
import com.panosdim.annualleaves.paddingLarge
import com.panosdim.annualleaves.utils.calculateWorkingDays
import com.panosdim.annualleaves.utils.getHolidays
import com.panosdim.annualleaves.utils.toEpochMilli
import com.panosdim.annualleaves.utils.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLeaveSheet(
    year: Int,
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    var isLoading by remember {
        mutableStateOf(false)
    }

    val initialDisplayMonthMillis = if (year == LocalDate.now().year) {
        LocalDate.now().toEpochMilli()
    } else {
        LocalDate.of(year, Month.JANUARY, 1).toEpochMilli()
    }

    // Sheet content
    if (bottomSheetState.isVisible) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = null,
            initialSelectedEndDateMillis = null,
            yearRange = IntRange(year, year + 1),
            initialDisplayedMonthMillis = initialDisplayMonthMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val nextYearDate = LocalDate.of(year + 1, Month.MARCH, 31)

                    val holidays = getHolidays(year)

                    return (date.isBefore(nextYearDate) || date.isEqual(nextYearDate))
                            && !holidays.contains(date)
                            && date.dayOfWeek != DayOfWeek.SATURDAY
                            && date.dayOfWeek != DayOfWeek.SUNDAY
                }
            }
        )

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingLarge)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(id = R.string.new_leave),
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedDateRangePicker(
                    state = state,
                    label = "Annual Leave Dates",
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddingLarge)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        enabled = state.selectedStartDateMillis != null && !isLoading,
                        onClick = {
                            isLoading = true

                            val newLeave = Leave(
                                id = null,
                                from = state.selectedStartDateMillis?.toLocalDate().toString(),
                                until = state.selectedEndDateMillis?.toLocalDate().toString(),
                                days = calculateWorkingDays(
                                    state.selectedStartDateMillis?.toLocalDate(),
                                    state.selectedEndDateMillis?.toLocalDate()
                                )
                            )

                            scope.launch {
                                viewModel.addLeave(year.toString(), newLeave)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            if (it) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.add_leave_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                isLoading = false

                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.create))
                    }
                }
            }
        }
    }
}