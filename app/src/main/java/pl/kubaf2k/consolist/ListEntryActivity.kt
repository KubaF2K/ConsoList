package pl.kubaf2k.consolist

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap46
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.kubaf2k.consolist.databinding.ActivityListEntryBinding
import pl.kubaf2k.consolist.dataclasses.*
import pl.kubaf2k.consolist.ui.AccessoryEntitiesAdapter
import java.io.File
import java.util.*

class ListEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListEntryBinding

    //index of old entity in the deviceEntities list, or in the case of accessories of the old entity in the accessories list of the parent
    //if -1 then add instead of edit
    private var index: Int = -1

    private lateinit var device: Device
    private lateinit var oldEntity: DeviceEntity
    private var model: Model? = null
    private var modelNumber = ""
    private val accessories = ArrayList<AccessoryEntity>()

    //if the "parent" extra is set then this is set to true
    private var isAccessory = false
    private lateinit var accessory: Accessory
    private lateinit var parent: DeviceEntity
    private lateinit var oldAccessoryEntity: AccessoryEntity

    private val images = LinkedList<Bitmap>()
    private var imgPos = 0

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
        images.add(bitmap)
        imgPos = images.size - 1
        binding.devicePhotoView.setImageBitmap(images[imgPos])

        updateButtons()
    }}
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) return@registerForActivityResult

        tempUri?.let { uri ->
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            images.add(bitmap)
            imgPos = images.size - 1
            binding.devicePhotoView.setImageBitmap(images[imgPos])

            updateButtons()
        }
    }

    private val addOrEditAccessoryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val index = result.data?.getIntExtra("index", -1) ?: -1

        if (index != -1) {
            result.data?.getParcelableExtra<AccessoryEntity>("resultDevice")?.let { accessories[index] = it }
            binding.accessoryRecyclerView.adapter?.notifyItemChanged(index)
        }
        else {
            result.data?.getParcelableExtra<AccessoryEntity>("resultDevice")?.let { accessories.add(it) }
            binding.accessoryRecyclerView.adapter?.notifyItemInserted(accessories.size - 1)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.accessoryRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { bundle ->
            //the id of the accessory's parent device in the deviceEntities list, required for accessories
            bundle.get("parent")?.let {
                isAccessory = true
                parent = MainActivity.deviceEntities[it as Int]
            }
            //index of the device in the devices list, or in the case of accessories in the parent's device's accessories list
            bundle.get("device")?.let {
                if (isAccessory) accessory = parent.device.accessories[it as Int]
                else device = MainActivity.devices[it as Int]
            }
            //index of old entity in the deviceEntities list, or in the case of accessories of the old entity in the accessories list of the parent
            bundle.get("index")?.let {
                index = it as Int
                if (isAccessory) {
                    oldAccessoryEntity = parent.accessories[index]
                }
                else {
                    oldEntity = MainActivity.deviceEntities[index]
                    device = oldEntity.device
                    accessories.addAll(oldEntity.accessories)
                }
            }
        }

        lifecycleScope.launch {
            getBitmapFromURL(if (isAccessory) accessory.imgURL else device.imgURL)?.let{
                binding.deviceImageView.setImageBitmap(it)
            }
        }

        if (isAccessory) {
//            TODO visibility.gone the location
            binding.accessoriesTextView.visibility = View.GONE
            binding.addAccessoryBtn.visibility = View.GONE
            binding.accessoryRecyclerView.visibility = View.GONE
        }

        binding.deviceNameTextView.text = if (isAccessory) "${accessory.name} (${accessory.modelNumber})" else "${device.manufacturer} ${device.name}"

        if (!isAccessory && device.models.size > 1) {
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
            if(!isAccessory)
                model = device.models[0]
        }

        if (index != -1) {
            if (isAccessory) {
                binding.conditionEditText.setText(oldAccessoryEntity.condition)

                if (oldAccessoryEntity.images.isNotEmpty()) {
                    images.addAll(oldAccessoryEntity.images)

                    binding.devicePhotoView.setImageBitmap(images[imgPos])
                }
            }
            else {
                binding.conditionEditText.setText(oldEntity.condition)

                if (oldEntity.images.isNotEmpty()) {
                    images.addAll(oldEntity.images)

                    binding.devicePhotoView.setImageBitmap(images[imgPos])
                }
            }
        }

        binding.prevImgBtn.setOnClickListener {
            if (imgPos <= 0) return@setOnClickListener

            binding.devicePhotoView.setImageBitmap(images[--imgPos])
            updateButtons()
        }
        binding.nextImgBtn.setOnClickListener {
            if (imgPos >= images.size-1) return@setOnClickListener

            binding.devicePhotoView.setImageBitmap(images[++imgPos])
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
                binding.devicePhotoView.setImageDrawable(getDrawable(android.R.drawable.ic_menu_gallery))
                imgPos = 0
            }
            else binding.devicePhotoView.setImageBitmap(images[imgPos])
            updateButtons()
        }
        binding.addAccessoryBtn.setOnClickListener {
            if (index == -1)
                AlertDialog.Builder(this)
                    .setTitle("Adding accessories") //TODO string
                    .setMessage("To add an accessory you must save this device first. Do you want to save?")
                    .setPositiveButton(android.R.string.ok) { _, _ -> binding.saveButton.callOnClick() }
                    .setNegativeButton(android.R.string.cancel, null )
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            else {
                val accArray = Array<CharSequence>(device.accessories.size) { index -> device.accessories[index].name }
                AlertDialog.Builder(this)
                    .setTitle("Select an accessory") //TODO string
                    .setItems(accArray) { _, which ->
                        val addIntent = Intent(this, ListEntryActivity::class.java)
                            .putExtra("parent", index)
                            .putExtra("device", which)
                        addOrEditAccessoryContract.launch(addIntent)
                    }.show()
            }
        }
        //TODO not working with images
        binding.saveButton.setOnClickListener {
            val resultDevice: Parcelable = if (isAccessory) AccessoryEntity(
                accessory,
                binding.conditionEditText.text.toString(),
                images
            ) else DeviceEntity(
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



//                if (index == -1)
//                    MainActivity.deviceEntities.add(deviceEntity)
//                else {
//                    MainActivity.deviceEntities[index] = deviceEntity
//                    ListFragment.deviceRecyclerView.adapter?.notifyItemChanged(index)
//                }

            setResult(Activity.RESULT_OK, Intent()
                .putExtra("index", index)
                .putExtra("resultDevice", resultDevice)
            )
            finish()
        }

        if (!isAccessory) {
            binding.accessoryRecyclerView.layoutManager = LinearLayoutManager(this)
            if (index != -1) binding.accessoryRecyclerView.adapter = AccessoryEntitiesAdapter(addOrEditAccessoryContract, index, accessories)
        }
    }
}