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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.annualleaves.R
import com.panosdim.annualleaves.data.MainViewModel
import com.panosdim.annualleaves.models.ParentalLeave
import com.panosdim.annualleaves.paddingLarge
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
fun EditParentalLeaveSheet(
    year: Int,
    leave: ParentalLeave,
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    var isLoading by remember {
        mutableStateOf(false)
    }

    val openDeleteDialog = remember { mutableStateOf(false) }

    val leaveDate = leave.date.toLocalDate()

    val initialDisplayMonthMillis = leaveDate.toEpochMilli()

    // Sheet content
    if (bottomSheetState.isVisible) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = leaveDate.toEpochMilli(),
                yearRange = IntRange(year, year + 1),
                initialDisplayedMonthMillis = initialDisplayMonthMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date =
                            Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        val nextYearDate = LocalDate.of(year + 1, Month.MARCH, 31)

                        val holidays = getHolidays(year)

                        return (date.isBefore(nextYearDate) || date.isEqual(nextYearDate))
                                && !holidays.contains(date)
                                && date.dayOfWeek != DayOfWeek.SATURDAY
                                && date.dayOfWeek != DayOfWeek.SUNDAY
                    }
                })
        var childName by remember { mutableStateOf(leave.childName) }

        fun isValid(): Boolean {
            // Check if we change something in the object
            datePickerState.selectedDateMillis?.let { date ->
                if (leave.date != date.toLocalDate().toString()) {
                    return true
                }
            }
            if (datePickerState.selectedDateMillis == null) {
                return false
            }
            if (childName.isEmpty()) {
                return false
            }
            if (childName != leave.childName) {
                return true
            }
            return false
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = { Text(text = stringResource(id = R.string.delete_parental_leave_dialog_title)) },
                text = {
                    Text(
                        stringResource(id = R.string.delete_parental_leave_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                            isLoading = true

                            scope.launch {
                                viewModel.deleteLeave(year.toString(), leave)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false

                                            if (it) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.delete_parental_leave_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

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
                    stringResource(id = R.string.edit_parental_leave),
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedDatePicker(
                    state = datePickerState,
                    label = stringResource(id = R.string.parental_leave_date),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = childName,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    onValueChange = { childName = it },
                    label = { Text(stringResource(id = R.string.child_name)) },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingLarge)
                ) {
                    OutlinedButton(
                        onClick = { openDeleteDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.delete))
                    }

                    Button(
                        enabled = isValid(),
                        onClick = {
                            isLoading = true

                            // Update leave object
                            leave.date =
                                datePickerState.selectedDateMillis?.toLocalDate().toString()
                            leave.childName = childName

                            scope.launch {
                                viewModel.updateLeave(year.toString(), leave)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (it) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.update_parental_leave_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
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
                            Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.update))
                    }
                }
            }
        }
    }
}