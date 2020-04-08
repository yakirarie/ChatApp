package com.yakirarie.chatapp.classObjects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String, val token:String, val status: String = "\uD83D\uDC4C Available \uD83D\uDC4C") : Parcelable {
    constructor() : this("","","", "")
}
