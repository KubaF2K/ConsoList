package pl.kubaf2k.consolist.dataclasses

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceEntity(
    val device: Device,
    val model: Model,
    val modelNumber: String,
    val condition: String,
    val location: Location? = null,
    val imageHashes: List<Int> = emptyList(),
    val accessories: List<AccessoryEntity> = emptyList()
): Parcelable {
    constructor(
        device: Device,
        model: Model,
        modelNumberIndex: Int,
        condition: String,
        location: Location? = null,
        imageHashes: List<Int> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, model, model.modelNumbers[modelNumberIndex], condition, location, imageHashes, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumber: String,
        condition: String,
        location: Location? = null,
        imageHashes: List<Int> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], modelNumber, condition, location, imageHashes, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumberIndex: Int,
        condition: String,
        location: Location? = null,
        imageHashes: List<Int> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], device.models[modelIndex].modelNumbers[modelNumberIndex], condition, location, imageHashes, accessories)
}

@Parcelize
data class AccessoryEntity(
    val device: Accessory,
    val condition: String,
    val imageHashes: List<Int>
): Parcelable