package pl.kubaf2k.consolist.dataclasses

import android.graphics.Bitmap
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
    val images: List<Bitmap> = emptyList(),
    val accessories: List<AccessoryEntity> = emptyList()
): Parcelable {
    constructor(
        device: Device,
        model: Model,
        modelNumberIndex: Int,
        condition: String,
        location: Location? = null,
        images: List<Bitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, model, model.modelNumbers[modelNumberIndex], condition, location, images, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumber: String,
        condition: String,
        location: Location? = null,
        images: List<Bitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], modelNumber, condition, location, images, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumberIndex: Int,
        condition: String,
        location: Location? = null,
        images: List<Bitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], device.models[modelIndex].modelNumbers[modelNumberIndex], condition, location, images, accessories)
}

@Parcelize
data class AccessoryEntity(
    val device: Accessory,
    val condition: String,
    val images: List<Bitmap>
): Parcelable