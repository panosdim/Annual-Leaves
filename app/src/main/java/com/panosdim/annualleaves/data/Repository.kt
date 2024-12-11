package com.panosdim.annualleaves.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.panosdim.annualleaves.TAG
import com.panosdim.annualleaves.models.AnnualLeave
import com.panosdim.annualleaves.models.Leave
import com.panosdim.annualleaves.models.ParentalLeave
import com.panosdim.annualleaves.selectedTab
import com.panosdim.annualleaves.utils.TabNames
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

const val TOTAL_LEAVES_PER_YEAR = "total-leaves-per-year"
const val ANNUAL_LEAVES_KEY = "annual"
const val PARENTAL_LEAVES_KEY = "parental"

class Repository {
    private val user = Firebase.auth.currentUser
    private val database = Firebase.database
    private var listeners: MutableMap<DatabaseReference, ValueEventListener> = mutableMapOf()

    private fun getLeavesPath(tab: TabNames): String {
        return when (tab) {
            TabNames.ANNUAL_LEAVES -> ANNUAL_LEAVES_KEY
            TabNames.PARENTAL_LEAVES -> PARENTAL_LEAVES_KEY
        }
    }

    private fun getLeavesClass(tab: TabNames): Class<out Leave> {
        return when (tab) {
            TabNames.ANNUAL_LEAVES -> AnnualLeave::class.java
            TabNames.PARENTAL_LEAVES -> ParentalLeave::class.java
        }
    }

    fun years(tab: TabNames): Flow<List<Int>> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child(getLeavesPath(tab)) }
        val today = LocalDate.now()

        dbRef?.get()?.addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val years = snapshot.children.mapNotNull { it.key?.toIntOrNull() }
                    .sorted() // Sort the years
                val startYear = years.first()
                val endYear = today.plusYears(1).year
                trySend((startYear..endYear).toList())
            } else {
                trySend(listOf(today.year, today.plusYears(1).year))
            }
        }?.addOnFailureListener {
            Log.e(TAG, "Error getting data", it)
        }

        awaitClose {
            channel.close()
        }
    }

    fun getTotalLeavesPerYear(year: String, tab: TabNames): Flow<Int> = callbackFlow {
        val defaultTotalValue = when (tab) {
            TabNames.ANNUAL_LEAVES -> 20
            TabNames.PARENTAL_LEAVES -> 4
        }

        val dbRef =
            user?.let {
                database.getReference(it.uid).child(getLeavesPath(tab)).child(year)
                    .child(TOTAL_LEAVES_PER_YEAR)
            }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val total = snapshot.value as Long
                    trySend(total.toInt())
                } else {
                    // Fetch from previous year using a single read
                    val previousYear = (year.toInt() - 1).toString()
                    val previousYearRef = user?.let {
                        database.getReference(it.uid).child(getLeavesPath(tab))
                            .child(previousYear)
                            .child(TOTAL_LEAVES_PER_YEAR)
                    }

                    previousYearRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(previousSnapshot: DataSnapshot) {
                            val previousYearTotal =
                                if (previousSnapshot.exists()) (previousSnapshot.value as Long).toInt() else defaultTotalValue
                            dbRef.setValue(previousYearTotal) // Set current year value
                            trySend(previousYearTotal)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, error.toString())
                            cancel()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            channel.close()
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    fun setTotalLeavesPerYear(year: String, total: Int, tab: TabNames): Flow<Boolean> =
        callbackFlow {
            val dbRef =
                user?.let {
                    database.getReference(it.uid).child(getLeavesPath(tab)).child(year)
                        .child(TOTAL_LEAVES_PER_YEAR)
                }

            dbRef?.setValue(total)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }
                ?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }

    fun getLeaves(year: String, tab: TabNames): Flow<List<Leave>> = callbackFlow {
        val dbRef =
            user?.let {
                database.getReference(it.uid).child(getLeavesPath(tab)).child(year)
            }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaveClass = getLeavesClass(tab)
                val leaves = snapshot.children.mapNotNull { dataSnapshot ->
                    if (dataSnapshot.key != TOTAL_LEAVES_PER_YEAR) {
                        val itm = dataSnapshot.getValue(leaveClass)
                        itm?.id = dataSnapshot.key.toString()
                        return@mapNotNull itm
                    } else {
                        null
                    }
                }

                trySend(leaves)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            channel.close()
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    fun addLeave(year: String, leave: Leave): Flow<Boolean> = callbackFlow {
        val dbRef = user?.let {
            database.getReference(it.uid).child(getLeavesPath(selectedTab)).child(year)
        }

        dbRef?.push()?.setValue(leave)
            ?.addOnSuccessListener {
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }

    fun updateLeave(year: String, leave: Leave): Flow<Boolean> = callbackFlow {
        val dbRef = user?.let {
            leave.id?.let { id ->
                database.getReference(it.uid).child(getLeavesPath(selectedTab)).child(year)
                    .child(id)
            }
        }
        val parentRef = dbRef?.parent

        dbRef?.removeValue()
        leave.id = null
        parentRef?.push()?.setValue(leave)
            ?.addOnSuccessListener {
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }

    fun deleteLeave(year: String, leave: Leave): Flow<Boolean> = callbackFlow {
        val dbRef = user?.let {
            leave.id?.let { id ->
                database.getReference(it.uid).child(getLeavesPath(selectedTab)).child(year)
                    .child(id)
            }
        }

        dbRef?.removeValue()
            ?.addOnSuccessListener {
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }
}
