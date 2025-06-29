package ru.molluk.git_authorization.ui.fragments.login

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.databinding.LoginFragmentBinding
import ru.molluk.git_authorization.utils.CustomTextWatcher
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.fadeVisibility

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var binding: LoginFragmentBinding? = null

    private val viewModel: LoginViewModel by viewModels()

    private val args: LoginFragmentArgs by navArgs()

    private var loginStrBuilder = StringBuilder()
    private var tokenStrBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickers()
        setObserveListener()

        binding?.motionLayout?.transitionToState(R.id.initial_state_button_hidden, 0)
    }

    private fun initViews() {
        binding?.toolbar?.visibility = if (args.goBackFragment) {
            binding?.toolbar?.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            View.VISIBLE
        } else {
            View.GONE
        }
        binding?.loginTextInput?.fadeVisibility(View.VISIBLE, 250)
        binding?.loginTextEdit?.addTextChangedListener(object : CustomTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding?.loginTextInput?.error = ""
                loginStrBuilder.clear().append(s)
                if (s?.count()!! >= 1) {
                    binding?.buttonCardContainer?.isClickable = true
                    if (binding?.motionLayout?.currentState == R.id.initial_state_button_hidden) {
                        binding?.motionLayout?.transitionToState(
                            R.id.initial_state_button_visible,
                            250
                        )
                    }
                } else {
                    binding?.buttonCardContainer?.isClickable
                    if (tokenStrBuilder.isEmpty() && loginStrBuilder.isEmpty()) {
                        binding?.motionLayout?.transitionToState(
                            R.id.initial_state_button_hidden,
                            250
                        )
                    }
                }
            }
        })

        binding?.tokenTextInput?.fadeVisibility(View.VISIBLE, 250)
        binding?.tokenTextEdit?.addTextChangedListener(object : CustomTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding?.tokenTextInput?.error = ""
                tokenStrBuilder.clear().append(s)
                if (s?.count()!! >= 6) {
                    binding?.buttonCardContainer?.isClickable = true
                    if (binding?.motionLayout?.currentState == R.id.initial_state_button_hidden) {
                        binding?.motionLayout?.transitionToState(
                            R.id.initial_state_button_visible,
                            250
                        )
                    }
                } else {
                    binding?.buttonCardContainer?.isClickable = false
                    if (loginStrBuilder.isEmpty() && tokenStrBuilder.isEmpty()) {
                        binding?.motionLayout?.transitionToState(
                            R.id.initial_state_button_hidden,
                            250
                        )
                    }
                }
            }
        })
    }

    private fun initClickers() {
        binding?.buttonCardContainer?.setOnClickListener {
            if (tokenStrBuilder.isEmpty() && loginStrBuilder.isNotEmpty()) {
                viewModel.getUserResponse(login = loginStrBuilder.toString())
            } else if (tokenStrBuilder.isNotEmpty() && loginStrBuilder.isEmpty()) {
                viewModel.getUserResponse(token = "$tokenStrBuilder")
            } else if (tokenStrBuilder.isNotEmpty() && loginStrBuilder.isNotEmpty()) {
                viewModel.getUserResponse(token = "$tokenStrBuilder")
            } else {
                binding?.buttonCardContainer?.isClickable = false
                binding?.motionLayout?.transitionToState(R.id.start_button_visible)
                Toast.makeText(requireContext(), "Enter login or token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setObserveListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { state ->
                when (state) {
                    is UiState.Error -> {
                        val userOrToken = if (loginStrBuilder.isNotEmpty()) {
                            binding?.loginTextInput?.error = state.message
                            "user: $loginStrBuilder"
                        } else {
                            binding?.tokenTextInput?.error = state.message
                            "token: $tokenStrBuilder"
                        }
                        Log.e(
                            this.javaClass.simpleName,
                            "Error loading $userOrToken, ${state.message}"
                        )

                        loadingProcess(false)
                        binding?.motionLayout?.transitionToState(R.id.start_button_visible)
                    }

                    is UiState.Loading -> {
                        loadingProcess(true)
                        binding?.motionLayout?.transitionToState(R.id.end_progress_visible)
                    }

                    is UiState.Success -> {}

                    else -> {
                        loadingProcess(false)
                        binding?.motionLayout?.transitionToState(R.id.start_button_visible)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { user ->
                loadingProcess(false)
                user?.let {
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment(it)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun loadingProcess(isLoading: Boolean) {
        binding?.loginTextInput?.isEnabled = !isLoading
        binding?.loginTextEdit?.isEnabled = !isLoading
        binding?.tokenTextInput?.isEnabled = !isLoading
        binding?.tokenTextEdit?.isEnabled = !isLoading
        binding?.buttonCardContainer?.isClickable = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}