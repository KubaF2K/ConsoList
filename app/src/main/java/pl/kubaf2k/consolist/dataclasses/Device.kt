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

    constructor(entity: DBDeviceWithModelsAndAccessories): this(
        entity.device,
        entity.models.map { Model(
            it.name,
            it.imgURL,
            it.modelNumbers.toMutableList()
        ) }.toMutableList(),
        entity.accessories.map { Accessory(
            it.name,
            it.imgURL,
            it.modelNumber,
            it.type
        ) }.toMutableList()
    )

    constructor(
        entity: DBDevice,
        models: MutableList<Model> = mutableListOf(),
        accessories: MutableList<Accessory> = mutableListOf()
    ): this(
        entity.name,
        entity.description,
        entity.imgURL,
        entity.manufacturer,
        entity.releaseYear,
        models,
        accessories
    )
}
//TODO db setup