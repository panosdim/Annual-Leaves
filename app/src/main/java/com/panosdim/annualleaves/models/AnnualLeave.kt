package com.panosdim.annualleaves.models

import kotlinx.serialization.Serializable

@Serializable
data class AnnualLeave(
    override var id: String? = "",
    var from: String = "",
    var until: String = "",
    var days: Int = 0
) : Leave
