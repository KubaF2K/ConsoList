package pl.kubaf2k.consolist.ui.editorpanel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.Model
import java.net.MalformedURLException
import java.net.URL

class ModelsAdapter(private val models: MutableList<Model>): RecyclerView.Adapter<ModelViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        this.parent = parent
        val layoutInflater = LayoutInflater.from(parent.context)
        val modelRow = layoutInflater.inflate(R.layout.model_row, parent, false)
        return ModelViewHolder(modelRow)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val name: EditText = holder.itemView.findViewById(R.id.nameEditText)
        val url: EditText = holder.itemView.findViewById(R.id.imgUrlEditText)
        val modelNumbersRecyclerView: RecyclerView = holder.itemView.findViewById(R.id.modelNumbersRecyclerView)
        val addModelNumberButton: Button = holder.itemView.findViewById(R.id.addModelNumberButton)
        val deleteButton: Button = holder.itemView.findViewById(R.id.deleteButton)

        val model = models[holder.adapterPosition]

        name.setText(model.name)
        name.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener

            if (name.text.isBlank())
                name.setText(model.name)
            else
                model.name = name.text.toString()
        }

        var urlString = if (model.imgURL.toString() != "http://example.org") model.imgURL.toString() else ""

        url.setText(urlString)
        url.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener

            if (url.text.isBlank())
                url.setText(urlString)
            else {
                try {
                    model.imgURL = URL(url.text.toString())
                    urlString = url.text.toString()
                } catch (e: MalformedURLException) {
                    Toast.makeText(parent.context, R.string.invalid_url, Toast.LENGTH_SHORT).show()
                }
            }
        }

        modelNumbersRecyclerView.layoutManager = LinearLayoutManager(parent.context)
        modelNumbersRecyclerView.adapter = ModelNumbersAdapter(model.modelNumbers)

        addModelNumberButton.setOnClickListener {
            model.modelNumbers.add("")
            modelNumbersRecyclerView.adapter?.notifyItemInserted(model.modelNumbers.size-1)
        }

        deleteButton.setOnClickListener {
            val index = holder.adapterPosition
            models.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

class ModelViewHolder(view: View): RecyclerView.ViewHolder(view)