package de.hsfl.PixelPioneers.FlagFury

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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


class CreateFragment : Fragment() {
    private val mainViewModel : MainViewModel by activityViewModels()
    private lateinit var binding: FragmentCreateBinding
    private val updateInterval: Long = 1000
    private val handler = Handler(Looper.getMainLooper())
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater,container,false)
        val navController = findNavController();
        val lobbyButton : Button = binding.buttonCreateGame
        val cancelButton : Button = binding.buttonCancel
        val setFlagButton : Button = binding.buttonFlagPosition
        val name : EditText = binding.editTextName
        val pointsList = mainViewModel.getConquestPoints()

        lobbyButton.setOnClickListener {
            mainViewModel.registerGame(name.text.toString(), mainViewModel.getConquestPoints() , { gameId, token ->
                mainViewModel.setGameId(gameId)
                mainViewModel.setToken(token)
                mainViewModel.setName(name.text.toString())
                navController.navigate(R.id.action_createFragment_to_lobbyFragment)
            }, { error ->
                error?.let { showErrorToast(it) }
            })
        }



        setFlagButton.setOnClickListener{
            startPeriodicUpdate()
        }


        cancelButton.setOnClickListener{
            navController.navigate(R.id.action_createFragment_to_homeScreen)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        stopPeriodicUpdate()
        Log.d("de.hsfl.PixelPioneers.FlagFury.CreateFragment", "Periodic updates paused")
    }

    private fun startPeriodicUpdate() {
        handler.postDelayed({
            val location = mainViewModel.getCurrentPosition()
            if (location != null) {
                updateMarkerPosition(location)
            }

            val markerPosition = mainViewModel.getMarkerPosition()
            if (markerPosition != null) {
                val mapImageWidth = binding.campusCard.width
                val mapImageHeight = binding.campusCard.height
                updateMarkerViewPosition(markerPosition, mapImageWidth, mapImageHeight)
                binding.target.visibility = View.VISIBLE
            }

            startPeriodicUpdate()
        }, updateInterval)
    }

    private fun stopPeriodicUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateMarkerPosition(location: Location) {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val posX = (location.longitude - tlLongitude) / (brLongitude - tlLongitude)
        val posY = (location.latitude - tlLatitude) / (brLatitude - tlLatitude)

        Log.d("posX", "$posX")
        Log.d("posY", "$posY")

        val markerPosition = Pair(posX, posY)
        mainViewModel.setMarkerPosition(markerPosition)
    }

    private fun updateMarkerViewPosition(markerPosition: Pair<Double, Double>, mapImageWidth: Int, mapImageHeight: Int) {
        val markerPosX = markerPosition.first
        val markerPosY = markerPosition.second

        val markerViewWidth = binding.target.width
        val markerViewHeight = binding.target.height

        val adjustedMarkerPosX = markerPosX * mapImageWidth - markerViewWidth / 2
        val adjustedMarkerPosY = markerPosY * mapImageHeight - markerViewHeight / 2

        binding.target.x = adjustedMarkerPosX.toFloat()
        binding.target.y = adjustedMarkerPosY.toFloat()

        Log.d("de.hsfl.PixelPioneers.FlagFury.CreateFragment", "Marker view position updated: ($adjustedMarkerPosX, $adjustedMarkerPosY)")
    }


    private fun showErrorToast(error : String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }
    }


