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
import com.panosdim.annualleaves.models.Leave
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

const val LEAVES = "leaves"
const val INFO = "info"
const val TOTAL_ANNUAL_LEAVES = "total-annual-leaves"
const val STARTING_YEAR = "starting-year"

class Repository {
    private val user = Firebase.auth.currentUser
    private val database = Firebase.database
    private var listeners: MutableMap<DatabaseReference, ValueEventListener> = mutableMapOf()

    fun years(): Flow<List<Int>> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child(INFO).child(STARTING_YEAR) }
        val today = LocalDate.now()

        dbRef?.get()?.addOnSuccessListener {
            if (it.exists()) {
                val startYear = it.value as Long
                val endYear = today.plusYears(1).year
                trySend((startYear.toInt()..endYear).toList())
            } else {
                dbRef.setValue(today.year)
                trySend(listOf(today.year, today.plusYears(1).year))
            }
        }?.addOnFailureListener {
            Log.e(TAG, "Error getting data", it)
        }

        awaitClose {
            channel.close()
        }
    }

    fun getTotalAnnualLeaves(): Flow<Int> = callbackFlow {
        val dbRef =
            user?.let { database.getReference(it.uid).child(INFO).child(TOTAL_ANNUAL_LEAVES) }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val total = snapshot.value as Long
                    trySend(total.toInt())
                } else {
                    dbRef.setValue(20)
                    trySend(20)
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

    fun setTotalAnnualLeaves(total: Int): Flow<Boolean> = callbackFlow {
        val dbRef =
            user?.let { database.getReference(it.uid).child(INFO).child(TOTAL_ANNUAL_LEAVES) }

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

    fun getLeaves(year: String): Flow<List<Leave>> = callbackFlow {
        val dbRef =
            user?.let { database.getReference(it.uid).child(LEAVES).child(year) }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaves = snapshot.children.mapNotNull { dataSnapshot ->
                    val itm = dataSnapshot.getValue(Leave::class.java)
                    itm?.id = dataSnapshot.key.toString()
                    return@mapNotNull itm
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
            database.getReference(it.uid).child(LEAVES).child(year)
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
                database.getReference(it.uid).child(LEAVES).child(year).child(id)
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
                database.getReference(it.uid).child(LEAVES).child(year).child(id)
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

    fun signOut() {
        listeners.let { list ->
            list.forEach {
                it.key.removeEventListener(it.value)
            }
        }
    }
}
