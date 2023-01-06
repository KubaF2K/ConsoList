package pl.kubaf2k.consolist.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
data class Model(
    val name: String,
    val imgURL: URL,
    val modelNumbers: List<String>
): Parcelable

@Parcelize
data class Accessory(
    val name: String,
    val imgURL: URL,
    val modelNumber: String?,
    val type: AccessoryType
): Parcelable {
    @Parcelize
    enum class AccessoryType: Parcelable {
        CONTROLLER,
        OTHER
    }
}

@Parcelize
data class Device(
    val name: String,
    val description: String,
    val imgURL: URL,
    val manufacturer: String,
    val releaseYear: Int,
    val models: List<Model>,
    val accessories: List<Accessory>
): Parcelable