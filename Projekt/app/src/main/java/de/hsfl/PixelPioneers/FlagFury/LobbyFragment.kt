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
import org.json.JSONObject

class LobbyFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var playerAdapter: PlayerAdapter
    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLobbyBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val joinGameButton: Button = binding.buttonStart
        val cancelButton: Button = binding.buttonCancel
        binding.textViewGameId.text = buildString {
            append("Game ID: ")
            append(mainViewModel.gameId.value)
        }

        val isHost = mainViewModel.isHost.value ?: false
        playerAdapter = PlayerAdapter(emptyList(), object : OnPlayerClickListener {
            override fun onPlayerClick(player: JSONObject) {
                val name = player.getString("name")
                val gameId = mainViewModel.gameId.value
                val token = player.getString("token")
                mainViewModel.removePlayer(gameId, name, token, { game, name ->
                    Log.d("Lobby", "$name wurde erfolgreich gekickt")
                }, { error ->
                    error?.let { showErrorToast(it) }
                })
            }
        }, isHost, mainViewModel.name.value!!)


        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = playerAdapter

        joinGameButton.setOnClickListener {
            mainViewModel.startGame()
        }


        cancelButton.setOnClickListener {
            mainViewModel.removePlayer(
                mainViewModel.gameId.value,
                mainViewModel.name.value,
                mainViewModel.token.value,
                { game, name ->
                    stopPeriodicUpdate()
                    if (navController.currentDestination?.id == R.id.lobbyFragment) {
                        findNavController().navigate(R.id.action_gameFragment_to_homeScreen)
                    }
                },
                { error ->
                    showErrorToast("Fehler bei der Abmeldung")
                })
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

        mainViewModel.players.observe(viewLifecycleOwner) { players ->
            val sortedPlayers = players.sortedWith(compareBy<JSONObject>
            { it.getString("name") == mainViewModel.name.value }.reversed()
            )
            playerAdapter.submitList(sortedPlayers)
        }

        mainViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == "1" && navController.currentDestination?.id == R.id.lobbyFragment) {
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


    private fun showErrorToast(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }


    private fun startPeriodicUpdate() {
        handler.postDelayed({
            mainViewModel.getPlayers { error ->
                showErrorToast("Du wurdest aus der Lobby gekickt")
                val navController = findNavController()
                stopPeriodicUpdate()
                if (navController.currentDestination?.id == R.id.lobbyFragment)
                    navController.navigate(R.id.action_lobbyFragment_to_homeScreen)
            }
            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }
}
