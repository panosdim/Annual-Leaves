package com.panosdim.annualleaves.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.panosdim.annualleaves.R
import com.panosdim.annualleaves.data.MainViewModel
import com.panosdim.annualleaves.paddingLarge
import com.panosdim.annualleaves.paddingSmall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    year: Int,
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val totalAnnualLeaves =
        viewModel.getTotalAnnualLeaves(year.toString())
            .collectAsStateWithLifecycle(initialValue = 20)

    var sliderPosition by remember { mutableFloatStateOf(totalAnnualLeaves.value.toFloat()) }
    sliderPosition = totalAnnualLeaves.value.toFloat()

    // Sheet content
    if (bottomSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    if (sliderPosition.toInt() != totalAnnualLeaves.value) {
                        viewModel.setTotalAnnualLeaves(year.toString(), sliderPosition.toInt())
                            .collect {
                                withContext(Dispatchers.Main) {
                                    if (it) {
                                        Toast.makeText(
                                            context,
                                            R.string.success_set_total_annual_leaves_toast,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        bottomSheetState.hide()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            R.string.error_set_total_annual_leaves_toast,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                    }
                }
            },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingLarge)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingLarge),
                    text = stringResource(id = R.string.total_annual_leaves),
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
                        text = sliderPosition.roundToInt().toString(),
                        fontWeight = FontWeight.Bold
                    )
                    VerticalDivider(Modifier.padding(paddingSmall))
                    Slider(
                        modifier = Modifier.padding(paddingSmall),
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 20f..26f,
                        steps = 5
                    )
                }
            }
        }
    }
}