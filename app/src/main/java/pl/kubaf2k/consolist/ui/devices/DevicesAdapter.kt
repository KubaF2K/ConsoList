package pl.kubaf2k.consolist.ui.devices

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.getBitmapFromURL

class DevicesAdapter: RecyclerView.Adapter<DevicesViewHolder>() {
    private var lifecycleOwner: LifecycleOwner? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceRow = layoutInflater.inflate(R.layout.device_row, parent, false)
        lifecycleOwner = parent.findViewTreeLifecycleOwner()
        return DevicesViewHolder(deviceRow)
    }

    override fun getItemCount(): Int {
        return MainActivity.devices.size
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.consoleNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.consoleDescTextView)
        val addBT: Button = holder.itemView.findViewById(R.id.addButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)

        val device = MainActivity.devices[holder.adapterPosition]

        name.text = "${device.manufacturer} ${device.name} (${device.releaseYear})"
        if (name.text.length > 30)
            name.text = "${name.text.slice(0..30)}..."
        description.text = device.description
        if (description.text.length > 150)
            description.text = "${description.text.slice(0..150)}..."

        lifecycleOwner?.lifecycleScope?.launch {
            val imgBitmap = getBitmapFromURL(device.imgURL)
            if (imgBitmap != null)
                image.setImageBitmap(imgBitmap)
        }
    }
}

class DevicesViewHolder(view: View): RecyclerView.ViewHolder(view)