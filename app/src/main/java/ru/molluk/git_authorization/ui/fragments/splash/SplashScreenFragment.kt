package ru.molluk.git_authorization.ui.fragments.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.databinding.SplashScreenFragmentBinding
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.fadeVisibility

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenFragment : Fragment() {
    private var binding: SplashScreenFragmentBinding? = null
    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplashScreenFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.progressCircular?.show()

        binding?.let { bind ->
            viewModel.fetchOctocat()
        }

        setObserveListener()
    }

    private fun setObserveListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.octocat.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding?.progressCircular?.show()
                    }

                    is UiState.Success -> {
                        viewModel.getUserActive()
                    }

                    is UiState.Error -> {
                        Log.e(this.javaClass.simpleName, "Error loading octocat: ${state.message}")
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isNetworkAvailable.collect { isAvailable ->
                if (isAvailable) {
                    Log.d(this.javaClass.simpleName, "Интернет доступен")
                } else {
                    Log.e(this.javaClass.simpleName, "Интернет недоступен")
                    viewModel.octocat.collect { state ->
                        if (viewModel.octocat.replayCache.lastOrNull() !is UiState.Loading &&
                            viewModel.octocat.replayCache.lastOrNull() !is UiState.Success
                        ) {
                            binding?.progressCircular?.fadeVisibility(View.GONE)
                            Toast.makeText(
                                requireContext(),
                                "Нет подключения к интернету",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                when (user) {
                    is UiState.Loading -> {
                        binding?.progressCircular?.show()
                    }
                    is UiState.Success -> {
                        binding?.progressCircular?.fadeVisibility(View.GONE, 200, onEnd = {
                            val action = SplashScreenFragmentDirections.actionSplashFragmentToHomeFragment(user.data)
                            findNavController().navigate(action)
                        })
                    }
                    is UiState.Error -> {
                        binding?.progressCircular?.fadeVisibility(View.GONE, 200, onEnd = {
                            binding?.let { bind ->
                                animateViewToBias(bind.gitLogoIv, 0.2f, bind.root, 200) {
                                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    private fun animateViewToBias(
        view: View,
        targetBias: Float,
        parent: ViewGroup,
        duration: Long = 500L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        val constraintLayout = parent as ConstraintLayout

        val transition = AutoTransition()
        transition.duration = duration
        onAnimationEnd?.let { entAnim ->
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(p0: Transition?) {}
                override fun onTransitionEnd(p0: Transition?) {
                    entAnim.invoke()
                    transition.removeListener(this)
                }

                override fun onTransitionCancel(p0: Transition?) {}
                override fun onTransitionPause(p0: Transition?) {}
                override fun onTransitionResume(p0: Transition?) {}
            })
        }

        val startSet = ConstraintSet()
        startSet.clone(constraintLayout)

        val endSet = ConstraintSet()
        endSet.clone(constraintLayout)

        endSet.setVerticalBias(view.id, targetBias)

        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        endSet.applyTo(constraintLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}