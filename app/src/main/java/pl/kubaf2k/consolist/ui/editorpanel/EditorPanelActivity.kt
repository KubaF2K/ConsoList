package pl.kubaf2k.consolist.ui.editorpanel

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.databinding.ActivityEditorPanelBinding
import pl.kubaf2k.consolist.dataclasses.Device

class EditorPanelActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditorPanelBinding
    val devices = mutableListOf<Device>()

    companion object {
        lateinit var instance: EditorPanelActivity
    }

    fun updateList() {
        val length = devices.size
        devices.clear()
        binding.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, length)
        MainActivity.instance.db.collection("devices")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    devices.add(Device(document))
                }
                binding.deviceRecyclerView.adapter?.notifyItemRangeInserted(0, result.size())
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditorPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instance = this

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceRecyclerView.adapter = DatabaseDevicesAdapter(this)
        updateList()

        val searchAndFilterItems = arrayOf(
            getString(R.string.name),
            getString(R.string.manufacturer),
            getString(R.string.release_year),
            getString(R.string.type),
            getString(R.string.generation)
        )

        binding.clearSearchButton.setOnClickListener {
            updateList()
        }

        binding.searchButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.search_dialog, null)
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(R.string.search)
                .setView(dialogView)
                .create()
            val spinner: Spinner = dialogView.findViewById(R.id.attributeSpinner)
            val search: EditText = dialogView.findViewById(R.id.searchEditText)
            val searchButton: Button = dialogView.findViewById(R.id.searchButton)

            val attributeArrayAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                searchAndFilterItems
            )
            spinner.adapter = attributeArrayAdapter

            searchButton.setOnClickListener {
                val field = when (searchAndFilterItems[spinner.selectedItemPosition]) {
                    getString(R.string.name) -> "name"
                    getString(R.string.manufacturer) -> "manufacturer"
                    getString(R.string.release_year) -> "releaseYear"
                    getString(R.string.type) -> "type"
                    getString(R.string.generation) -> "generation"
                    else -> ""
                }
                val collection = MainActivity.instance.db.collection("devices")
                val query = if (field == "releaseYear" || field == "generation")
                    collection.whereEqualTo(field, search.text.toString().toInt())
                else collection.whereEqualTo(field, search.text.toString())

                query.get()
                    .addOnSuccessListener {
                        val devicesCount = devices.size
                        devices.clear()
                        binding.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, devicesCount)
                        for (document in it) {
                            devices.add(Device(document))
                        }
                        binding.deviceRecyclerView.adapter?.notifyItemRangeInserted(0, devices.size)
                        alertDialog.dismiss()
                    }
            }
            alertDialog.show()
        }

        binding.sortButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.sorting)
                .setItems(searchAndFilterItems) { _, which ->
                    when (searchAndFilterItems[which]) {
                        getString(R.string.manufacturer) -> devices.sortBy { it.manufacturer }
                        getString(R.string.release_year) -> devices.sortBy { it.releaseYear }
                        getString(R.string.type) -> devices.sortBy { it.type }
                        getString(R.string.generation) -> devices.sortBy { it.generation }
                        else -> devices.sortBy { it.name }
                    }
                    binding.deviceRecyclerView.adapter?.notifyItemRangeChanged(0, devices.size)
                }.show()
        }

        binding.reverseSortButton.setOnClickListener {
            devices.reverse()
            binding.deviceRecyclerView.adapter?.notifyItemRangeChanged(0, devices.size)
        }

        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, DatabaseEntryActivity::class.java))
        }

    }
}