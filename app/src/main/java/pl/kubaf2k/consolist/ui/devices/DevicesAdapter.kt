package pl.kubaf2k.consolist.ui.devices

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.ListEntryActivity
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import pl.kubaf2k.consolist.getBitmapFromURL

class DevicesAdapter(activityCaller: ActivityResultCaller): RecyclerView.Adapter<DevicesViewHolder>() {
    private lateinit var parent: ViewGroup

    private val addDeviceEntityContract = activityCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        result.data?.getParcelableExtra<DeviceEntity>("pl.kubaf2k.consolist.resultDevice")?.let { MainActivity.deviceEntities.add(it) }
    }

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
            val addIntent = Intent(parent.context, ListEntryActivity::class.java)
                .putExtra("pl.kubaf2k.consolist.device", holder.adapterPosition)
            addDeviceEntityContract.launch(addIntent)
        }
    }
}

class DevicesViewHolder(view: View): RecyclerView.ViewHolder(view)