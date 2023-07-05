package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentWinRedBinding

class WinRedFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWinRedBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val closeButton = binding.closeButton

        closeButton.setOnClickListener {
            mainViewModel.setIsHost(false)
            navController.navigate(R.id.action_winRedFragment_to_homeScreen)
        }
        return binding.root
    }


}
