package pl.kubaf2k.consolist.ui.editorpanel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import pl.kubaf2k.consolist.R

class ModelNumbersAdapter(private val modelNumbers: MutableList<String>): RecyclerView.Adapter<ModelNumberViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelNumberViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val modelNumberRow = layoutInflater.inflate(R.layout.model_number_row, parent, false)
        return ModelNumberViewHolder(modelNumberRow)
    }

    override fun getItemCount(): Int {
        return modelNumbers.size
    }

    override fun onBindViewHolder(holder: ModelNumberViewHolder, position: Int) {
        val editText: EditText = holder.itemView.findViewById(R.id.modelNumberEditText)
        val deleteButton: ImageButton = holder.itemView.findViewById(R.id.deleteButton)

        editText.setText(modelNumbers[holder.adapterPosition])
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus || holder.adapterPosition == -1)
                return@setOnFocusChangeListener

            if (editText.text.isBlank())
                editText.setText(modelNumbers[holder.adapterPosition])
            else
                modelNumbers[holder.adapterPosition] = editText.text.toString()
        }

        deleteButton.setOnClickListener {
            if (modelNumbers.size <= 1)
                return@setOnClickListener

            modelNumbers.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }
}

class ModelNumberViewHolder(view: View): RecyclerView.ViewHolder(view)