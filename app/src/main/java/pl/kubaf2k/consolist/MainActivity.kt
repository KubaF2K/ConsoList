package pl.kubaf2k.consolist

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.simpleframework.xml.core.Persister
import pl.kubaf2k.consolist.MainActivity.Companion.cachedLocalImages
import pl.kubaf2k.consolist.MainActivity.Companion.cachedWebImages
import pl.kubaf2k.consolist.MainActivity.Companion.listCache
import pl.kubaf2k.consolist.databinding.ActivityMainBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import pl.kubaf2k.consolist.dataclasses.WrapperList
import pl.kubaf2k.consolist.ui.list.ListFragment
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

suspend fun getBitmapFromURL(url: URL): Bitmap? {
    if (cachedWebImages.containsKey(url))
        return cachedWebImages[url]

    var bitmap: Bitmap? = null
    val bmOptions = BitmapFactory.Options().apply {
        inSampleSize = 1
    }
    withContext(Dispatchers.IO) {
        try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connect()
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions)
                stream.close()
            }
        } catch (e: Exception) {
            println(e.message)
            bitmap = null
        }
    }
    bitmap?.let { cachedWebImages[url] = it }
    return bitmap
}

@Suppress("DEPRECATION")
suspend fun compressBitmapToStream(image: Bitmap, stream: OutputStream) {
    withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            image.compress(Bitmap.CompressFormat.WEBP, 99, stream)
        else
            image.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, stream)
    }
}

fun saveList(list: List<DeviceEntity>) {
    val deviceEntitiesCopy = list.toMutableList()
    val serializer = Persister()

    val listFile = File(listCache, "list.xml")
    if (listFile.exists()) listFile.delete()
    listFile.createNewFile()

    serializer.write(WrapperList(deviceEntitiesCopy), listFile)
}

suspend fun saveImage(image: Bitmap) {
    if (listCache.list()?.contains("${image.hashCode()}.webp") == true) return

    withContext(Dispatchers.IO) {
        val file = File(listCache, "${image.hashCode()}.webp")
        file.createNewFile()
        compressBitmapToStream(image, FileOutputStream(file))
    }
}
suspend fun saveImages(hashes: Collection<Int>) {
    for (hash in hashes) cachedLocalImages[hash]?.let { saveImage(it) }
}

suspend fun saveDevicesToFolder(folder: File, deviceEntities: List<DeviceEntity>) {
    val deviceEntitiesCopy = deviceEntities.toMutableList()
    val serializer = Persister()

    if (folder.exists()) folder.deleteRecursively()
    folder.mkdir()

    withContext(Dispatchers.IO) {
        val listFile = File(folder, "list.xml")
        listFile.createNewFile()
        serializer.write(WrapperList(deviceEntitiesCopy), listFile)

        @Suppress("DEPRECATION")
        for (device in deviceEntitiesCopy) {
            for (accessory in device.accessories) {
                for (hash in accessory.imageHashes) {
                    cachedLocalImages[hash]?.let { bmp ->
                        val file = File(folder, "$hash.webp")
                        file.createNewFile()
                        compressBitmapToStream(bmp, FileOutputStream(file))
                    }
                }
            }
            for (hash in device.imageHashes) {
                cachedLocalImages[hash]?.let { bmp ->
                    val file = File(folder, "$hash.webp")
                    file.createNewFile()
                    compressBitmapToStream(bmp, FileOutputStream(file))
                }
            }
        }
    }
}

suspend fun saveDevicesToFile(
    contentResolver: ContentResolver,
    uri: Uri,
    deviceEntities: List<DeviceEntity>
) {
    contentResolver.openAssetFileDescriptor(uri, "w")?.use { file ->
        val stream = ZipOutputStream(FileOutputStream(file.fileDescriptor))
        val deviceEntitiesCopy = deviceEntities.toMutableList()
        val serializer = Persister()

        withContext(Dispatchers.IO) {
            stream.putNextEntry(ZipEntry("list.xml"))
            serializer.write(WrapperList(deviceEntitiesCopy), stream)


            @Suppress("DEPRECATION")
            for (device in deviceEntitiesCopy) {
                for (accessory in device.accessories)
                    for (hash in accessory.imageHashes) {
                        cachedLocalImages[hash]?.let { bmp ->
                            stream.putNextEntry(ZipEntry("$hash.webp"))
                            compressBitmapToStream(bmp, stream)
                        }
                    }
                for (hash in device.imageHashes) {
                    cachedLocalImages[hash]?.let { bmp ->
                        stream.putNextEntry(ZipEntry("$hash.webp"))
                        compressBitmapToStream(bmp, stream)
                    }
                }
            }

            stream.close()
        }
    }
}
suspend fun loadDevicesFromFolder(folder: File, append: Boolean = false) {
    if (!folder.isDirectory) throw IllegalArgumentException("Provided file is not a valid list folder.")

    var tempDeviceEntities = mutableListOf<DeviceEntity>()
    val tempCachedImages = mutableMapOf<Int, Bitmap>()

    val loadedImages = mutableSetOf<File>()
    val devicesWithMissingImages = mutableSetOf<DeviceEntity>()

    val serializer = Persister()

    withContext(Dispatchers.IO) {
        folder.listFiles()?.let { files ->
            val listFile = files.find { it.name == "list.xml" } ?: throw IllegalArgumentException("Provided list folder doesn't contain a valid list file")
            tempDeviceEntities = serializer.read(
                WrapperList::class.java,
                FileInputStream(listFile)
            ).list

            for (device in tempDeviceEntities) {
                for (accessory in device.accessories) {
                    for (hash in accessory.imageHashes) {
                        if (tempCachedImages.containsKey(hash)) continue

                        val imageFile = files.find { it.name == "$hash.webp" }

                        if (imageFile == null) devicesWithMissingImages.add(device)
                        else {
                            tempCachedImages[hash] = ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(imageFile)
                            )
                            loadedImages.add(imageFile)
                        }
                    }
                }
                for (hash in device.imageHashes) {
                    if (tempCachedImages.containsKey(hash)) continue

                    val imageFile = files.find { it.name == "$hash.webp" }

                    if (imageFile == null) devicesWithMissingImages.add(device)
                    else {
                        tempCachedImages[hash] = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(imageFile)
                        )
                        loadedImages.add(imageFile)
                    }
                }
            }
            for (file in files.filter { !loadedImages.contains(it) && it.name != "list.xml" }) file.delete()
        }
    }
    if (!append) {
        val size = MainActivity.deviceEntities.size
        MainActivity.deviceEntities.clear()
        ListFragment.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, size)
    }
    val startIndex = MainActivity.deviceEntities.size
    MainActivity.deviceEntities.addAll(tempDeviceEntities)
    cachedLocalImages.putAll(tempCachedImages)
    ListFragment.deviceRecyclerView.adapter?.notifyItemRangeInserted(
        startIndex,
        tempDeviceEntities.size
    )
}
suspend fun loadDevicesFromFile(contentResolver: ContentResolver, uri: Uri, append: Boolean = false) {
    contentResolver.openAssetFileDescriptor(uri, "r")?.use { file ->
        val stream = ZipInputStream(FileInputStream(file.fileDescriptor))
        var tempDeviceEntities: MutableList<DeviceEntity> = mutableListOf()
        val tempCachedImages = HashMap<Int, Bitmap>()

        val serializer = Persister()

        withContext(Dispatchers.IO) {
            var ze = stream.nextEntry
            while (ze != null) {
                if (ze.name == "list.xml") {
                    tempDeviceEntities = serializer.read(
                        WrapperList::class.java,
                        ByteArrayInputStream(stream.readBytes())
                    ).list
                    stream.closeEntry()
                } else {
                    val bmp = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            ByteBuffer.wrap(stream.readBytes())
                        )
                    )
                    tempCachedImages[ze.name.substringBefore('.').toInt()] = bmp
                    stream.closeEntry()
                }
                ze = stream.nextEntry
            }
            stream.close()
        }

        if (!append) {
            val size = MainActivity.deviceEntities.size
            MainActivity.deviceEntities.clear()
            ListFragment.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, size)
        }
        val startIndex = MainActivity.deviceEntities.size
        MainActivity.deviceEntities.addAll(tempDeviceEntities)
        cachedLocalImages.putAll(tempCachedImages)
        ListFragment.deviceRecyclerView.adapter?.notifyItemRangeInserted(
            startIndex,
            tempDeviceEntities.size
        )
    }
}

//TODO local copy of firestore
class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mainActivity: MainActivity
        val devices: MutableList<Device> = ArrayList()
        var deviceEntities: MutableList<DeviceEntity> = ArrayList()
        val cachedWebImages: MutableMap<URL, Bitmap> = HashMap()
        val cachedLocalImages: HashMap<Int, Bitmap> = HashMap()
        lateinit var listCache: File
    }

    lateinit var binding: ActivityMainBinding

    private val saveRequest = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
        it?.let { uri ->
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.isIndeterminate = true
            lifecycleScope.launch {
                saveDevicesToFile(contentResolver, uri, deviceEntities)
                binding.progressBar.isIndeterminate = false
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, R.string.saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val loadRequest = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.isIndeterminate = true
            lifecycleScope.launch {
                loadDevicesFromFile(contentResolver, uri, true)
                saveDevicesToFolder(listCache, deviceEntities)
                binding.progressBar.isIndeterminate = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_list -> {
                saveRequest.launch("list.clst")
                true
            }
            R.id.load_list -> {
                loadRequest.launch(arrayOf("application/octet-stream"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainActivity = this
        val db = Firebase.firestore

        db.collection("devices")
            .get()
            .addOnSuccessListener {result ->
                for (document in result) {
                    devices.add(Device(document))
                }
            }

        listCache = File(filesDir, "listCache")
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true
        lifecycleScope.launch {
            try {
                loadDevicesFromFolder(listCache)
            } catch (e: IllegalArgumentException) {
                println(e.message)
            } finally {
                binding.progressBar.isIndeterminate = false
                binding.progressBar.visibility = View.GONE
            }
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list, R.id.navigation_devices
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}