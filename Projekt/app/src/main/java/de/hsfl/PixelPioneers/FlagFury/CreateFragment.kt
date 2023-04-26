package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController


class CreateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =  inflater.inflate(R.layout.fragment_create, container, false)
        val navController = findNavController();
        val lobbyButton : Button = rootView.findViewById(R.id.buttonCreateGame)
        val cancelButton : Button = rootView.findViewById(R.id.buttonCancel)

        lobbyButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_lobbyFragment)
        }

        cancelButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_homeScreen)
        }

        return rootView
    }
    }


