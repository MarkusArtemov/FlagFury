package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentJoinBinding


class JoinFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJoinBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val joinGameButton: Button = binding.buttonJoinGame
        val cancelButton: Button = binding.buttonJoinCancel
        val gameId: EditText = binding.editTextGameId
        val nameInput: EditText = binding.editTextJoinName
        joinGameButton.setOnClickListener {
            mainViewModel.joinGame(gameId.text.toString(), nameInput.text.toString(), { team, token ->
                mainViewModel.setTeam(team)
                mainViewModel.setToken(token)
                mainViewModel.setGameId(gameId.text.toString())
                mainViewModel.setName(nameInput.text.toString())
                navController.navigate(R.id.action_joinFragment_to_lobbyFragment)
            }, { error ->
                error?.let { error -> showErrorToast(error) }
            })
        }




        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_joinFragment_to_homeScreen)
        }

        return binding.root
    }


    private fun showErrorToast(error : String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }
}
