package pl.kubaf2k.consolist.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Devices")
class WrapperList(
    @field:ElementList
    var list: MutableList<DeviceEntity>
) {
    constructor(): this(mutableListOf())
}

@Parcelize
data class DeviceEntity(
    var device: Device,
    var model: Model,
    var modelNumber: String,
    var condition: String,
//    var location: Location? = null,
    var imageHashes: MutableList<Int> = mutableListOf(),
    var accessories: MutableList<AccessoryEntity> = mutableListOf()
): Parcelable {
    constructor(): this(Device(), Model(), "", "")
    constructor(
        device: Device,
        model: Model,
        modelNumberIndex: Int,
        condition: String,
//        location: Location? = null,
        imageHashes: MutableList<Int> = mutableListOf(),
        accessories: MutableList<AccessoryEntity> = mutableListOf()
    ) : this(device, model, model.modelNumbers[modelNumberIndex], condition,/* location,*/ imageHashes, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumber: String,
        condition: String,
//        location: Location? = null,
        imageHashes: MutableList<Int> = mutableListOf(),
        accessories: MutableList<AccessoryEntity> = mutableListOf()
    ) : this(device, device.models[modelIndex], modelNumber, condition,/* location,*/ imageHashes, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumberIndex: Int,
        condition: String,
//        location: Location? = null,
        imageHashes: MutableList<Int> = mutableListOf(),
        accessories: MutableList<AccessoryEntity> = mutableListOf()
    ) : this(device, device.models[modelIndex], device.models[modelIndex].modelNumbers[modelNumberIndex], condition,/* location,*/ imageHashes, accessories)
}

@Parcelize
data class AccessoryEntity(
    var device: Accessory,
    var condition: String,
    var imageHashes: MutableList<Int> = mutableListOf()
): Parcelable {
    constructor(): this(Accessory(), "")
}