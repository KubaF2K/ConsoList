package pl.kubaf2k.consolist.ui

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
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.AccessoryEntity
import pl.kubaf2k.consolist.getBitmapFromURL
import java.util.*

class AccessoryEntityAdapter(private val accessories: MutableList<AccessoryEntity>): RecyclerView.Adapter<AccessoryEntityViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccessoryEntityViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceRow = layoutInflater.inflate(R.layout.device_instance_row, parent, false)
        this.parent = parent
        return AccessoryEntityViewHolder(deviceRow)
    }

    override fun getItemCount(): Int {
        return accessories.size
    }

    override fun onBindViewHolder(holder: AccessoryEntityViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.deviceNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.deviceDescTextView)
        val editBT: Button = holder.itemView.findViewById(R.id.editButton)
        val deleteBT: Button = holder.itemView.findViewById(R.id.deleteButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)

        val deviceEntity = accessories[holder.adapterPosition]

        name.text = "${deviceEntity.device.name} (${deviceEntity.device.name})"
        if (name.text.length > 30)
            name.text = "${name.text.slice(0..30)}..."

        description.text = "Typ: ${parent.resources.getString(
            parent.resources.getIdentifier(deviceEntity.device.type.toString().lowercase(Locale.getDefault()),
                "string",
                parent.context.packageName
            )
        )}\nStan: ${deviceEntity.condition}"

        if (deviceEntity.images.isNotEmpty()) {
            image.setImageBitmap(deviceEntity.images[0].bitmap)
        } else {
            parent.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                getBitmapFromURL(deviceEntity.device.imgURL)?.let {
                    image.setImageBitmap(it)
                }
            }
        }

        editBT.setOnClickListener {
            TODO()
        }
        deleteBT.setOnClickListener {
            accessories.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }
}

class AccessoryEntityViewHolder(view: View): RecyclerView.ViewHolder(view)