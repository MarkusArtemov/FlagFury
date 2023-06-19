package de.hsfl.PixelPioneers.FlagFury

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentCreateBinding

class CreateFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentCreateBinding
    private val conquestPoints: MutableList<Pair<Double, Double>> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val lobbyButton: Button = binding.buttonCreateGame
        val cancelButton: Button = binding.buttonCancel
        val setFlagButton: Button = binding.buttonFlagPosition
        val name: EditText = binding.editTextName

        lobbyButton.setOnClickListener {
            mainViewModel.registerGame(
                name.text.toString(),
                conquestPoints,
                { gameId, token ->
                    mainViewModel.setGameId(gameId)
                    mainViewModel.setToken(token)
                    mainViewModel.setName(name.text.toString())
                    mainViewModel.setIsHost(true)
                    navController.navigate(R.id.action_createFragment_to_lobbyFragment)
                },
                { error ->
                    error?.let { showErrorToast(it) }
                }
            )
        }

        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_createFragment_to_homeScreen)
        }

        setFlagButton.setOnClickListener {
            addFlagMarker(
                mainViewModel.currentPosition.value,
                mainViewModel.markerPosition.value
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.currentPosition.observe(viewLifecycleOwner) { currentPosition ->
            Log.d("position", "Pooositiooon ${mainViewModel.currentPosition}")
            updateLocationMarker(currentPosition)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("de.hsfl.PixelPioneers.FlagFury.CreateFragment", "Periodic updates paused")
    }

    private fun updateMarkerPosition(location: Pair<Double, Double>) {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val posX = (location.first - tlLongitude) / (brLongitude - tlLongitude)
        val posY = (location.second - tlLatitude) / (brLatitude - tlLatitude)

        val markerPosition = Pair(posX, posY)
        mainViewModel.setMarkerPosition(markerPosition)
    }

    private fun updateLocationMarker(location: Pair<Double, Double>?) {
        location?.let { it ->
            updateMarkerPosition(Pair(it.first, it.second))
            Log.d("Locationvergleich create", " longi : ${location.first} lati : ${location.second}")
            val markerPosition = mainViewModel.markerPosition?.value
            Log.d("MarkerPosition", "$markerPosition")
            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height
            markerPosition?.let { updateMarkerViewPosition(it, mapImageWidth, mapImageHeight) }
            binding.target.visibility = View.VISIBLE
        }
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

    private fun addFlagMarker(
        currentPosition: Pair<Double, Double>?,
        markerPosition: Pair<Double, Double>?
    ) {
        currentPosition?.let { currentPosition -> conquestPoints.add(currentPosition) }
        val markerSize = 20
        markerPosition?.let {
            val flagMarker = createFlagMarker(markerSize, R.drawable.circle_grey)
            binding.constraintLayout.addView(flagMarker)

            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height

            val markerPosX = (markerPosition.first * mapImageWidth) - (markerSize / 2)
            val markerPosY = markerPosition.second * mapImageHeight - markerSize / 2

            Log.d("Position im Fragment", "$markerPosX $markerPosY")

            setViewConstraints(flagMarker, markerPosX, markerPosY)
        }
    }

    private fun createFlagMarker(markerSize: Int, picture: Int): ImageView {
        val flagMarker = ImageView(requireContext())
        flagMarker.id = View.generateViewId()
        flagMarker.setImageResource(picture)
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

    private fun showErrorToast(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }
}
