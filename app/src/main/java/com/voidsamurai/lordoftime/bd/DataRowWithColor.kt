package layout

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
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