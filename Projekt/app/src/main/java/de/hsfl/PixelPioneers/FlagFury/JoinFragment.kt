package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentJoinBinding


class JoinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJoinBinding.inflate(inflater,container,false)
        val navController = findNavController()
        val joinGameButton: Button = binding.buttonJoinGame
        val cancelButton: Button = binding.buttonJoinCancel


        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_joinFragment_to_lobbyFragment)
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_joinFragment_to_homeScreen)
        }
        return binding.root;
    }
}
