package com.yakirarie.chatapp.classObjects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Group(val uid: String, val groupAdminUID: String, val groupName: String, val groupImageUrl: String, val usersList: ArrayList<User>) :
    Parcelable {
    constructor() : this("","","" ,"", arrayListOf<User>())
}