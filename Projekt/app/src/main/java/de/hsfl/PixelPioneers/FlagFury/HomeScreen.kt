package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class HomeScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =  inflater.inflate(R.layout.fragment_home_screen, container, false)
        val navController = findNavController();
        val hostButton : Button = rootView.findViewById(R.id.hostGame)
        val joinGameButton : Button = rootView.findViewById(R.id.joinGame)
        hostButton.setOnClickListener{
            navController.navigate(R.id.action_homeScreen_to_createFragment)
        }

        joinGameButton.setOnClickListener{
            navController.navigate(R.id.action_homeScreen_to_joinFragment)
        }

        return rootView
    }




}
