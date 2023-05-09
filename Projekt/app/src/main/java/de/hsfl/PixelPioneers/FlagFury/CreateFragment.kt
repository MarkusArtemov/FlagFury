package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentHomeScreenBinding

class CreateFragment : Fragment() {
    val mainViewModel : MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCreateBinding.inflate(inflater,container,false)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val navController = findNavController();
        val lobbyButton : Button = binding.buttonCreateGame
        val cancelButton : Button = binding.buttonCancel

        lobbyButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_lobbyFragment)
        }

        cancelButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_homeScreen)
        }

        return binding.root
    }
    }


