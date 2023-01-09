package pl.kubaf2k.consolist.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
class Model(
    var name: String,
    var imgURL: URL,
    var modelNumbers: MutableList<String>
): Parcelable {
    constructor() : this("", URL("http://example.org"), mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (name != other.name) return false
        if (imgURL != other.imgURL) return false
        if (modelNumbers != other.modelNumbers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + imgURL.hashCode()
        result = 31 * result + modelNumbers.hashCode()
        return result
    }
}

@Parcelize
class Accessory(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Accessory

        if (name != other.name) return false
        if (imgURL != other.imgURL) return false
        if (modelNumber != other.modelNumber) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + imgURL.hashCode()
        result = 31 * result + (modelNumber?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

}

@Parcelize
class Device(
    var name: String,
    var description: String,
    var imgURL: URL,
    var manufacturer: String,
    var releaseYear: Int,
    var models: MutableList<Model>,
    var accessories: MutableList<Accessory>
): Parcelable {
    constructor(): this("", "", URL("http://example.org"), "", -1, mutableListOf(), mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Device

        if (name != other.name) return false
        if (description != other.description) return false
        if (imgURL != other.imgURL) return false
        if (manufacturer != other.manufacturer) return false
        if (releaseYear != other.releaseYear) return false
        if (models != other.models) return false
        if (accessories != other.accessories) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + imgURL.hashCode()
        result = 31 * result + manufacturer.hashCode()
        result = 31 * result + releaseYear
        result = 31 * result + models.hashCode()
        result = 31 * result + accessories.hashCode()
        return result
    }


}