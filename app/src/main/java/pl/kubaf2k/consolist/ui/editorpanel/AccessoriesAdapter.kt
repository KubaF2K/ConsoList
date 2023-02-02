package pl.kubaf2k.consolist.ui.editorpanel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.dataclasses.Accessory
import java.net.MalformedURLException
import java.net.URL

class AccessoriesAdapter(private val accessories: MutableList<Accessory>): RecyclerView.Adapter<AccessoryViewHolder>() {
    private lateinit var parent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccessoryViewHolder {
        this.parent = parent
        val layoutInflater = LayoutInflater.from(parent.context)
        val accessoryRow = layoutInflater.inflate(R.layout.accessory_row, parent, false)
        return AccessoryViewHolder(accessoryRow)
    }

    override fun getItemCount(): Int {
        return accessories.size
    }

    override fun onBindViewHolder(holder: AccessoryViewHolder, position: Int) {
        val name: EditText = holder.itemView.findViewById(R.id.nameEditText)
        val url: EditText = holder.itemView.findViewById(R.id.imgUrlEditText)
        val modelNumber: EditText = holder.itemView.findViewById(R.id.modelNumberEditText)
        val type: Spinner = holder.itemView.findViewById(R.id.accessoryTypeSpinner)
        val deleteButton: Button = holder.itemView.findViewById(R.id.deleteButton)

        val accessory = accessories[holder.adapterPosition]

        name.setText(accessory.name)
        name.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener

            if (name.text.isBlank())
                name.setText(accessory.name)
            else
                accessory.name = name.text.toString()
        }

        var urlString = if (accessory.imgURL.toString() != "http://example.org") accessory.imgURL.toString() else ""
        url.setText(urlString)
        url.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener

            if (url.text.isBlank())
                url.setText(urlString)
            else {
                try {
                    accessory.imgURL = URL(url.text.toString())
                    urlString = url.text.toString()
                } catch (e: MalformedURLException) {
                    Toast.makeText(parent.context, R.string.invalid_url, Toast.LENGTH_SHORT).show()
                }
            }
        }

        accessory.modelNumber?.let {
            modelNumber.setText(it)
        }
        modelNumber.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener

            accessory.modelNumber = if (modelNumber.text.isBlank())
                null else modelNumber.text.toString()
        }

        val typeArrayAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(
            parent.context,
            android.R.layout.simple_spinner_item,
            Accessory.AccessoryType.values().map { it.name }
        )
        type.adapter = typeArrayAdapter
        type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                accessory.type = Accessory.AccessoryType.values()[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                accessory.type = Accessory.AccessoryType.OTHER
            }
        }

        deleteButton.setOnClickListener {
            val index = holder.adapterPosition
            accessories.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

class AccessoryViewHolder(view: View): RecyclerView.ViewHolder(view)