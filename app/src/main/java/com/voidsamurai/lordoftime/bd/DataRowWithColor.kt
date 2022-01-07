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
    var outdated:Boolean? = false,
    var finished:Int=0
   ):Parcelable

@Parcelize
data class DataRow(
    var id: Int,
    var category: String, var name:String,
    var date: Calendar,
    var workingTime:Float, var priority:Int,
    var currentWorkingTime:Float,
    var finished: Int=0
):Parcelable
@Parcelize
data class RutinesRow(
    var id: Int,
    var task_id: Int,
    var days:String,
    var hours:String,
):Parcelable

@Parcelize
data class OldData(
    var date_id: Long,
    var category: String,
    var workingTime:Float
):Parcelable

