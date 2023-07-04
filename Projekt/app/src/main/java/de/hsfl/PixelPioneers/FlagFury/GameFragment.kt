package de.hsfl.PixelPioneers.FlagFury

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
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentGameBinding

class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())
    private var currentPoints: List<Point> = emptyList()
    private var timerIsRunning = false
    private var currentPoint: Point? = null
    private var _isDefended = false
    private var gameOver = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        setupLeaveGameButton()
        binding.target.visibility = View.VISIBLE
        return binding.root
    }

    private fun setupLeaveGameButton() {
        val leaveButton: Button = binding.button
        leaveButton.setOnClickListener {
            showExitConfirmation()
        }
    }

    private fun showExitConfirmation() {
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
                    stopPeriodicUpdate()
                    mainViewModel.cancelAllRequests()
                    findNavController().navigate(R.id.action_gameFragment_to_homeScreen)
                },
                { error ->
                    showErrorToast("Fehler bei der Abmeldung")
                }
            )
        }
        snackBar.show()
    }



    val timer = object: CountDownTimer(10_000, 1_000) {
        override fun onTick(millisUntilFinished: Long) {
            if(LocationUtils.checkConquerPoint(currentPoint!!,mainViewModel.currentPosition.value!!,mainViewModel.team.value!!)){
                connectToOpponents()
            }
            if (
                !LocationUtils.checkConquerPoint(currentPoint!!, mainViewModel.currentPosition.value!!,mainViewModel.team.value!!)
                || _isDefended
            ) {
                conquerPoint(currentPoint?.id, "${mainViewModel.oldConquerPointTeam.value}")
                timerIsRunning = false
                cancel()
            }

        }

        override fun onFinish(){
            timerIsRunning = false
            currentPoint?.let {
                conquerPoint(it.id, "${mainViewModel.team.value}")

                currentPoint = null
            }
        }
    }


    private fun getPlayers() {
        mainViewModel.getPlayers{error-> error?.let { showErrorToast(it) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        askForConquestPoints { points ->
            createAllFlags(points)
        }

        observeLiveData()

        startPeriodicUpdate()
    }

    private fun observeLiveData() {
        mainViewModel.isDefended.observe(viewLifecycleOwner) { isDefended ->
            _isDefended = isDefended
            Log.d("GameFragmet","is defended $_isDefended")
        }

        mainViewModel.currentPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                handlePositionUpdate(it)
            }
        })
    }

    private fun handlePositionUpdate(position: Pair<Double, Double>) {
        updateMarkerPosition(position)

        for (point in currentPoints){
            if (LocationUtils.checkConquerPoint(point, position, mainViewModel.team.value!!) && !timerIsRunning){
                handleConquestPoint(point)
            }
        }
    }

    private fun handleConquestPoint(point: Point) {
        timerIsRunning = true
        if(point.team!=-1){
            mainViewModel.setOldConquerPointTeamValue("${point.team}")
        }
        conquerPoint(point.id, "-1")
        currentPoint = point
        timer.start()
    }


    override fun onPause() {
        super.onPause()
        stopPeriodicUpdate()
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    private fun askForConquestPoints(callback: (points: List<Point>) -> Unit) {
        mainViewModel.getPoints { points, state ->
            handleConquestPoints(points, state,mainViewModel.gameId.value,callback)
        }
    }

    private fun handleConquestPoints(
        points: List<Point>?,
        state: String?,
        game: String?,
        callback: (points: List<Point>) -> Unit
    ) {
        if (state == "2" && !gameOver) {
            handleGameOver(points)
        }

        handleConquestResult(points)

        callback(points ?: emptyList())
    }

    private fun handleGameOver(points: List<Point>?) {
        gameOver = true
        points?.let {
            if (checkAllPointsSameTeam(points, 1)) {
                navigateToWinFragment(R.id.action_gameFragment_to_winRedFragment)
            } else if (checkAllPointsSameTeam(points, 2)) {
                navigateToWinFragment(R.id.action_gameFragment_to_winBlueFragment)
            }
        }
    }

    private fun handleConquestResult(points: List<Point>?) {
        points?.let {
            if (checkAllPointsSameTeam(points, 1)) {
                mainViewModel.endGame()
            } else if (checkAllPointsSameTeam(points, 2)) {
                mainViewModel.endGame()
            }
        }
    }

    private fun navigateToWinFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    private fun checkAllPointsSameTeam(points: List<Point>, team: Int): Boolean {
        val filteredPoints = points.filter { it.team == team }
        return filteredPoints.size == points.size
    }



    private fun startPeriodicUpdate() {
        handler.postDelayed({
            getPlayers()
            askForConquestPoints { points ->
                currentPoints = points
                updateFlagStatus(points)
            }
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
        val devicesMap = mainViewModel.discoveredDevices.value
        val team = if (mainViewModel.team.value == 1) "blau" else "rot"
        if (devicesMap != null) {
            for(device in devicesMap.values){
                mainViewModel.connectToServer(device,team)
            }
        }
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateMarkerPosition(position: Pair<Double, Double>) {
        val markerPosition = LocationUtils.generatePosition(position)
        updateMarkerViewPosition(markerPosition)
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
    ) {

        val markerViewWidth = binding.target.width
        val markerViewHeight = binding.target.height
        val mapImageWidth = binding.campusCard.width
        val mapImageHeight = binding.campusCard.height

        val markerPosX = LocationUtils.calculateMarkerPosX(markerPosition,mapImageWidth,markerViewWidth)
        val markerPosY = LocationUtils.calculateMarkerPosY(markerPosition,mapImageHeight,markerViewHeight)

        with(binding.target) {
            x = markerPosX.toFloat()
            y = markerPosY.toFloat()
        }
    }

    private fun createAllFlags(points: List<Point>) {
        for (point in points) {
            addFlagMarker(point)
        }
    }

    private fun addFlagMarker(point: Point) {
        val mapImageWidth = binding.campusCard.width
        val mapImageHeight = binding.campusCard.height
        val markerSize = 20

        val flagPosition = LocationUtils.generatePosition(Pair(point.longitude, point.latitude))

        flagPosition.let { position ->
            val flagMarker = createFlagMarkerForPoint(point)

            val markerPosX = LocationUtils.calculateMarkerPosX(position, mapImageWidth, markerSize)
            val markerPosY = LocationUtils.calculateMarkerPosY(position, mapImageHeight, markerSize)

            setViewConstraints(flagMarker, markerPosX.toDouble(), markerPosY.toDouble())
        }
    }

    private fun createFlagMarkerForPoint(point: Point): ImageView {
        val markerSize = 20
        val imageResource = getColorFromTeamNumber(point.team)
        return ImageView(requireContext()).apply {
            setImageResource(imageResource)
            layoutParams = ViewGroup.LayoutParams(markerSize, markerSize)
            id = point.id.toInt()
            binding.constraintLayout.addView(this)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.stopServer()
        mainViewModel.stopDiscoverDevices()
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
