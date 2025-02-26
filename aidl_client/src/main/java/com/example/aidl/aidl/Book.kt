package com.example.aidl.aidl

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Book(
    val bookId: Int,
    val bookName: String?
) : Parcelable