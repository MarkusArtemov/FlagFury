package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController


class LobbyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_lobby, container, false)
        val navController = findNavController()
        val joinGameButton: Button = rootView.findViewById(R.id.buttonStart)
        val cancelButton: Button = rootView.findViewById(R.id.buttonCancel)


        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_homeScreen)
        }
        return rootView;
    }


}
