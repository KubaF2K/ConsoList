package pl.kubaf2k.consolist.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import pl.kubaf2k.consolist.databinding.FragmentListBinding

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listViewModel =
            ViewModelProvider(this)[ListViewModel::class.java]

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.deviceRecyclerView.adapter = DeviceEntitiesAdapter()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}