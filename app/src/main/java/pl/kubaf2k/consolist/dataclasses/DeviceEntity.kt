package pl.kubaf2k.consolist.dataclasses

import android.graphics.Bitmap
import android.location.Location

data class DeviceEntity(
    val device: Device,
    val condition: String,
    val location: Location?,
    val images: List<Bitmap>,
    val accessories: List<AccessoryEntity>
)

data class AccessoryEntity(
    val device: Accessory,
    val condition: String,
    val images: List<Bitmap>
)