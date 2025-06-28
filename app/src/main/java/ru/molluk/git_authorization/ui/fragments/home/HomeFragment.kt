package ru.molluk.git_authorization.ui.fragments.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.databinding.HomeFragmentBinding
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.formatToDateYMD
import kotlin.getValue
import androidx.core.net.toUri
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_TYPE_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_USER_DELETED
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_USER_SELECTED
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.DELETED_USER_PROFILE_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.REQUEST_USER_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.SELECTED_USER_PROFILE_KEY
import ru.molluk.git_authorization.utils.fadeVisibility

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: HomeFragmentBinding? = null
    private val args: HomeFragmentArgs by navArgs()

    private val viewModel: HomeViewModel by viewModels()

    val userBottomShield = UserShieldFragment()

    private lateinit var userProfile: UserProfile
    private lateinit var userResponse: UserResponse

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingData = false
    private var isUserDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initClickers()
        setObserveListener()
    }

    private fun initViews() {
        args.userProfile?.let {
            userProfile = it
            viewModel.getUserResponse(it)
        }
        binding?.reposRv?.adapter = GitReposAdapter()

        childFragmentManager.setFragmentResultListener(
            REQUEST_USER_KEY,
            this
        ) { requestKey, bundle ->
            when (bundle.getString(ACTION_TYPE_KEY)) {
                UserShieldFragment.ACTION_ADD_NEW_USER -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment(true)
                    findNavController().navigate(action)
                }

                ACTION_USER_SELECTED -> {
                    val selectedProfile: UserProfile? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle.getParcelable(SELECTED_USER_PROFILE_KEY, UserProfile::class.java)
                        } else {
                            bundle.getParcelable(SELECTED_USER_PROFILE_KEY)
                        }

                    selectedProfile?.let {
                        currentPage = 1
                        this.userProfile = it
                        viewModel.getUserResponse(userProfile = it, changeUser = true)
                    }
                }

                ACTION_USER_DELETED -> {
                    val userDeleted: UserProfile? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle.getParcelable(DELETED_USER_PROFILE_KEY, UserProfile::class.java)
                        } else {
                            bundle.getParcelable(DELETED_USER_PROFILE_KEY)
                        }
                    userDeleted?.let {
                        isUserDeleted = true
                        viewModel.deleteUser(it)
                    }
                }
            }
        }
    }

    private fun initClickers() {
        binding?.toolbar?.setOnClickListener {
            userBottomShield.show(childFragmentManager, userBottomShield.javaClass.simpleName)
        }

        (binding?.reposRv?.adapter as GitReposAdapter).apply {
            copyLinkClickListener = { item ->
                val clipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText(item.name, item.url)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.repos_link_copy, item.name),
                    Toast.LENGTH_SHORT
                ).show()
            }
            goToRepoClickListener = { item ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = item.htmlUrl.toUri()

                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        val chooser = Intent.createChooser(intent, "")
                        requireActivity().startActivity(chooser)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.app_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(
                        this@HomeFragment.javaClass.simpleName,
                        requireContext().getString(R.string.url_error_open, item.url),
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.url_error_open, e.localizedMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            loadNextPage = {
                if (!isLastPage && !isLoadingData) {
                    currentPage++
                    viewModel.getRepository(userProfile, currentPage)
                }
            }
        }
    }

    private fun setObserveListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { user ->
                when (user) {
                    is UiState.Loading -> {
                        binding?.progressCircular?.show()
                    }

                    is UiState.Success -> {
                        userProfile = user.data
                        viewModel.getUserResponse(userProfile)
                    }

                    is UiState.Error -> {
                        Log.e(
                            this.javaClass.simpleName,
                            "${
                                requireContext().getString(
                                    R.string.user_error_removed,
                                    user.message
                                )
                            }, ${user.throwable}"
                        )
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.user_error_removed, user.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                when (user) {
                    is UiState.Loading -> {
                        binding?.progressCircular?.show()
                        (binding?.reposRv?.adapter as GitReposAdapter).clearData()
                    }

                    is UiState.Success -> {
                        binding?.progressCircular?.hide()
                        userResponse = user.data
                        binding?.profileLogin?.text = userResponse.login
                        binding?.profileId?.text =
                            requireContext().getString(R.string.user_id, userResponse.id.toString())
                        binding?.profileCreatedDate?.text = userResponse.createdAt.formatToDateYMD()
                        binding?.profileToken?.apply {
                            if (userProfile.accessToken.isNullOrEmpty()) {
                                text = requireContext().getString(R.string.token_not_specified)
                                setTextColor(ContextCompat.getColor(context, R.color.error_default))
                            } else {
                                text = requireContext().getString(R.string.token_specified)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.element_active
                                    )
                                )
                            }
                        }
                        Glide.with(requireContext()).load(userResponse.avatarUrl)
                            .into(binding?.profileAvatar!!)

                        viewModel.getAllUsers()
                        viewModel.getRepository(userProfile)
                    }

                    is UiState.Error -> {
                        binding?.progressCircular?.hide()
                        Log.e(
                            this.javaClass.simpleName,
                            "${user.message}, ${user.throwable}: stackTrace: "
                        )
                        user.throwable?.printStackTrace()

                        Toast.makeText(
                            requireContext(),
                            user.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repos.collect { repos ->
                when (repos) {
                    is UiState.Loading -> {
                        binding?.progressRv?.show()
                        (binding?.reposRv?.adapter as GitReposAdapter).setLoading(true)
                    }

                    is UiState.Success -> {
                        binding?.progressRv?.hide()
                        val data = repos.data
                        if (data.isNotEmpty()) {
                            (binding?.reposRv?.adapter as GitReposAdapter).addData(data)
                        } else {
                            isLastPage = true
                        }
                        (binding?.reposRv?.adapter as GitReposAdapter).setLoading(false)
                    }

                    is UiState.Error -> {
                        (binding?.reposRv?.adapter as GitReposAdapter).setLoading(false)
                        binding?.progressRv?.hide()
                        Log.e(
                            this.javaClass.simpleName,
                            "${
                                requireContext().getString(
                                    R.string.repos_error_loading,
                                    repos.message
                                )
                            }, ${repos.throwable}"
                        )
                        Toast.makeText(
                            requireContext(),
                            repos.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collect { users ->
                when (users) {
                    is UiState.Loading -> {
                        binding?.profileCountCw?.fadeVisibility(View.GONE)
                    }

                    is UiState.Success -> {
                        if (users.data.isNotEmpty()) {
                            binding?.profileCountCw?.fadeVisibility(if (users.data.size > 1) View.VISIBLE else View.GONE)
                            binding?.profileCountText?.text = (users.data.size).toString()
                            if (isUserDeleted) {
                                val nextUser = users.data.first { it.id != userResponse.id.toString() }
                                userProfile = nextUser
                                viewModel.saveUser(nextUser)
                                viewModel.getUserResponse(nextUser)
                            }
                            isUserDeleted = false
                        } else {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentLogoutToLoginFragment(false)
                            findNavController().navigate(action)
                        }
                    }

                    is UiState.Error -> {
                        binding?.profileCountCw?.fadeVisibility(View.GONE)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}