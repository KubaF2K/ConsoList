package pl.kubaf2k.consolist.ui.editorpanel

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.databinding.ActivityDatabaseEntryBinding
import pl.kubaf2k.consolist.dataclasses.Accessory
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.Model
import pl.kubaf2k.consolist.getBitmapFromURL
import java.net.URL

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

        device?.let {
            binding.deviceManufacturerEditText.setText(it.manufacturer)
            binding.deviceNameEditText.setText(it.name)
            binding.deviceReleaseYearEditText.setText(it.releaseYear.toString())
            binding.deviceDescriptionEditText.setText(it.description)
            binding.deviceImageURLEditText.setText(it.imgURL.toString())
            updateImage(it.imgURL)

            //TODO models
            models = it.models
            //TODO accessories
            accessories = it.accessories
        }

        binding.modelRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.modelRecyclerView.adapter = ModelsAdapter(models)
        binding.addModelBtn.setOnClickListener {
            models.add(Model().apply { modelNumbers = mutableListOf("") })
            binding.modelRecyclerView.adapter?.notifyItemInserted(models.size-1)
        }

        binding.saveButton.setOnClickListener {
            val newDevice = Device(
                binding.deviceNameEditText.text.toString(),
                binding.deviceDescriptionEditText.text.toString(),
                URL(binding.deviceImageURLEditText.text.toString()),
                binding.deviceManufacturerEditText.text.toString(),
                binding.deviceReleaseYearEditText.text.toString().toInt(),
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
                                    Toast.makeText(this, "Device already exists!", Toast.LENGTH_SHORT)
                                        .show()
                                    //TODO string
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
                                    Toast.makeText(this, "Edited device", Toast.LENGTH_SHORT).show()
                                    //TODO string
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
                            Toast.makeText(this, "Device already exists!", Toast.LENGTH_SHORT)
                                .show()
                            //TODO string
                            return@addOnSuccessListener
                        }
                        MainActivity.instance.db.collection("devices").add(newDevice)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Added device", Toast.LENGTH_SHORT).show()
                                //TODO string
                            }
                    }
            }
            //TODO update parent list
        }
    }
}