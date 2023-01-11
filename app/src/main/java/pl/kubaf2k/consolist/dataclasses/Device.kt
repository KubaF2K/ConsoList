package pl.kubaf2k.consolist.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
data class Model(
    var name: String,
    var imgURL: URL,
    var modelNumbers: MutableList<String> = mutableListOf()
): Parcelable {
    constructor(): this("", URL("http://example.org"))
}

@Parcelize
data class Accessory(
    var name: String,
    var imgURL: URL,
    var modelNumber: String?,
    var type: AccessoryType
): Parcelable {
    constructor(): this("", URL("http://example.org"), null, AccessoryType.OTHER)

    @Parcelize
    enum class AccessoryType: Parcelable {
        CONTROLLER,
        OTHER
    }
}

@Parcelize
data class Device(
    var name: String,
    var description: String,
    var imgURL: URL,
    var manufacturer: String,
    var releaseYear: Int,
    var models: MutableList<Model> = mutableListOf(),
    var accessories: MutableList<Accessory> = mutableListOf()
): Parcelable {
    constructor(): this("", "", URL("http://example.org"), "", -1)
    //TODO constructor from firebase document
}