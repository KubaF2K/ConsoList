package pl.kubaf2k.consolist.dataclasses

import java.net.URL
import java.io.Serializable

data class Model(
    val name: String,
    val imgURL: URL,
    val modelNumbers: List<String>
): Serializable

data class Accessory(
    val name: String,
    val imgURL: URL,
    val modelNumber: String?,
    val type: AccessoryType
): Serializable {
    enum class AccessoryType {
        CONTROLLER,
        OTHER
    }
}
data class Device(
    val name: String,
    val description: String,
    val imgURL: URL,
    val manufacturer: String,
    val releaseYear: Int,
    val models: List<Model>,
    val accessories: List<Accessory>
): Serializable