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
import pl.kubaf2k.consolist.*
import pl.kubaf2k.consolist.dataclasses.DeviceEntity

class DevicesAdapter(activityCaller: ActivityResultCaller): RecyclerView.Adapter<DevicesViewHolder>() {
    private lateinit var parent: ViewGroup

    private val addDeviceEntityContract = activityCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU)
            result.data?.getParcelableExtra<DeviceEntity>(
                "pl.kubaf2k.consolist.resultDevice"
            )?.let { MainActivity.deviceEntities.add(it) }
        else result.data?.getParcelableExtra(
            "pl.kubaf2k.consolist.resultDevice",
            DeviceEntity::class.java
        )?.let { MainActivity.deviceEntities.add(it) }

        saveList(MainActivity.deviceEntities)
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
        val viewBT: Button = holder.itemView.findViewById(R.id.viewButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)

        val device = MainActivity.devices[holder.adapterPosition]

        var nameText = "${device.manufacturer} ${device.name} (${device.releaseYear})"
        if (nameText.length > 30)
            nameText = "${nameText.slice(0..30)}..."
        var descText = device.description
        if (descText.length > 150)
            descText = "${descText.slice(0..150)}..."
        name.text = nameText
        description.text = descText

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
        viewBT.setOnClickListener {
            val viewIntent = Intent(parent.context, DeviceViewActivity::class.java)
                .putExtra("pl.kubaf2k.consolist.device", holder.adapterPosition)
            parent.context.startActivity(viewIntent)
        }
    }
}

class DevicesViewHolder(view: View): RecyclerView.ViewHolder(view)