package de.hsfl.PixelPioneers.FlagFury

import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
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
    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())
    private var currentPoints: List<Point> = emptyList()
    private var timerIsRunning = false
    private var currentPoint: Point? = null
    private var _isDefended = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val leaveButton: Button = binding.button
        binding.target.visibility = View.VISIBLE

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
                        mainViewModel.stopServer()
                        mainViewModel.stopDiscoverDevices()
                        navController.navigate(R.id.action_gameFragment_to_homeScreen)
                    },
                    { error ->
                        showErrorToast("Fehler bei der Abmeldung")
                    }
                )
            }
            snackBar.show()
        }

        return binding.root
    }


    val timer = object: CountDownTimer(10_000, 1_000) {
        override fun onTick(millisUntilFinished: Long) {
            if(mainViewModel.discoveryEnabled.value == true && checkConquerPoint(currentPoint!!,mainViewModel.currentPosition.value!!)){
                mainViewModel.startDiscoverDevices()
                connectToOpponents()
            }
               else if (_isDefended ||
                    !checkConquerPoint(currentPoint!!, mainViewModel.currentPosition.value!!)) {
                    timerIsRunning = false
                    mainViewModel.stopDiscoverDevices()
                    Log.d("Game","Das ist alte Punkt ${mainViewModel.oldConquerPointTeam.value}")
                    conquerPoint(currentPoint?.id, "${mainViewModel.oldConquerPointTeam.value}")
                    cancel()
                }

        }


        override fun onFinish() {
            mainViewModel.stopDiscoverDevices()
            timerIsRunning = false
            currentPoint?.let {
                if(!_isDefended){
                    conquerPoint(it.id, "${mainViewModel.team.value}")
                }
                currentPoint = null
            }
        }
    }


        private fun getPlayers() {
        mainViewModel.getPlayers(
            mainViewModel.gameId.value,
            mainViewModel.name.value,
            mainViewModel.token.value,
            { players ->
                players?.let {
                        val playerJSONArray = it.getJSONArray("players")
                        val playerName = mainViewModel.name.value
                        var playerTeam: Int? = null

                        for (i in 0 until playerJSONArray.length()) {
                            val player = playerJSONArray.getJSONObject(i)
                            val currentPlayerName = player.getString("name")
                            val currentPlayerTeam = player.getInt("team")

                            if (currentPlayerName == playerName) {
                                playerTeam = currentPlayerTeam
                                mainViewModel.setTeam(playerTeam)
                                break
                            }
                        }
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
        mainViewModel.isDefended.observe(viewLifecycleOwner, Observer { isDefended ->
            _isDefended = isDefended
        })
        askForConquestPoints { points ->
            createAllFlags(points)
        }
        mainViewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            it?.let { updateMarkerPosition(it)
                for (point in currentPoints){
                    if(checkConquerPoint(point,it) && !timerIsRunning && _isDefended){
                        timer.start()
                        timerIsRunning = true
                        mainViewModel.setOldConquerPointTeamValue("${point.team}")
                        conquerPoint(point.id, "-1")
                        currentPoint = point
                    }
                }
            }
        })
        startPeriodicUpdate()
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
               if(state == "2"){
                   points?.let {
                       if(checkAllPointsSameTeam(points,1)){
                           findNavController().navigate(R.id.action_gameFragment_to_winRedFragment)

                       } else if(checkAllPointsSameTeam(points,2)){
                           findNavController().navigate(R.id.action_gameFragment_to_winBlueFragment)
                       }
                   }
               }
                points?.let {
                    if(checkAllPointsSameTeam(points,1)){
                        Log.d("GameFragment","team rot hat gewonnen!")
                        endGame()
                    } else if(checkAllPointsSameTeam(points,2)){
                        Log.d("GameFragment","team blau hat gewonnen!")
                        endGame()
                    }
                    callback(it)
                }
            },
            { error ->
                showErrorToast("Fehler beim Abrufen der Eroberungspunkte")
            }
        )
    }

    private fun checkAllPointsSameTeam(points: List<Point>, team: Int): Boolean {
        val filteredPoints = points.filter { it.team == team }
        return filteredPoints.size == points.size
    }



    private fun endGame() {
        mainViewModel.endGame(
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
            getPlayers()
            Log.d("GameFragment"," is defended : ${_isDefended}")
            askForConquestPoints { points ->
                currentPoints = points
                Log.d("GameFragment", "Die Punkte $points")
                updateFlagStatus(points)
            }
            Log.d("GameFragment", "Discovered Devices: ${mainViewModel.discoveredDevices.value}")
            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun updateFlagStatus(points: List<Point>) {
        for (point in points) {
            val flagMarker = getFlagMarkerByPointId(point.id.toInt())
            flagMarker?.let {
                val imageResource = getColorFromTeamNumber(point.team)
                it.setImageResource(imageResource)
            }
        }
    }

    private fun getFlagMarkerByPointId(pointId: Int): ImageView? {
        val flagMarkers = binding.constraintLayout.children.filterIsInstance<ImageView>()
        return flagMarkers.firstOrNull { it.id == pointId }
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
        Log.d("GameFragment","Team is : $team")
        if (devices != null) {
            for(device in devices){
                mainViewModel.connectToServer(device,team)
            }
            mainViewModel.setIsDefended(true)
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

    private fun conquerPoint(id : String?, team: String){
        val game = mainViewModel.gameId.value
        val name = mainViewModel.name.value
        val token = mainViewModel.token.value
        mainViewModel.conquerPoint(game,id,team,name,token,{ response->
            Log.d("GameFragment","Das bekommen: $response")

        },{error->
            error?.let { showErrorToast(it) }

        })
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
