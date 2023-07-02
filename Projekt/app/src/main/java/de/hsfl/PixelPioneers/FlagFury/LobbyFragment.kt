package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentLobbyBinding
import org.json.JSONArray
import org.json.JSONObject

class LobbyFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var playerAdapter: PlayerAdapter
    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLobbyBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val joinGameButton: Button = binding.buttonStart
        val cancelButton: Button = binding.buttonCancel
        binding.textViewGameId.text = buildString {
            append("Game ID: ")
            append(mainViewModel.gameId.value)
        }

        playerAdapter = PlayerAdapter(emptyList())

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = playerAdapter

        joinGameButton.setOnClickListener {
            startGame()
        }


        cancelButton.setOnClickListener {
            mainViewModel.setIsHost(false)
            navController.navigate(R.id.action_lobbyFragment_to_homeScreen)
        }

        mainViewModel.isHost.observe(viewLifecycleOwner) { isHost ->
            binding.buttonStart.visibility = if (isHost) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mainViewModel.players.observe(viewLifecycleOwner){ players ->
            Log.d("players",players.toString())
            playerAdapter.updateList(players)
        }

        mainViewModel.state.observe(viewLifecycleOwner){ state ->
            if(state == "1" && navController.currentDestination?.id == R.id.lobbyFragment) {
                (activity as MainActivity).startServerAndDiscovery()
                navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
            }
        }

        startPeriodicUpdate()
    }

    override fun onDestroyView() {
        stopPeriodicUpdate()
        super.onDestroyView()
    }

    private fun startGame() {
        mainViewModel.startGame(
            mainViewModel.gameId.value,
            mainViewModel.name.value,
            mainViewModel.token.value,
            { game,state ->
                Log.d("Lobby","State changed in Game $game to $state")
            },
            { error ->
                showErrorToast("Fehler beim Abrufen der Eroberungspunkte")
            }
        )
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed({
            mainViewModel.getPlayers()
            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun showErrorToast(error : String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }
}
