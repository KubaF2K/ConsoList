package pl.kubaf2k.consolist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.kubaf2k.consolist.databinding.ActivityMainBinding
import pl.kubaf2k.consolist.dataclasses.Device
import pl.kubaf2k.consolist.dataclasses.Model
import pl.kubaf2k.consolist.dataclasses.DeviceEntity
import java.net.HttpURLConnection
import java.net.URL

suspend fun getBitmapFromURL(url: URL): Bitmap? {
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
                    "Slim",
                    URL("https://upload.wikimedia.org/wikipedia/commons/0/02/PS2-Fat-Console-Set.jpg"),
                    listOf("SCPH-70000")
                )
            ),
            listOf()
        ))
        lateinit var deviceEntities: MutableList<DeviceEntity>
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        deviceEntities = ArrayList()
    }
}