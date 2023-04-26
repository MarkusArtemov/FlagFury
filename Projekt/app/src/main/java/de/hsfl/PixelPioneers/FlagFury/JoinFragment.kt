package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController


class JoinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_join, container, false)
        val navController = findNavController()
        val joinGameButton: Button = rootView.findViewById(R.id.buttonJoinGame)
        val cancelButton: Button = rootView.findViewById(R.id.buttonJoinCancel)


        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_joinFragment_to_lobbyFragment)
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_joinFragment_to_homeScreen)
        }
        return rootView;
    }
}
