package pl.kubaf2k.consolist.ui.devices

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pl.kubaf2k.consolist.MainActivity
import pl.kubaf2k.consolist.R
import pl.kubaf2k.consolist.databinding.FragmentDevicesBinding
import pl.kubaf2k.consolist.dataclasses.Device

class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val devicesViewModel =
            ViewModelProvider(this)[DevicesViewModel::class.java]

        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.deviceRecyclerView.adapter = DevicesAdapter(this)

        val searchAndFilterItems = arrayOf(
            getString(R.string.name),
            getString(R.string.manufacturer),
            getString(R.string.release_year),
            getString(R.string.type),
            getString(R.string.generation)
        )

        binding.clearSearchButton.setOnClickListener {
            MainActivity.instance.db.collection("devices")
                .get().addOnSuccessListener {
                    val deviceCount = MainActivity.devices.size
                    MainActivity.devices.clear()
                    binding.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, deviceCount)
                    for (document in it) {
                        MainActivity.devices.add(Device(document))
                    }
                    binding.deviceRecyclerView.adapter?.notifyItemRangeInserted(0, deviceCount)
                }
        }

        binding.searchButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.search_dialog, null)
            val alertDialog = AlertDialog.Builder(context)
                .setTitle(R.string.search)
                .setView(dialogView)
                .create()
            val spinner: Spinner = dialogView.findViewById(R.id.attributeSpinner)
            val search: EditText = dialogView.findViewById(R.id.searchEditText)
            val searchButton: Button = dialogView.findViewById(R.id.searchButton)

            context?.let { ctx ->
                val attributeArrayAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(
                    ctx,
                    android.R.layout.simple_spinner_item,
                    searchAndFilterItems
                )
                spinner.adapter = attributeArrayAdapter

                searchButton.setOnClickListener {
                    val field = when (searchAndFilterItems[spinner.selectedItemPosition]) {
                        getString(R.string.name) -> "name"
                        getString(R.string.manufacturer) -> "manufacturer"
                        getString(R.string.release_year) -> "releaseYear"
                        getString(R.string.type) -> "type"
                        getString(R.string.generation) -> "generation"
                        else -> ""
                    }
                    val collection = MainActivity.instance.db.collection("devices")
                    val query = if (field == "releaseYear" || field == "generation")
                        collection.whereEqualTo(field, search.text.toString().toInt())
                    else collection.whereEqualTo(field, search.text.toString())

                    query.get()
                        .addOnSuccessListener {
                            val devices = MainActivity.devices.size
                            MainActivity.devices.clear()
                            binding.deviceRecyclerView.adapter?.notifyItemRangeRemoved(0, devices)
                            for (document in it) {
                                MainActivity.devices.add(Device(document))
                            }
                            binding.deviceRecyclerView.adapter?.notifyItemRangeInserted(0, MainActivity.devices.size)
                            alertDialog.dismiss()
                        }
                }
                alertDialog.show()
            }
        }

        binding.sortButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(R.string.sorting)
                .setItems(searchAndFilterItems) { _, which ->
                    when (searchAndFilterItems[which]) {
                        getString(R.string.manufacturer) -> MainActivity.devices.sortBy { it.manufacturer }
                        getString(R.string.release_year) -> MainActivity.devices.sortBy { it.releaseYear }
                        getString(R.string.type) -> MainActivity.devices.sortBy { it.type }
                        getString(R.string.generation) -> MainActivity.devices.sortBy { it.generation }
                        else -> MainActivity.devices.sortBy { it.name }
                    }
                    binding.deviceRecyclerView.adapter?.notifyItemRangeChanged(0, MainActivity.devices.size)
                }.show()
        }

        binding.reverseSortButton.setOnClickListener {
            MainActivity.devices.reverse()
            binding.deviceRecyclerView.adapter?.notifyItemRangeChanged(0, MainActivity.devices.size)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}