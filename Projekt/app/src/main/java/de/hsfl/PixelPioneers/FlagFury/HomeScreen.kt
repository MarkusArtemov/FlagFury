package de.hsfl.PixelPioneers.FlagFury

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.PixelPioneers.FlagFury.MainViewModel
import de.hsfl.PixelPioneers.FlagFury.R
import de.hsfl.PixelPioneers.FlagFury.databinding.FragmentHomeScreenBinding

class HomeScreen : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val hostButton: Button = binding.hostGame
        val joinGameButton: Button = binding.joinGame

        hostButton.setOnClickListener {
            navController.navigate(R.id.action_homeScreen_to_createFragment)
        }

        joinGameButton.setOnClickListener {
            navController.navigate(R.id.action_homeScreen_to_joinFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkGreyColor = Color.parseColor("#4F4F4F")
        val whiteColor = Color.WHITE
        val duration = 5000L

        val backgroundView: View = binding.root

        val colorAnimator = ObjectAnimator.ofObject(
            backgroundView,
            "backgroundColor",
            ArgbEvaluator(),
            darkGreyColor,
            whiteColor
        )
        colorAnimator.duration = duration
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.interpolator = AccelerateInterpolator()

        colorAnimator.start()
    }
}
