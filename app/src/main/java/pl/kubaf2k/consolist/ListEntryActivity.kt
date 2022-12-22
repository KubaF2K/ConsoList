package pl.kubaf2k.consolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.databinding.ActivityListEntryBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.DeviceEntity

class ListEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListEntryBinding
    private lateinit var device: Device
    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { bundle ->
            bundle.get("index")?.let {
                index = it as Int
                device = MainActivity.deviceEntities[index].device
            }
            bundle.get("device")?.let {
                device = MainActivity.devices[it as Int]
            }
        }

        lifecycleScope.launch {
            getBitmapFromURL(device.imgURL)?.let{
                binding.deviceImageView.setImageBitmap(it)
            }
        }

        binding.consoleNameTextView.text = "${device.manufacturer} ${device.name}"

        if (index != -1) {
            val deviceEntity = MainActivity.deviceEntities[index]

            binding.conditionEditText.setText(deviceEntity.condition)
        }

        binding.saveButton.setOnClickListener {
            val deviceEntity = DeviceEntity(
                device,
                binding.conditionEditText.text.toString(),
                null, // TODO
                emptyList(),
                emptyList()
            )

            if (index == -1)
                MainActivity.deviceEntities.add(deviceEntity)
            else
                MainActivity.deviceEntities[index] = deviceEntity
            finish()
        }
    }
}