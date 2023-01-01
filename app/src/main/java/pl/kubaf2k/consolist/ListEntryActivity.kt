package pl.kubaf2k.consolist

import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.databinding.ActivityListEntryBinding
import pl.kubaf2k.consolist.dataclasses.*
import pl.kubaf2k.consolist.ui.AccessoryEntityAdapter
import pl.kubaf2k.consolist.ui.list.ListFragment
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ListEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListEntryBinding
    private lateinit var device: Device
    private lateinit var oldEntity: DeviceEntity

    private lateinit var emptyImgIcon: Drawable

    private var model: Model? = null
    private var modelNumber = ""
    private var index: Int = -1
    private val images = LinkedList<SerializableBitmap>()
    private var imgPos = 0
    private val accessories = ArrayList<AccessoryEntity>()

    private var imgJob: Job? = null
    private var tempUri: Uri? = null

    private fun updateButtons() {
        binding.prevImgBtn.isEnabled = imgPos > 0
        binding.nextImgBtn.isEnabled = imgPos <= images.size-1
        binding.delImgBtn.isEnabled = images.isNotEmpty()
        binding.setImgBtn.isEnabled = images.size > 1 && imgPos > 0
        binding.photosTextView.text = getString(R.string.photos) + if (images.isNotEmpty()) " (${imgPos+1}/${images.size})" else ""
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let {
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, it))
        images.add(SerializableBitmap(bitmap))
        imgPos = images.size - 1
        binding.devicePhotoView.setImageBitmap(images[imgPos].bitmap)

        updateButtons()
    }}
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) return@registerForActivityResult

        tempUri?.let { uri ->
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            images.add(SerializableBitmap(bitmap))
            imgPos = images.size - 1
            binding.devicePhotoView.setImageBitmap(images[imgPos].bitmap)

            updateButtons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { bundle ->
            bundle.get("device")?.let {
                device = MainActivity.devices[it as Int]
            }
            bundle.get("index")?.let {
                index = it as Int
                oldEntity = MainActivity.deviceEntities[index]
                device = oldEntity.device
                accessories.addAll(oldEntity.accessories)
            }
        }

        emptyImgIcon = binding.deviceImageView.drawable

        lifecycleScope.launch {
            getBitmapFromURL(device.imgURL)?.let{
                binding.deviceImageView.setImageBitmap(it)
            }
        }

        binding.deviceNameTextView.text = "${device.manufacturer} ${device.name}"

        if (device.models.size > 1) {
            val modelArrayAdapter: ArrayAdapter<CharSequence> =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, device.models.map { it.name })
            binding.modelSpinner.adapter = modelArrayAdapter

            binding.modelSpinner.visibility = View.VISIBLE

            binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    model = device.models[position]
                    model?.let {
                        if (it.modelNumbers.isNotEmpty()) {
                            val modelNumberList = ArrayList<CharSequence>()
                            modelNumberList.addAll(it.modelNumbers)
                            modelNumberList.add(getString(R.string.other))
                            val modelNumberArrayAdapter: ArrayAdapter<CharSequence> =
                                ArrayAdapter(this@ListEntryActivity, android.R.layout.simple_spinner_item, modelNumberList)

                            binding.modelNumberSpinner.adapter = modelNumberArrayAdapter

                            if (index != -1) {
                                val modelNumberIndex = it.modelNumbers.indexOf(oldEntity.modelNumber)

                                if (modelNumberIndex != -1)
                                    binding.modelNumberSpinner.setSelection(modelNumberIndex)
                                else {
                                    binding.modelNumberSpinner.setSelection(it.modelNumbers.size)
                                    binding.modelNumberEditText.setText(oldEntity.modelNumber)
                                }
                            }

                            binding.modelNumberSpinner.visibility = View.VISIBLE

                            binding.modelNumberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    if (position >= it.modelNumbers.size) {
                                        binding.modelNumberEditText.visibility = View.VISIBLE
                                    } else {
                                        binding.modelNumberEditText.visibility = View.GONE
                                        modelNumber = it.modelNumbers[position]
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                    binding.modelNumberEditText.visibility = View.VISIBLE
                                }
                            }
                        }

                        imgJob?.let { job ->
                            if (job.isActive) job.cancel()
                        }

                        imgJob = lifecycleScope.launch {
                            getBitmapFromURL(it.imgURL)?.let { bmp ->
                                binding.deviceImageView.setImageBitmap(bmp)
                            }
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    model = null
                }
            }
            if (index != -1) {
                device.models.indexOf(oldEntity.model).let {
                    if (it != -1) binding.modelSpinner.setSelection(it)
                }
            }
        } else {
            model = device.models[0]
        }

        if (index != -1) {
            val deviceEntity = oldEntity

            binding.conditionEditText.setText(deviceEntity.condition)

            if (oldEntity.images.isNotEmpty()) {
                images.addAll(oldEntity.images)

                binding.devicePhotoView.setImageBitmap(images[imgPos].bitmap)
            }
        }

        binding.prevImgBtn.setOnClickListener {
            if (imgPos <= 0) return@setOnClickListener

            binding.devicePhotoView.setImageBitmap(images[--imgPos].bitmap)
            updateButtons()
        }
        binding.nextImgBtn.setOnClickListener {
            if (imgPos >= images.size-1) return@setOnClickListener

            binding.devicePhotoView.setImageBitmap(images[++imgPos].bitmap)
            updateButtons()
        }
        binding.setImgBtn.setOnClickListener {
            if (images.size <= 1) return@setOnClickListener

            val img = images[imgPos]
            images.removeAt(imgPos)
            images.addFirst(img)

            imgPos = 0
            updateButtons()
        }
        binding.addImgBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }
        binding.cameraBtn.setOnClickListener {
            val tmpFile = File.createTempFile("tmp_image", ".jpg", cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            tempUri = FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
            camera.launch(tempUri)
        }
        binding.delImgBtn.setOnClickListener {
            if (images.isEmpty()) return@setOnClickListener

            images.removeAt(imgPos)
            while (imgPos >= images.size) imgPos--

            if (imgPos < 0) {
                binding.devicePhotoView.setImageDrawable(emptyImgIcon)
                imgPos = 0
            }
            else binding.devicePhotoView.setImageBitmap(images[imgPos].bitmap)
            updateButtons()
        }
        binding.saveButton.setOnClickListener {
            val deviceEntity = DeviceEntity(
                device,
                model ?: device.models[0],
                if (binding.modelNumberEditText.visibility == View.VISIBLE) {
                    binding.modelNumberEditText.text.toString()
                } else {
                    binding.modelNumberSpinner.selectedItem.toString()
                },
                binding.conditionEditText.text.toString(),
                images = images,
                accessories = accessories
            )

            if (index == -1)
                MainActivity.deviceEntities.add(deviceEntity)
            else {
                MainActivity.deviceEntities[index] = deviceEntity
                ListFragment.deviceRecyclerView.adapter?.notifyItemChanged(index)
            }
            finish()
        }

        binding.accessoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.accessoryRecyclerView.adapter = AccessoryEntityAdapter(accessories)
    }
}