package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentHomeScreenBinding
import kotlin.properties.Delegates

class HomeScreen : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        val navController = findNavController();
        val hostButton: Button = binding.hostGame
        val joinGameButton: Button = binding.joinGame

        hostButton.setOnClickListener {
            navController.navigate(R.id.action_homeScreen_to_createFragment)
        }

        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_homeScreen_to_joinFragment)
        }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        if (!mainViewModel.permissionChecked) {
            (activity as MainActivity).startLocation()
            mainViewModel.permissionChecked = true;

        }
    }
}
