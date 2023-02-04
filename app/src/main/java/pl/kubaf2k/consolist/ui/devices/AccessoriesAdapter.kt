package pl.kubaf2k.consolist.ui.devices

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.Accessory
import pl.kubaf2k.consolist.getBitmapFromURL

class AccessoriesAdapter(private val accessories: List<Accessory>): RecyclerView.Adapter<AccessoryViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccessoryViewHolder {
        this.parent = parent
        val layoutInflater = LayoutInflater.from(parent.context)
        val accessoryRow = layoutInflater.inflate(R.layout.accessory_view_row, parent, false)
        return AccessoryViewHolder(accessoryRow)
    }

    override fun getItemCount(): Int {
        return accessories.size
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: AccessoryViewHolder, position: Int) {
        val text: TextView = holder.itemView.findViewById(R.id.accessoryTextView)
        val image: ImageView = holder.itemView.findViewById(R.id.accessoryImageView)

        val accessory = accessories[holder.adapterPosition]

        val textToSet = StringBuilder(accessory.name)
        accessory.modelNumber?.let { textToSet.append(" ($it)") }
        textToSet.append("\n")
            .append(parent.context.resources.getString(
                parent.context.resources.getIdentifier(
                    accessory.type.name.lowercase(),
                    "string",
                    parent.context.packageName
                )
            ))

        text.text = textToSet

        parent.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            getBitmapFromURL(accessory.imgURL)?.let { image.setImageBitmap(it) }
        }
    }
}
class AccessoryViewHolder(view: View): RecyclerView.ViewHolder(view)