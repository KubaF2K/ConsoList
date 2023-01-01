package pl.kubaf2k.consolist.ui.devices

import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.ListEntryActivity
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.getBitmapFromURL

class DevicesAdapter: RecyclerView.Adapter<DevicesViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceRow = layoutInflater.inflate(R.layout.device_row, parent, false)
        this.parent = parent
        return DevicesViewHolder(deviceRow)
    }

    override fun getItemCount(): Int {
        return MainActivity.devices.size
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.deviceNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.deviceDescTextView)
        val addBT: Button = holder.itemView.findViewById(R.id.addButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)

        val device = MainActivity.devices[holder.adapterPosition]

        name.text = "${device.manufacturer} ${device.name} (${device.releaseYear})"
        if (name.text.length > 30)
            name.text = "${name.text.slice(0..30)}..."
        description.text = device.description
        if (description.text.length > 150)
            description.text = "${description.text.slice(0..150)}..."

        parent.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            getBitmapFromURL(device.imgURL)?.let {
                image.setImageBitmap(it)
            }
        }

        addBT.setOnClickListener {
            val addIntent = Intent(parent.context, ListEntryActivity::class.java).apply {
                putExtra("device", holder.adapterPosition)
            }
            parent.context.startActivity(addIntent)
        }
    }
}

class DevicesViewHolder(view: View): RecyclerView.ViewHolder(view)