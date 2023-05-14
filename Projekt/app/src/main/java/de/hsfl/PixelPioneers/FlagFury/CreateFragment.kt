package de.hsfl.PixelPioneers.FlagFury

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding

class CreateFragment : Fragment() {
    private val mainViewModel : MainViewModel by activityViewModels()
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCreateBinding.inflate(inflater,container,false)
        val navController = findNavController();
        val lobbyButton : Button = binding.buttonCreateGame
        val cancelButton : Button = binding.buttonCancel
        val name : EditText = binding.editTextName

            lobbyButton.setOnClickListener{
                mainViewModel.registerGame(name.text.toString()) { gameId, token ->
                    mainViewModel.setGameId(gameId)
                    mainViewModel.setToken(token)
                    mainViewModel.setName(name.text.toString())
                    navController.navigate(R.id.action_createFragment_to_lobbyFragment)
                }
            }

        cancelButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_homeScreen)
        }

        return binding.root
    }
    }


