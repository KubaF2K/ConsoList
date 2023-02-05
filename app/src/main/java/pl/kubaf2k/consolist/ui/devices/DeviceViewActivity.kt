package pl.kubaf2k.consolist.ui.devices

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.*
import pl.kubaf2k.consolist.databinding.ActivityDeviceViewBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.DeviceEntity

class DeviceViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceViewBinding
    private lateinit var device: Device
    private var index = -1

    private val addDeviceEntityContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU)
            result.data?.getParcelableExtra<DeviceEntity>(
                "pl.kubaf2k.consolist.resultDevice"
            )?.let { MainActivity.deviceEntities.add(it) }
        else result.data?.getParcelableExtra(
            "pl.kubaf2k.consolist.resultDevice",
            DeviceEntity::class.java
        )?.let { MainActivity.deviceEntities.add(it) }

        saveList(MainActivity.deviceEntities)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { bundle ->
            if (!bundle.containsKey("pl.kubaf2k.consolist.device")) {
                finish()
                return
            }

            index = bundle.getInt("pl.kubaf2k.consolist.device")
            device = MainActivity.devices[index]
        }

        lifecycleScope.launch {
            getBitmapFromURL(device.imgURL)?.let { binding.deviceImageView.setImageBitmap(it) }
        }

        val nameText = "${device.manufacturer} ${device.name} (${device.releaseYear})"
        binding.deviceNameTextView.text = nameText

        binding.deviceAddButton.setOnClickListener {
            val addIntent = Intent(this, ListEntryActivity::class.java)
                .putExtra("pl.kubaf2k.consolist.device", index)
            addDeviceEntityContract.launch(addIntent)
        }
        binding.deviceFindButton.setOnClickListener {
            val link = StringBuilder()
            AlertDialog.Builder(this)
                .setTitle(R.string.select_website)
                .setItems(arrayOf("Allegro", "OLX")) { _, which ->
                    link.append(when (which) {
                        0 -> "https://allegro.pl/listing?string="
                        1 -> "https://www.olx.pl/d/oferty/q-"
                        else -> return@setItems
                    })
                    var deviceString = "${device.manufacturer} ${device.name}"
                    if (which == 1)
                        deviceString = deviceString.replace(' ', '-')
                    link.append(deviceString)
                    val searchIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(link.toString())
                    }
                    startActivity(searchIntent)
                }.show()

//            AlertDialog.Builder(this)
//                    .setTitle(R.string.select_accessory)
//                    .setItems(accArray) { _, which ->
//                        val addIntent = Intent(this, ListEntryActivity::class.java)
//                            .putExtra("pl.kubaf2k.consolist.parent", index)
//                            .putExtra("pl.kubaf2k.consolist.device", which)
//                        addOrEditAccessoryContract.launch(addIntent)
//                    }.show()
        }

        binding.deviceDescriptionTextView.text = device.description
        binding.deviceGenerationTextView.text = when (device.generation) {
            1 -> getString(R.string.first)
            2 -> getString(R.string.second)
            3 -> getString(R.string.third)
            4 -> getString(R.string.fourth)
            5 -> getString(R.string.fifth)
            6 -> getString(R.string.sixth)
            7 -> getString(R.string.seventh)
            8 -> getString(R.string.eighth)
            9 -> getString(R.string.ninth)
            10 -> getString(R.string.tenth)
            else -> getString(R.string.other)
        }

        @SuppressLint("DiscouragedApi")
        binding.deviceTypeTextView.text = getString(resources.getIdentifier(
            device.type.name.lowercase(),
            "string",
            packageName
        ))

        binding.deviceModelsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceModelsRecyclerView.adapter = ModelsAdapter(device.models)

        binding.deviceAccessoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceAccessoriesRecyclerView.adapter = AccessoriesAdapter(device.accessories)

        val existingEntities = MainActivity.deviceEntities.filter { it.device == device }
        if (existingEntities.isEmpty())
            return

        binding.deviceYourDevicesHeaderTextView.visibility = View.VISIBLE
        binding.deviceYourDevicesRecyclerView.visibility = View.VISIBLE
        binding.deviceYourDevicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceYourDevicesRecyclerView.adapter = ExistingDeviceEntitiesAdapter(existingEntities)
    }
}