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
    ): View {
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
            updateLocationMarker(currentPosition)
        }
    }

    private fun updateMarkerPosition(location: Pair<Double, Double>) {
        val markerPosition = LocationUtils.generatePosition(Pair(location.first, location.second))
        mainViewModel.setMarkerPosition(markerPosition)
    }

    private fun updateLocationMarker(location: Pair<Double, Double>?) {
        location?.let { it ->
            updateMarkerPosition(Pair(it.first, it.second))
            val markerPosition = mainViewModel.markerPosition.value
            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height
            markerPosition?.let { updateMarkerViewPosition(it) }
            binding.target.visibility = View.VISIBLE
        }
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
            x = markerPosX
            y = markerPosY
        }
    }

    private fun addFlagMarker(
        currentPosition: Pair<Double, Double>?,
        markerPosition: Pair<Double, Double>?
    ) {
        currentPosition?.let { position -> conquestPoints.add(position) }
        val markerSize = 20
        markerPosition?.let {
            val flagMarker = createFlagMarker(R.drawable.circle_grey)
            binding.constraintLayout.addView(flagMarker)

            val mapImageWidth = binding.campusCard.width
            val mapImageHeight = binding.campusCard.height

            val markerPosX = LocationUtils.calculateMarkerPosX(markerPosition,mapImageWidth,markerSize)
            val markerPosY = LocationUtils.calculateMarkerPosY(markerPosition,mapImageHeight,markerSize)

            setViewConstraints(flagMarker, markerPosX.toDouble(), markerPosY.toDouble())
        }
    }

    private fun createFlagMarker( picture: Int): ImageView {
        val markerSize = 20
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
