package de.hsfl.PixelPioneers.FlagFury

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentGameBinding
import org.json.JSONArray
import org.json.JSONObject


class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private var playerList = emptyList<JSONObject>()

    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val leaveButton: Button = binding.button

        leaveButton.setOnClickListener {
            val snackBar = Snackbar.make(requireView(), "Möchtest du das Spiel wirklich verlassen?", Snackbar.LENGTH_LONG)
            snackBar.setAction("Ja") {
                mainViewModel.removePlayer(mainViewModel.getGameId(), mainViewModel.getName(), mainViewModel.getToken(),
                    { game, name ->
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askForConquestPoints { points ->
            createAllFlags(points)
        }
        startPeriodicUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopPeriodicUpdate()
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    private fun askForConquestPoints(callback : (points : List<Point>) -> Unit){
        mainViewModel.getPoints(
            mainViewModel.getGameId(),
            mainViewModel.getName(),
            mainViewModel.getToken(),
            {points, state, game ->
                Log.d("GameFragment", "$points $state $game")
                points?.let { callback(it) }
            },
            {  error ->
                Log.d("GameFragment", "Es ist zu einem Fehler gekommen")
            }

        )
    }
    private fun updateLocationMarker(){
        var location = mainViewModel.getCurrentPosition()
        location?.let {
            updateMarkerPosition(location)
            val markerPosition = mainViewModel.getMarkerPosition()
            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height
            markerPosition?.let { it1 -> updateMarkerViewPosition(it1, mapImageWidth, mapImageHeight) }
            binding.target.visibility = View.VISIBLE
        }
    }



    fun getPlayers(){
        mainViewModel.getPlayers(mainViewModel.getGameId(), mainViewModel.getName(), mainViewModel.getToken(), { players ->
            players?.let {
                val playerJSONArray = it.getJSONArray("players")
                this.playerList = jsonArrayToList(playerJSONArray)
                Log.d("GameFragment", " Spielerliste : $playerList")
            }
        }, { error ->
            error?.let { showErrorToast(it) }
        })
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(jsonObject)
        }
        return list
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed({
            updateLocationMarker()
            askForConquestPoints { points ->
                updateFlagStatus(points)
            }
            getPlayers()
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


    private fun getColorFromTeamNumber(team : Int) : Int {
        return when (team) {
            -1 -> R.drawable.circle_yellow
            0 -> R.drawable.circle_grey
            1 -> R.drawable.circle_red
            2 -> R.drawable.circle_blue
            else -> R.drawable.circle_grey
        }
    }


    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateMarkerPosition(position : Pair<Double, Double>) {
        val markerPosition = generatePosition(position)
        mainViewModel.setMarkerPosition(markerPosition)
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



    private fun createAllFlags(points : List<Point>){
        for (point in points){
            addFlagMarker(point)
        }
    }

    private fun addFlagMarker(point: Point, ) {

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











