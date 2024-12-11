package com.panosdim.annualleaves.data


import androidx.lifecycle.ViewModel
import com.panosdim.annualleaves.models.Leave
import com.panosdim.annualleaves.utils.TabNames


class MainViewModel : ViewModel() {
    private val repository = Repository()

    fun getAnnualLeaves(year: String) =
        repository.getLeaves(year, TabNames.ANNUAL_LEAVES)

    fun getParentalLeaves(year: String) =
        repository.getLeaves(year, TabNames.PARENTAL_LEAVES)

    fun getTotalAnnualLeavesPerYear(year: String) =
        repository.getTotalLeavesPerYear(year, TabNames.ANNUAL_LEAVES)

    fun getTotalParentalLeavesPerYear(year: String) =
        repository.getTotalLeavesPerYear(year, TabNames.PARENTAL_LEAVES)

    fun setTotalAnnualLeavesPerYear(year: String, total: Int) =
        repository.setTotalLeavesPerYear(year, total, TabNames.ANNUAL_LEAVES)

    fun setTotalParentalLeavesPerYear(year: String, total: Int) =
        repository.setTotalLeavesPerYear(year, total, TabNames.PARENTAL_LEAVES)

    fun annualLeavesYears() = repository.years(TabNames.ANNUAL_LEAVES)
    fun parentalLeavesYears() = repository.years(TabNames.PARENTAL_LEAVES)

    fun addLeave(year: String, leave: Leave) = repository.addLeave(year, leave)

    fun updateLeave(year: String, leave: Leave) = repository.updateLeave(year, leave)

    fun deleteLeave(year: String, leave: Leave) = repository.deleteLeave(year, leave)
}