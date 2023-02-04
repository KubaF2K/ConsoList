package pl.kubaf2k.consolist.ui.devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import pl.kubaf2k.consolist.getBitmapFromURL

class ExistingDeviceEntitiesAdapter(private val deviceEntities: List<DeviceEntity>): RecyclerView.Adapter<DeviceEntityViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceEntityViewHolder {
        this.parent = parent
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceEntityRow = layoutInflater.inflate(R.layout.device_instance_row, parent, false)
        return DeviceEntityViewHolder(deviceEntityRow)
    }

    override fun getItemCount(): Int {
        return deviceEntities.size
    }

    override fun onBindViewHolder(holder: DeviceEntityViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.deviceNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.deviceDescTextView)
        val editBT: Button = holder.itemView.findViewById(R.id.editButton)
        val deleteBT: Button = holder.itemView.findViewById(R.id.deleteButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)
        val accessoriesHeaderText: TextView = holder.itemView.findViewById(R.id.accessoriesHeaderText)
        val accessoriesText: TextView = holder.itemView.findViewById(R.id.accessoriesTextView)

        val deviceEntity = deviceEntities[holder.adapterPosition]

        editBT.visibility = View.GONE
        deleteBT.visibility = View.GONE

        var nameText = "${deviceEntity.device.manufacturer} ${deviceEntity.device.name}"
        if (deviceEntity.device.models.size > 1 || deviceEntity.model.modelNumbers.size > 1)
            nameText += " (${deviceEntity.modelNumber})"
        if (nameText.length > 40)
            nameText = "${nameText.slice(0..39)}..."
        name.text = nameText

        val descText = "${parent.resources.getString(R.string.condition)}: ${deviceEntity.condition}"
        description.text = descText

        if (deviceEntity.imageHashes.isNotEmpty()) {
            image.setImageBitmap(MainActivity.cachedLocalImages[deviceEntity.imageHashes[0]])
        } else {
            parent.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                getBitmapFromURL(deviceEntity.model.imgURL)?.let {
                    image.setImageBitmap(it)
                }
            }
        }

        if (deviceEntity.accessories.isNotEmpty()) {
            accessoriesHeaderText.visibility = View.VISIBLE
            accessoriesText.visibility = View.VISIBLE

            val accessoriesString = StringBuilder()
            for (accessory in deviceEntity.accessories)
                accessoriesString.append("${accessory.device.name} (${accessory.device.modelNumber}): ${accessory.condition}\n")
            accessoriesString.deleteCharAt(accessoriesString.length-1)

            accessoriesText.text = accessoriesString.toString()
        }
    }
}
class DeviceEntityViewHolder(view: View): RecyclerView.ViewHolder(view)