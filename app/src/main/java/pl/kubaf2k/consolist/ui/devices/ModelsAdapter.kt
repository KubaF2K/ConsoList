package pl.kubaf2k.consolist.ui.devices

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
import pl.kubaf2k.consolist.dataclasses.Model
import pl.kubaf2k.consolist.getBitmapFromURL

class ModelsAdapter(private val models: List<Model>): RecyclerView.Adapter<ModelViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        this.parent = parent
        val layoutInflater = LayoutInflater.from(parent.context)
        val modelRow = layoutInflater.inflate(R.layout.model_view_row, parent, false)
        return ModelViewHolder(modelRow)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val image: ImageView = holder.itemView.findViewById(R.id.modelImageView)
        val nameText: TextView = holder.itemView.findViewById(R.id.modelNameTextView)
        val modelNumbersHeader: TextView = holder.itemView.findViewById(R.id.modelNumbersHeaderTextView)
        val modelNumbers: TextView = holder.itemView.findViewById(R.id.modelNumbersTextView)

        val model = models[holder.adapterPosition]

        parent.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            getBitmapFromURL(model.imgURL)?.let { image.setImageBitmap(it) }
        }
        nameText.text = model.name

        if (model.modelNumbers.size == 1)
            modelNumbersHeader.text = parent.context.resources.getString(R.string.model_number)

        val modelNumbersStringBuilder = StringBuilder()
        for (modelNumber in model.modelNumbers)
            modelNumbersStringBuilder.append(modelNumber).append(", ")
        modelNumbers.text = modelNumbersStringBuilder.dropLast(2)
    }
}
class ModelViewHolder(view: View): RecyclerView.ViewHolder(view)