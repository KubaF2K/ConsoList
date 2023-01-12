package pl.kubaf2k.consolist

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.core.Persister
import pl.kubaf2k.consolist.MainActivity.Companion.cachedWebImages
import pl.kubaf2k.consolist.databinding.ActivityMainBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import pl.kubaf2k.consolist.dataclasses.WrapperList
import pl.kubaf2k.consolist.ui.list.ListFragment
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
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

//TODO autosave list on close
//TODO local copy of firestore
class MainActivity : AppCompatActivity() {

    companion object {
        val devices: MutableList<Device> = ArrayList()
        var deviceEntities: MutableList<DeviceEntity> = ArrayList()
        val cachedWebImages: MutableMap<URL, Bitmap> = HashMap()
        val cachedLocalImages: HashMap<Int, Bitmap> = HashMap()
    }

    private lateinit var binding: ActivityMainBinding

    //TODO launch this async because jeez images are big
    private val saveRequest = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
        it?.let { uri ->
            contentResolver.openAssetFileDescriptor(uri, "w")?.use { file ->
                val stream = ZipOutputStream(FileOutputStream(file.fileDescriptor))

                val serializer = Persister()

                stream.putNextEntry(ZipEntry("list.xml"))
                serializer.write(WrapperList(deviceEntities), stream)

                @Suppress("DEPRECATION")
                for (device in deviceEntities) {
                    for (accessory in device.accessories)
                        for (hash in accessory.imageHashes) {
                            cachedLocalImages[hash]?.let { bmp ->
                                stream.putNextEntry(ZipEntry("$hash.webp"))
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                                    bmp.compress(Bitmap.CompressFormat.WEBP, 99, stream)
                                else
                                    bmp.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, stream)

                            }
                        }
                    for (hash in device.imageHashes) {
                        cachedLocalImages[hash]?.let { bmp ->
                            stream.putNextEntry(ZipEntry("$hash.webp"))
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                                bmp.compress(Bitmap.CompressFormat.WEBP, 99, stream)
                            else
                                bmp.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, stream)
                        }
                    }
                }

                stream.close()
            }
//            contentResolver.openAssetFileDescriptor(uri, "w")?.use { file ->
//                val stream = FileOutputStream(file.fileDescriptor)
//                val serializer = Persister()
//
//                serializer.write(WrapperList(deviceEntities), stream)
//            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val loadRequest = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            contentResolver.openFileDescriptor(uri, "r")?.use { file ->
                val stream = ZipInputStream(FileInputStream(file.fileDescriptor))
                val serializer = Persister()

                var ze = stream.nextEntry
                while (ze != null) {
                    if (ze.name == "list.xml") {
                        deviceEntities = serializer.read(
                            WrapperList::class.java,
                            ByteArrayInputStream(stream.readBytes())
                        ).list
                        ListFragment.deviceRecyclerView.adapter?.notifyDataSetChanged()
                        stream.closeEntry()
                    }
                    else {
                        val bmp = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                ByteBuffer.wrap(stream.readBytes())
                            )
                        )
                        cachedLocalImages[ze.name.substringBefore('.').toInt()] = bmp
                        stream.closeEntry()
                    }
                    ze = stream.nextEntry
                }
                stream.close()
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

//        database = Room.databaseBuilder(this, DeviceDatabase::class.java, "device_db").build()
//        dbDao = database.deviceDao()
//        dbDao.insertDevicesWithChildren(*devices.toTypedArray())
        val db = Firebase.firestore

        db.collection("devices")
            .get()
            .addOnSuccessListener {result ->
                for (document in result) {
                    devices.add(Device(document))
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