package de.hsfl.PixelPioneers.FlagFury

import android.app.AlertDialog
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
            val teams = arrayOf("Egal", "Rot", "Blau")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Team auswählen")
            builder.setItems(teams) { dialog, which ->
                val team = when (which) {
                    0 -> 0 // "Egal"
                    1 -> 1 // "Rot"
                    2 -> 2 // "Blau"
                    else -> 0
                }
                mainViewModel.joinGame(
                    gameId.text.toString(),
                    nameInput.text.toString(),
                    { team1, token ->
                        mainViewModel.setTeam(team1)
                        mainViewModel.setToken(token)
                        mainViewModel.setGameId(gameId.text.toString())
                        mainViewModel.setName(nameInput.text.toString())
                        navController.navigate(R.id.action_joinFragment_to_lobbyFragment)
                    },
                    { error ->
                        error?.let { error -> showErrorToast(error) }
                    })
            }
            builder.show()
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
