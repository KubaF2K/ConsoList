package pl.kubaf2k.consolist.dataclasses

import android.os.Parcelable
import com.google.firebase.firestore.QueryDocumentSnapshot
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
    var generation: Int,
    var releaseYear: Int,
    var type: DeviceType,
    var models: MutableList<Model> = mutableListOf(),
    var accessories: MutableList<Accessory> = mutableListOf()
): Parcelable {
    constructor(): this("", "", URL("http://example.org"), "", -1, -1, DeviceType.OTHER)

    @Suppress("UNCHECKED_CAST")
    constructor(document: QueryDocumentSnapshot): this(
        document.data["name"] as String? ?: "",
        document.data["description"] as String? ?: "",
        URL(document.data["imgURL"] as String? ?: "http://example.org"),
        document.data["manufacturer"] as String? ?: "",
        (document.data["generation"] as Long? ?: -1).toInt(),
        (document.data["releaseYear"] as Long? ?: -1).toInt(),
        DeviceType.valueOf(document.data["type"] as String? ?: DeviceType.OTHER.name),
        mutableListOf(),
        mutableListOf()
    ) {
        if (document.data["models"] != null)
            for (model in document.data["models"] as List<Map<String, Any>>) {
                models.add(Model(
                    model["name"] as String? ?: "",
                    URL(model["imgURL"] as String? ?: "http://example.org"),
                    (model["modelNumbers"] as List<String>?)?.toMutableList() ?: mutableListOf()
                ))
            }
        if (document.data["accessories"] != null)
            for (accessory in (document.data["accessories"] as List<Map<String, Any>>)) {
                accessories.add(Accessory(
                    accessory["name"] as String? ?: "",
                    URL(accessory["imgURL"] as String? ?: "http://example.org"),
                    accessory["modelNumber"] as String? ?: "",
                    Accessory.AccessoryType.valueOf(accessory["type"] as String? ?: DeviceType.OTHER.name)
                ))
            }
    }

//    override fun equals(other: Any?): Boolean {
//        if (other !is Device)
//            return false
//
//        if (name != other.name)
//            return false
//        if (description != other.description)
//            return false
//        if (imgURL != other.imgURL)
//            return false
//        if (manufacturer != other.manufacturer)
//            return false
//        if (generation != other.generation)
//            return false
//        if (releaseYear != other.releaseYear)
//            return false
//        if (deviceType != other.deviceType)
//            return false
//
//        if (models.size != other.models.size)
//            return false
//        for (modelIndex in models.indices) {
//            if (models[modelIndex] != other.models[modelIndex])
//                return false
//        }
//
//        if (accessories.size != other.accessories.size)
//             return false
//        for (accessoryIndex in accessories.indices) {
//            if (accessories[accessoryIndex] != other.accessories[accessoryIndex])
//                return false
//        }
//
//        return true
//    }

    @Parcelize
    enum class DeviceType: Parcelable {
        HOME,
        HANDHELD,
        COMPUTER,
        OTHER
    }
}