package pl.kubaf2k.consolist.dataclasses

import androidx.room.*
import java.net.URL


@Entity(tableName = "devices")
data class DBDevice(
    @PrimaryKey val deviceId: Int,

    val name: String,
    val description: String,
    val imgURL: URL,
    val manufacturer: String,
    val releaseYear: Int
)

@Entity(tableName = "models")
data class DBModel(
    @PrimaryKey val modelId: Int,
    val deviceId: Int,

    val name: String,
    val imgURL: URL,
    val modelNumbers: List<String>
)

@Entity(tableName = "accessories")
data class DBAccessory(
    @PrimaryKey val accessoryId: Int,
    val deviceId: Int,

    val name: String,
    val imgURL: URL,
    val modelNumber: String?,
    val type: Accessory.AccessoryType
)

data class DBDeviceWithModelsAndAccessories(
    @Embedded val device: DBDevice,
    @Relation(
        parentColumn = "deviceId",
        entityColumn = "deviceId"
    )
    val models: List<DBModel>,
    @Relation(
        parentColumn = "deviceId",
        entityColumn = "deviceId"
    )
    val accessories: List<DBAccessory>
)

@Dao
interface ModelDao {
    @Insert
    fun insertAll(vararg devices: DBDevice)
    @Delete
    fun delete(device: DBDevice)
    @Query("SELECT * FROM devices")
    fun getAll(): List<DBDevice>
    @Transaction
    @Query("SELECT * FROM devices")
    fun getAllWithRelations(): List<DBDeviceWithModelsAndAccessories>
}