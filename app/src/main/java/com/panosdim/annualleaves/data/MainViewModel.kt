package com.panosdim.annualleaves.data


import androidx.lifecycle.ViewModel
import com.panosdim.annualleaves.models.Leave


class MainViewModel : ViewModel() {
    private val repository = Repository()

    fun getLeaves(year: String) = repository.getLeaves(year)

    fun getTotalAnnualLeaves() = repository.getTotalAnnualLeaves()

    fun setTotalAnnualLeaves(total: Int) = repository.setTotalAnnualLeaves(total)

    fun years() = repository.years()

    fun addLeave(year: String, leave: Leave) = repository.addLeave(year, leave)

    fun updateLeave(year: String, leave: Leave) = repository.updateLeave(year, leave)

    fun deleteLeave(year: String, leave: Leave) = repository.deleteLeave(year, leave)

    fun signOut() {
        repository.signOut()
    }
}