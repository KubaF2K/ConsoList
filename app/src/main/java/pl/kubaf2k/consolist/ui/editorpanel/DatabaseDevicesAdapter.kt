package pl.kubaf2k.consolist.ui.editorpanel

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
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.getBitmapFromURL

class DatabaseDevicesAdapter(activityCaller: ActivityResultCaller): RecyclerView.Adapter<DatabaseDevicesViewHolder>() {
    private lateinit var parent: ViewGroup

    private val saveOrEditRequest = activityCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        EditorPanelActivity.instance.updateList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatabaseDevicesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val deviceRow = layoutInflater.inflate(R.layout.database_device_row, parent, false)
        this.parent = parent
        return DatabaseDevicesViewHolder(deviceRow)
    }

    override fun getItemCount(): Int {
        return EditorPanelActivity.instance.devices.size
    }

    override fun onBindViewHolder(holder: DatabaseDevicesViewHolder, position: Int) {
        val name: TextView = holder.itemView.findViewById(R.id.deviceNameTextView)
        val description: TextView = holder.itemView.findViewById(R.id.deviceDescTextView)
        val editBT: Button = holder.itemView.findViewById(R.id.editButton)
        val deleteBT: Button = holder.itemView.findViewById(R.id.deleteButton)
        val image: ImageView = holder.itemView.findViewById(R.id.deviceImageView)

        val device = EditorPanelActivity.instance.devices[holder.adapterPosition]

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

        editBT.setOnClickListener {
            saveOrEditRequest.launch(
                Intent(parent.context, DatabaseEntryActivity::class.java).apply {
                    putExtra("pl.kubaf2k.consolist.index", holder.adapterPosition)
                }
            )
        }
        deleteBT.setOnClickListener {
            MainActivity.instance.db.collection("devices")
                .whereEqualTo("manufacturer", device.manufacturer)
                .whereEqualTo("name", device.name)
                .whereEqualTo("releaseYear", device.releaseYear)
                .get()
                .addOnSuccessListener {
                    for (document in it) {
                        document.reference.delete()
                    }
                    EditorPanelActivity.instance.updateList()
                }
        }
    }
}

class DatabaseDevicesViewHolder(view: View): RecyclerView.ViewHolder(view)