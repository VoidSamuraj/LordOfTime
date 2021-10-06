package com.voidsamurai.lordoftime.bd

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class DataRowWithColor(
    var id: Int,
    var category: String, var name:String,
    var date: Calendar,
    var workingTime:Float, var priority:Int,
    var currentWorkingTime:Float,
    var color: String,
    var outdated:Boolean? = false
   ):Parcelable

@Parcelize
data class DataRow(
    var id: Int,
    var category: String, var name:String,
    var date: Calendar,
    var workingTime:Float, var priority:Int,
    var currentWorkingTime:Float
):Parcelable

@Parcelize
data class OldData(
    var date_id: Int,
    var category: String,
    var workingTime:Float
):Parcelable

