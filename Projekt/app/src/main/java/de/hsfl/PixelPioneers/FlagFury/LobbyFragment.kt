package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentLobbyBinding


class LobbyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLobbyBinding.inflate(inflater,container,false)
        val navController = findNavController()
        val joinGameButton: Button = binding.buttonStart
        val cancelButton: Button = binding.buttonCancel


        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_homeScreen)
        }
        return binding.root;
    }


}
