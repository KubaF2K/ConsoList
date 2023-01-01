package pl.kubaf2k.consolist.ui.list

import android.content.Intent
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
import pl.kubaf2k.consolist.ListEntryActivity
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.getBitmapFromURL

class DeviceEntitiesAdapter: RecyclerView.Adapter<DeviceEntitiesViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceEntitiesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceEntityRow = layoutInflater.inflate(R.layout.device_instance_row, parent, false)
        this.parent = parent

        return DeviceEntitiesViewHolder(deviceEntityRow)
    }

    override fun getItemCount(): Int {
        return MainActivity.deviceEntities.size
    }

    override fun onBindViewHolder(holder: DeviceEntitiesViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.deviceNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.deviceDescTextView)
        val editBT: Button = holder.itemView.findViewById(R.id.editButton)
        val deleteBT: Button = holder.itemView.findViewById(R.id.deleteButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)
        val accessoriesHeaderText: TextView = holder.itemView.findViewById(R.id.accessoriesHeaderText)
        val accessoriesText: TextView = holder.itemView.findViewById(R.id.accessoriesTextView)

        val deviceEntity = MainActivity.deviceEntities[holder.adapterPosition]

        name.text = "${deviceEntity.device.manufacturer} ${deviceEntity.device.name} (${deviceEntity.device.releaseYear})"
        if (name.text.length > 30)
            name.text = "${name.text.slice(0..30)}..."

        description.text = "Stan: ${deviceEntity.condition}"

        if (deviceEntity.images.isNotEmpty()) {
            image.setImageBitmap(deviceEntity.images[0].bitmap)
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

        editBT.setOnClickListener {
            val editIntent = Intent(parent.context, ListEntryActivity::class.java).putExtra("index", holder.adapterPosition)
            parent.context.startActivity(editIntent)
        }
        deleteBT.setOnClickListener {
            MainActivity.deviceEntities.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }
}

class DeviceEntitiesViewHolder(view: View): RecyclerView.ViewHolder(view)