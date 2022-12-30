package pl.kubaf2k.consolist.dataclasses

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.Serializable

data class DeviceEntity(
    val device: Device,
    val model: Model,
    val modelNumber: String,
    val condition: String,
    val location: Pair<Double, Double>? = null,
    val images: List<SerializableBitmap> = emptyList(),
    val accessories: List<AccessoryEntity> = emptyList()
): Serializable {
    constructor(
        device: Device,
        model: Model,
        modelNumberIndex: Int,
        condition: String,
        location: Pair<Double, Double>? = null,
        images: List<SerializableBitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, model, model.modelNumbers[modelNumberIndex], condition, location, images, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumber: String,
        condition: String,
        location: Pair<Double, Double>? = null,
        images: List<SerializableBitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], modelNumber, condition, location, images, accessories)
    constructor(
        device: Device,
        modelIndex: Int,
        modelNumberIndex: Int,
        condition: String,
        location: Pair<Double, Double>? = null,
        images: List<SerializableBitmap> = emptyList(),
        accessories: List<AccessoryEntity> = emptyList()
    ) : this(device, device.models[modelIndex], device.models[modelIndex].modelNumbers[modelNumberIndex], condition, location, images, accessories)
}

data class AccessoryEntity(
    val device: Accessory,
    val condition: String,
    val images: List<SerializableBitmap>
): Serializable

class SerializableBitmap(var bitmap: Bitmap): Serializable {
    private fun writeObject(outStream: java.io.ObjectOutputStream) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        outStream.writeInt(byteArray.size)
        outStream.write(byteArray)
    }
    private fun readObject(inStream: java.io.ObjectInputStream) {
        val bufferLength = inStream.readInt()
        val byteArray = ByteArray(bufferLength)
        var pos = 0
        do {
            val read = inStream.read(byteArray, pos, bufferLength - pos)
            if (read != -1)
                pos += read
            else
                break
        } while (pos < bufferLength)
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength)
    }
}