package com.panosdim.annualleaves.models

import kotlinx.serialization.Serializable

@Serializable
data class ParentalLeave(
    override var id: String? = "",
    var date: String = "",
    var childName: String = "",
) : Leave
