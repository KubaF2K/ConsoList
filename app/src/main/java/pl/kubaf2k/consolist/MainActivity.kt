package pl.kubaf2k.consolist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.kubaf2k.consolist.MainActivity.Companion.cachedImages
import pl.kubaf2k.consolist.databinding.ActivityMainBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.Model
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import pl.kubaf2k.consolist.dataclasses.SerializableBitmap
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

suspend fun getBitmapFromURL(url: URL): Bitmap? {
    if (cachedImages.containsKey(url))
        return cachedImages[url]

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
    bitmap?.let { cachedImages[url] = it }
    return bitmap
}

class MainActivity : AppCompatActivity() {

    companion object {
        val devices = listOf(Device(
            "Playstation 2",
            "Sony Ps2 jest to kozacka konsola lorem ipsum dolor sit ametSony Ps2 jest to kozacka konsola lorem ipsum dolor sit ametSony Ps2 jest to kozacka konsola lorem ipsum dolor sit ametSony Ps2 jest to kozacka konsola lorem ipsum dolor sit amet",
            URL("https://upload.wikimedia.org/wikipedia/commons/0/02/PS2-Fat-Console-Set.jpg"),
            "Sony",
            2000,
            listOf(
                Model(
                    "Fat",
                    URL("https://upload.wikimedia.org/wikipedia/commons/0/02/PS2-Fat-Console-Set.jpg"),
                    listOf("SCPH-30000")
                ),
                Model(
                    "Slim (70k)",
                    URL("https://lowendmac.com/wp-content/uploads/ps2-slim.jpg"),
                    listOf("SCPH-70000")
                ),
                Model(
                    "Slim (90k)",
                    URL("https://www.justpushstart.com/wp-content/uploads/2013/01/ps2-console.jpg"),
                    listOf("SCPH-90000")
                )
            ),
            listOf()
        ))
        val deviceEntities = mutableListOf(DeviceEntity(
            devices[0],
            0,
            0,
            "Good",
            null,
            emptyList(),
            emptyList()
        ))
        val cachedImages: MutableMap<URL, Bitmap> = HashMap()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val images = LinkedList<SerializableBitmap>()

        lifecycleScope.launch {
            var image = getBitmapFromURL(URL("https://www.justpushstart.com/wp-content/uploads/2013/01/ps2-console.jpg"))
            image?.let { images.add(SerializableBitmap(it)) }
            image = getBitmapFromURL(URL("https://lowendmac.com/wp-content/uploads/ps2-slim.jpg"))
            image?.let { images.add(SerializableBitmap(it)) }
            deviceEntities.add(DeviceEntity(
                devices[0],
                0,
                0,
                "Good",
                null,
                images,
                emptyList()
            ))
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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