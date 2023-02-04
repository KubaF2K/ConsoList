package pl.kubaf2k.consolist.ui.editorpanel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.databinding.ActivityEditorPanelBinding
import pl.kubaf2k.consolist.dataclasses.Device

class EditorPanelActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditorPanelBinding
    val devices = mutableListOf<Device>()

    companion object {
        lateinit var instance: EditorPanelActivity
    }

    fun updateList() {
        val length = devices.size
        devices.clear()
        binding.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, length)
        MainActivity.instance.db.collection("devices")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    devices.add(Device(document))
                }
                binding.deviceRecyclerView.adapter?.notifyItemRangeInserted(0, result.size())
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditorPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instance = this

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceRecyclerView.adapter = DatabaseDevicesAdapter(this)
        updateList()

        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, DatabaseEntryActivity::class.java))
        }
    }
}