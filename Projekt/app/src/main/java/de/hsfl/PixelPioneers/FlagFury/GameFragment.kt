package de.hsfl.PixelPioneers.FlagFury

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.MainActivity
import de.hsfl.PixelPioneers.FlagFury.MainViewModel
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentGameBinding

class GameFragment : Fragment() {
    private lateinit var binding: FragmentGameBinding
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var currentPositionObserver: Observer<Location>
    private lateinit var markerPositionObserver: Observer<Pair<Double, Double>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val leaveButton: Button = binding.button

        leaveButton.setOnClickListener {
            navController.navigate(R.id.action_gameFragment_to_homeScreen)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPositionObserver = Observer { location ->
            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height
            updateMarkerPosition(location, mapImageWidth, mapImageHeight)
        }

        markerPositionObserver = Observer { markerPosition ->
            binding.target.x = markerPosition.first.toFloat()
            Log.d("GameFragment",binding.target.x.toString())
            binding.target.y = markerPosition.second.toFloat()
            Log.d("GameFragment",binding.target.y.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.currentPosition.observe(viewLifecycleOwner, currentPositionObserver)
        mainViewModel.markerPosition.observe(viewLifecycleOwner, markerPositionObserver)
        (requireActivity() as MainActivity).requestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.currentPosition.removeObserver(currentPositionObserver)
        mainViewModel.markerPosition.removeObserver(markerPositionObserver)
    }

    private fun updateMarkerPosition(location: Location, mapImageWidth: Int, mapImageHeight: Int) {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val posX = (location.longitude - tlLongitude) / (brLongitude - tlLongitude)
        val posY = (location.latitude - tlLatitude) / (brLatitude - tlLatitude)

        val markerPosX = posX * mapImageWidth
        val markerPosY = posY * mapImageHeight

        val markerPosition = Pair(markerPosX, markerPosY)
        mainViewModel.setMarkerPosition(markerPosition)
    }
}
