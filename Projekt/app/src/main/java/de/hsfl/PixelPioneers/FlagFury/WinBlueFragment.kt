package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentGameBinding
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentWinBlueBinding

class WinBlueFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWinBlueBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val closeButton =  binding.closeButton

        closeButton.setOnClickListener {
            mainViewModel.setIsHost(false)
            mainViewModel.stopServer()
            mainViewModel.stopDiscoverDevices()
            navController.navigate(R.id.action_winBlueFragment_to_homeScreen)
        }
        return binding.root
    }
}

