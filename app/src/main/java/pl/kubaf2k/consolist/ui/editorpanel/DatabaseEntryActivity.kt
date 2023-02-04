package pl.kubaf2k.consolist.ui.editorpanel

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.databinding.ActivityDatabaseEntryBinding
import pl.kubaf2k.consolist.dataclasses.Accessory
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.Model
import pl.kubaf2k.consolist.getBitmapFromURL
import java.net.URL
import java.util.*

//TODO refresh list after saving, go back after saving
class DatabaseEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDatabaseEntryBinding
    private var index = -1
    private var device: Device? = null
    private var models = mutableListOf<Model>()
    private var accessories = mutableListOf<Accessory>()

    private var imgJob: Job? = null

    private fun updateImage(url: URL) {
        if (imgJob?.isActive == true) imgJob?.cancel()
        imgJob = lifecycleScope.launch {
            getBitmapFromURL(url)?.let {
                binding.deviceImageView.setImageBitmap(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { bundle ->
            index = bundle.getInt("pl.kubaf2k.consolist.index", -1)
            if (index != -1)
                device = EditorPanelActivity.instance.devices[index]
        }

        @SuppressLint("DiscouragedApi")
        val deviceTypeArrayAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Device.DeviceType.values().map {
                resources.getString(
                    resources.getIdentifier(it.name.lowercase(), "string", packageName)
                )
            }
        )
        binding.deviceTypeSpinner.adapter = deviceTypeArrayAdapter

        device?.let {
            binding.deviceManufacturerEditText.setText(it.manufacturer)
            binding.deviceNameEditText.setText(it.name)
            binding.deviceGenerationEditText.setText(it.generation.toString())
            binding.deviceReleaseYearEditText.setText(it.releaseYear.toString())
            binding.deviceTypeSpinner.setSelection(it.type.ordinal)
            binding.deviceDescriptionEditText.setText(it.description)
            binding.deviceImageURLEditText.setText(it.imgURL.toString())
            updateImage(it.imgURL)

            val nameText = "${it.manufacturer} ${it.name} (${it.releaseYear})"
            binding.deviceNameTextView.text = nameText

            models = it.models.map { model -> model.copy() }.toMutableList()
            accessories = it.accessories.map { accessory -> accessory.copy() }.toMutableList()
        }

        binding.modelRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.modelRecyclerView.adapter = ModelsAdapter(models.toMutableList())
        binding.addModelBtn.setOnClickListener {
            models.add(Model().apply { modelNumbers = mutableListOf("") })
            binding.modelRecyclerView.adapter?.notifyItemInserted(models.size-1)
        }

        binding.accessoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.accessoryRecyclerView.adapter = AccessoriesAdapter(accessories.toMutableList())
        binding.addAccessoryBtn.setOnClickListener {
            accessories.add(Accessory())
            binding.accessoryRecyclerView.adapter?.notifyItemInserted(accessories.size-1)
        }

        binding.saveButton.setOnClickListener {
            val newDevice = Device(
                binding.deviceNameEditText.text.toString(),
                binding.deviceDescriptionEditText.text.toString(),
                URL(binding.deviceImageURLEditText.text.toString().ifBlank { "http://example.org" }),
                binding.deviceManufacturerEditText.text.toString(),
                binding.deviceGenerationEditText.text.toString().toInt(),
                binding.deviceReleaseYearEditText.text.toString().toInt(),
                Device.DeviceType.values()[binding.deviceTypeSpinner.selectedItemPosition],
                models.toMutableList(),
                accessories.toMutableList()
            )
            if (device != null) device?.let {
                MainActivity.instance.db.collection("devices")
                    .whereEqualTo("manufacturer", newDevice.manufacturer)
                    .whereEqualTo("name", newDevice.name)
                    .whereEqualTo("releaseYear", newDevice.releaseYear)
                    .get()
                    .addOnSuccessListener { existsQuery ->
                        if (!existsQuery.isEmpty) {
                            for (document in existsQuery) {
                                if (Device(document) != it) {
                                    Toast.makeText(this, R.string.device_already_exists, Toast.LENGTH_SHORT)
                                        .show()
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        MainActivity.instance.db.collection("devices")
                            .whereEqualTo("manufacturer", it.manufacturer)
                            .whereEqualTo("name", it.name)
                            .whereEqualTo("releaseYear", it.releaseYear)
                            .get()
                            .addOnSuccessListener { query ->
                                for (document in query) {
                                    document.reference.set(newDevice)
                                    Toast.makeText(this, R.string.edited_device, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
            } else {
                MainActivity.instance.db.collection("devices")
                    .whereEqualTo("manufacturer", newDevice.manufacturer)
                    .whereEqualTo("manufacturer", newDevice.name)
                    .whereEqualTo("releaseYear", newDevice.releaseYear)
                    .get().addOnSuccessListener { existsQuery ->
                        if (!existsQuery.isEmpty) {
                            Toast.makeText(this, R.string.device_already_exists, Toast.LENGTH_SHORT)
                                .show()
                            return@addOnSuccessListener
                        }
                        MainActivity.instance.db.collection("devices").add(newDevice)
                            .addOnSuccessListener {
                                Toast.makeText(this, R.string.added_device, Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            //TODO update parent list
        }
    }
}