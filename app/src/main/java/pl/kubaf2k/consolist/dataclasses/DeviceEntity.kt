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
class DeviceEntity(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceEntity

        if (device != other.device) return false
        if (model != other.model) return false
        if (modelNumber != other.modelNumber) return false
        if (condition != other.condition) return false
        if (imageHashes != other.imageHashes) return false
        if (accessories != other.accessories) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + modelNumber.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + imageHashes.hashCode()
        result = 31 * result + accessories.hashCode()
        return result
    }
}

@Parcelize
class AccessoryEntity(
    var device: Accessory,
    var condition: String,
    var imageHashes: MutableList<Int>
): Parcelable {
    constructor(): this(Accessory(), "", mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessoryEntity

        if (device != other.device) return false
        if (condition != other.condition) return false
        if (imageHashes != other.imageHashes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + imageHashes.hashCode()
        return result
    }
}