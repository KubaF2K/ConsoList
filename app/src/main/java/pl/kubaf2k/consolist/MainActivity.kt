package pl.kubaf2k.consolist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import pl.kubaf2k.consolist.dataclasses.*
import pl.kubaf2k.consolist.ui.list.ListFragment
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

class MainActivity : AppCompatActivity() {

    companion object {
        val devices: MutableList<Device> = ArrayList()
        var deviceEntities: MutableList<DeviceEntity> = ArrayList()
        val cachedWebImages: MutableMap<URL, Bitmap> = HashMap()
        val cachedLocalImages: HashMap<Int, Bitmap> = HashMap()
//        lateinit var database: DeviceDatabase
//        lateinit var dbDao: DeviceDao
    }

    private lateinit var binding: ActivityMainBinding

    private val saveRequest = registerForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) {
        it?.let { uri ->
            contentResolver.openFileDescriptor(uri, "w")?.use { file ->
                val stream = FileOutputStream(file.fileDescriptor)
                val serializer = Persister()

                serializer.write(WrapperList(deviceEntities), stream)
            }
        }
    }

    private val loadRequest = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            contentResolver.openFileDescriptor(uri, "r")?.use { file ->
                val stream = FileInputStream(file.fileDescriptor)
                val serializer = Persister()

                deviceEntities = serializer.read(WrapperList::class.java, stream).list
                ListFragment.deviceRecyclerView.adapter?.notifyDataSetChanged()
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
                saveRequest.launch("list.xml")
                true
            }
            R.id.load_list -> {
                loadRequest.launch(arrayOf("text/xml"))
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
                    val accessories = ArrayList<Accessory>()
                    for (accessory in document.data["accessories"] as List<Map<String, Any>>) {
                        accessories.add(Accessory(
                            accessory["name"] as String,
                            URL(accessory["imgURL"] as String),
                            accessory["modelNumber"] as String,
                            Accessory.AccessoryType.valueOf(accessory["type"] as String)
                        ))
                    }
                    val models = ArrayList<Model>()
                    for (model in document.data["models"] as List<Map<String, Any>>) {
                        models.add(Model(
                            model["name"] as String,
                            URL(model["imgURL"] as String),
                            (model["modelNumbers"] as List<String>).toMutableList()
                        ))
                    }
                    devices.add(Device(
                        document.data["name"] as String,
                        document.data["description"] as String,
                        URL(document.data["imgURL"] as String),
                        document.data["manufacturer"] as String,
                        (document.data["releaseYear"] as Long).toInt(),
                        models,
                        accessories
                    ))
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