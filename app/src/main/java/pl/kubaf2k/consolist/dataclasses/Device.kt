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
    var releaseYear: Int,
    var models: MutableList<Model> = mutableListOf(),
    var accessories: MutableList<Accessory> = mutableListOf()
): Parcelable {
    constructor(): this("", "", URL("http://example.org"), "", -1)

    @Suppress("UNCHECKED_CAST")
    constructor(document: QueryDocumentSnapshot): this(
        document.data["name"] as String,
        document.data["description"] as String,
        URL(document.data["imgURL"] as String),
        document.data["manufacturer"] as String,
        (document.data["releaseYear"] as Long).toInt(),
        mutableListOf(),
        mutableListOf()
    ) {
        for (model in document.data["models"] as List<Map<String, Any>>) {
            models.add(Model(
                model["name"] as String,
                URL(model["imgURL"] as String),
                (model["modelNumbers"] as List<String>?)?.toMutableList() ?: mutableListOf()
            ))
        }
        if (document.data["accessories"] != null)
            for (accessory in (document.data["accessories"] as List<Map<String, Any>>)) {
                accessories.add(Accessory(
                    accessory["name"] as String,
                    URL(accessory["imgURL"] as String),
                    accessory["modelNumber"] as String,
                    Accessory.AccessoryType.valueOf(accessory["type"] as String)
                ))
            }
    }
}