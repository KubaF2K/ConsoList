package pl.kubaf2k.consolist.dataclasses

import java.net.URL

data class Model(
    val name: String,
    val imgURL: URL,
    val modelNumbers: List<String>
)

data class Accessory(
    val name: String,
    val imgURL: URL,
    val modelNumber: String?
)
data class Device(
    val name: String,
    val description: String,
    val imgURL: URL,
    val manufacturer: String,
    val releaseYear: Int,
    val models: List<Model>,
    val accessories: List<Accessory>
)
