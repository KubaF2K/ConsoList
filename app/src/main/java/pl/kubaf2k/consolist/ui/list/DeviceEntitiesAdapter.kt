package pl.kubaf2k.consolist.ui.list

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

class DeviceEntitiesAdapter(activityCaller: ActivityResultCaller): RecyclerView.Adapter<DeviceEntitiesViewHolder>() {
    private lateinit var parent: ViewGroup

    private val editDeviceContract = activityCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val index = result.data?.getIntExtra("pl.kubaf2k.consolist.index", -1) ?: -1

        if (index == -1) return@registerForActivityResult

        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU)
            result.data?.getParcelableExtra<DeviceEntity>("pl.kubaf2k.consolist.resultDevice")
                ?.let {
                    MainActivity.deviceEntities[index] = it
                    notifyItemChanged(index)
                }
        else result.data?.getParcelableExtra(
            "pl.kubaf2k.consolist.resultDevice",
            DeviceEntity::class.java
        )?.let {
            MainActivity.deviceEntities[index] = it
            notifyItemChanged(index)
        }

        saveList(MainActivity.deviceEntities)
    }

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

        editBT.setOnClickListener {
            val editIntent = Intent(parent.context, ListEntryActivity::class.java)
                .putExtra("pl.kubaf2k.consolist.index", holder.adapterPosition)
            editDeviceContract.launch(editIntent)
        }
        deleteBT.setOnClickListener {
            MainActivity.deviceEntities.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            saveList(MainActivity.deviceEntities)
        }
    }
}

class DeviceEntitiesViewHolder(view: View): RecyclerView.ViewHolder(view)