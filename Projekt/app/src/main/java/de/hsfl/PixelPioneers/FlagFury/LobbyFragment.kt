package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        playerAdapter = PlayerAdapter(emptyList())

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = playerAdapter
        binding.textViewGameId.text = "Game ID: "+ mainViewModel.getGameId()

        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_lobbyFragment_to_homeScreen)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Lobbyfragment","update")
        startPeriodicUpdate()
    }

    override fun onDestroyView() {
        stopPeriodicUpdate()
        super.onDestroyView()
    }

    private fun startPeriodicUpdate() {
        Log.d("Läuft", "Läuft eigentlich")
        handler.postDelayed({
            mainViewModel.getPlayers(mainViewModel.getGameId(), mainViewModel.getName(), mainViewModel.getToken()) { players ->
                players?.let {
                    val playerList = it.getJSONArray("players")
                    val convertedList = jsonArrayToList(playerList)
                    playerAdapter.updateList(convertedList)
                }
            }
            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(jsonObject)
        }
        return list
    }
}
