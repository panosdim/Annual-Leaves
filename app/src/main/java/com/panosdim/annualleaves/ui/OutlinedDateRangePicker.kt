package com.panosdim.annualleaves.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.panosdim.annualleaves.R
import com.panosdim.annualleaves.paddingLarge
import com.panosdim.annualleaves.utils.fromEpochMilli
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedDateRangePicker(
    state: DateRangePickerState,
    label: String,
    modifier: Modifier
) {
    val openDatePickerDialog = remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) {
        openDatePickerDialog.value = true
    }

    if (openDatePickerDialog.value) {
        Dialog(
            onDismissRequest = {
                openDatePickerDialog.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(verticalArrangement = Arrangement.Top) {
                    // Add a row with "Save" and dismiss actions.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = paddingLarge, end = paddingLarge),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { openDatePickerDialog.value = false }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                        TextButton(
                            onClick = {
                                openDatePickerDialog.value = false
                            },
                            enabled = state.selectedEndDateMillis != null
                        ) {
                            Text(text = stringResource(id = R.string.save))
                        }
                    }

                    DateRangePicker(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                        dateFormatter = CustomDatePickerFormatter(),
                    )
                }
            }
        }
    }

    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = label) },
        readOnly = true,
        interactionSource = source,
        value = showRangeDate(state.selectedStartDateMillis, state.selectedEndDateMillis),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar Icon"
            )
        },
        onValueChange = { },
    )
}

fun showRangeDate(startDateMilli: Long?, endDateMilli: Long?): String {
    if (startDateMilli == null || endDateMilli == null) {
        return ""
    }

    return "${
        startDateMilli.fromEpochMilli().format(displayDateFormatter)
    } - ${endDateMilli.fromEpochMilli().format(displayDateFormatter)}"
}

@OptIn(ExperimentalMaterial3Api::class)
class CustomDatePickerFormatter : DatePickerFormatter {
    override fun formatDate(
        dateMillis: Long?,
        locale: CalendarLocale,
        forContentDescription: Boolean
    ): String? {
        return dateMillis?.let {
            displayDateFormatter.format(
                Instant.ofEpochMilli(it).atZone(
                    ZoneId.systemDefault()
                )
            )
        } ?: run { null }
    }

    override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String? {
        return monthMillis?.let {
            DateTimeFormatter.ofPattern("MMMM yyyy").format(
                Instant.ofEpochMilli(it).atZone(
                    ZoneId.systemDefault()
                )
            )
        } ?: run { null }
    }
}