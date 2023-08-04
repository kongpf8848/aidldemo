package com.example.aidl.aidl

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Book(val bookId: Int,val bookName: String) : Parcelable