package de.hsfl.PixelPioneers.FlagFury

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lokibt.bluetooth.BluetoothDevice
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentGameBinding
import org.json.JSONArray
import org.json.JSONObject

class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private var playerList = emptyList<JSONObject>()

    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())
    private var conquerPoints: List<Point> = emptyList()
    private val conquerCountdownDuration: Long = 10000
    private var isConquerCountdownRunning: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val leaveButton: Button = binding.button

        leaveButton.setOnClickListener {
            val snackBar = Snackbar.make(
                requireView(),
                "Möchtest du das Spiel wirklich verlassen?",
                Snackbar.LENGTH_LONG
            )
            snackBar.setAction("Ja") {
                mainViewModel.removePlayer(
                    mainViewModel.gameId.value,
                    mainViewModel.name.value,
                    mainViewModel.token.value,
                    { game, name ->
                        mainViewModel.stopDiscoverDevices()
                        mainViewModel.stopServer()
                        navController.navigate(R.id.action_gameFragment_to_homeScreen)
                    },
                    { error ->
                        showErrorToast("Fehler bei der Abmeldung")
                    }
                )
            }
            snackBar.show()
        }

        mainViewModel.isDefended.observe(viewLifecycleOwner) { isDefended ->
            if (isDefended) {
                mainViewModel.currentPosition.value?.let { currentPosition ->
                    conquerPoints.forEach { conquerPoint ->
                        if (checkConquerPoint(conquerPoint, currentPosition)) {
                            mainViewModel.oldConquerPointTeam.value = conquerPoint.team
                            conquerPoint.team = -1
                            startConquerCountdown(conquerPoint)
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun getPlayers() {
        mainViewModel.getPlayers(
            mainViewModel.gameId.value,
            mainViewModel.name.value,
            mainViewModel.token.value,
            { players ->
                players?.let {
                    val playerJSONArray = it.getJSONArray("players")
                    this.playerList = jsonArrayToList(playerJSONArray)
                    Log.d("GameFragment", "Spielerliste: $playerList")
                }
            },
            { error ->
                error?.let { showErrorToast(it) }
            }
        )
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(jsonObject)
        }
        return list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askForConquestPoints { points ->
            conquerPoints = points
            createAllFlags(points)
        }
        startPeriodicUpdate()

        mainViewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            it?.let { updateMarkerPosition(it) }
            binding.target.visibility = View.VISIBLE
        })
    }

    override fun onPause() {
        super.onPause()
        stopPeriodicUpdate()
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    private fun askForConquestPoints(callback: (points: List<Point>) -> Unit) {
        mainViewModel.getPoints(
            mainViewModel.gameId.value,
            mainViewModel.name.value,
            mainViewModel.token.value,
            { points, state, game ->
                Log.d("GameFragment", "$points $state $game")
                points?.let {
                    conquerPoints = it
                    updateFlagStatus(it)
                    callback(it)
                }
            },
            { error ->
                showErrorToast("Fehler beim Abrufen der Eroberungspunkte")
            }
        )
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed({
            connectToOpponents()
            getPlayers()
            Log.d("GameFragment", "Discovered Devices: ${mainViewModel.discoveredDevices.value}")
            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun updateFlagStatus(points: List<Point>) {
        for (point in points) {
            val flagMarker = getFlagMarkerByPointId(point.id)
            flagMarker?.let {
                val imageResource = getColorFromTeamNumber(point.team)
                it.setImageResource(imageResource)
            }
        }
    }

    private fun getFlagMarkerByPointId(pointId: String): ImageView? {
        val flagMarkers = binding.constraintLayout.children.filterIsInstance<ImageView>()
        return flagMarkers.firstOrNull { it.id == pointId.toInt() }
    }

    private fun getColorFromTeamNumber(team: Int): Int {
        return when (team) {
            -1 -> R.drawable.circle_yellow
            0 -> R.drawable.circle_grey
            1 -> R.drawable.circle_red
            2 -> R.drawable.circle_blue
            else -> R.drawable.circle_grey
        }
    }

    private fun connectToOpponents() {
        val devices = mainViewModel.discoveredDevices.value
        val team = if (mainViewModel.team.value == 1) "blau" else "rot"
        if (devices != null) {
            devices.forEach { device ->
                mainViewModel.connectToServer(device, team)
            }
        } else {
            mainViewModel.setIsDefended(false)
        }
    }

    private fun checkConquerPoint(conquerPoint: Point, currentPosition: Pair<Double, Double>): Boolean {
        val playerLocation = Location("Player")
        playerLocation.latitude = currentPosition.second
        playerLocation.longitude = currentPosition.first

        val conquerLocation = Location("ConquerPoint")
        conquerLocation.latitude = conquerPoint.latitude
        conquerLocation.longitude = conquerPoint.longitude

        val distance = playerLocation.distanceTo(conquerLocation)
        return distance < 5 && conquerPoint.team != mainViewModel.team.value
    }

    private fun startConquerCountdown(conquerPoint: Point) {
        val oldTeam = mainViewModel.team.value
        conquerPoint.team = -1

        if (!isConquerCountdownRunning) {
            isConquerCountdownRunning = true
            connectToOpponents()

            handler.postDelayed({
                mainViewModel.oldConquerPointTeam.value = oldTeam
                isConquerCountdownRunning = false

                mainViewModel.team.value?.let { team ->
                    val imageResource = getColorFromTeamNumber(team)
                    val flagMarker = getFlagMarkerByPointId(conquerPoint.id)
                    flagMarker?.setImageResource(imageResource)
                }
            }, conquerCountdownDuration)
        }
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateMarkerPosition(position: Pair<Double, Double>) {
        val markerPosition = generatePosition(position)
        val mapImageWidth = binding.campusCard.width
        val mapImageHeight = binding.campusCard.height
        updateMarkerViewPosition(markerPosition, mapImageWidth, mapImageHeight)
    }

    private fun generatePosition(position: Pair<Double, Double>): Pair<Double, Double> {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val posX = (position.first - tlLongitude) / (brLongitude - tlLongitude)
        val posY = (position.second - tlLatitude) / (brLatitude - tlLatitude)

        return Pair(posX, posY)
    }

    private fun updateMarkerViewPosition(
        markerPosition: Pair<Double, Double>,
        mapImageWidth: Int,
        mapImageHeight: Int
    ) {
        val markerViewWidth = binding.target.width
        val markerViewHeight = binding.target.height

        val markerPosX = markerPosition.first * mapImageWidth - markerViewWidth / 2
        val markerPosY = markerPosition.second * mapImageHeight - markerViewHeight / 2

        binding.target.x = markerPosX.toFloat()
        binding.target.y = markerPosY.toFloat()
    }

    private fun createAllFlags(points: List<Point>) {
        for (point in points) {
            addFlagMarker(point)
        }
    }

    private fun addFlagMarker(point: Point) {
        val flagPosition = generatePosition(Pair(point.longitude, point.latitude))

        flagPosition.let { flagPosition ->
            val markerSize = 20
            val imageResource = getColorFromTeamNumber(point.team)
            val flagMarker = createFlagMarker(markerSize, imageResource)
            flagMarker.id = point.id.toInt()

            binding.constraintLayout.addView(flagMarker)

            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height

            val markerPosX = flagPosition.first * mapImageWidth - markerSize / 2
            val markerPosY = flagPosition.second * mapImageHeight - markerSize / 2

            setViewConstraints(flagMarker, markerPosX, markerPosY)
        }
    }

    private fun createFlagMarker(markerSize: Int, imageResource: Int): ImageView {
        val flagMarker = ImageView(requireContext())
        flagMarker.setImageResource(imageResource)
        flagMarker.layoutParams = ViewGroup.LayoutParams(markerSize, markerSize)
        return flagMarker
    }

    private fun setViewConstraints(
        flagMarker: ImageView,
        markerPosX: Double,
        markerPosY: Double
    ) {
        ConstraintSet().apply {
            clone(binding.constraintLayout)
            connect(
                flagMarker.id,
                ConstraintSet.START,
                binding.campusCard.id,
                ConstraintSet.START,
                markerPosX.toInt()
            )
            connect(
                flagMarker.id,
                ConstraintSet.TOP,
                binding.campusCard.id,
                ConstraintSet.TOP,
                markerPosY.toInt()
            )
            applyTo(binding.constraintLayout)
        }
    }
}
