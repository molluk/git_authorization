package ru.molluk.git_authorization.ui.fragments.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_TYPE_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_USER_DELETED
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.ACTION_USER_SELECTED
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.DELETED_USER_PROFILE_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.REQUEST_USER_KEY
import ru.molluk.git_authorization.ui.fragments.userShield.UserShieldFragment.Companion.SELECTED_USER_PROFILE_KEY
import ru.molluk.git_authorization.utils.fadeVisibility
import ru.molluk.git_authorization.utils.getParcelableFromBundle

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: HomeFragmentBinding? = null
    private val args: HomeFragmentArgs by navArgs()
    private val viewModel: HomeViewModel by viewModels()

    private var currentPage = 1
    private var isLastPage = false

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

        args.userProfile?.let {
            viewModel.switchActiveUserProfile(it)
        }
    }

    private fun initViews() {
        binding?.reposRv?.adapter = GitReposAdapter()

        binding?.refreshLayout?.setOnRefreshListener {
            viewModel.loadCurrentActiveUser()
        }

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
                        bundle.getParcelableFromBundle(SELECTED_USER_PROFILE_KEY)

                    selectedProfile?.let { profile ->
                        val currentUserId =
                            (viewModel.user.value as? UiState.Success)?.data?.first?.id.toString()
                        if (profile.id != currentUserId) {
                            viewModel.switchActiveUserProfile(profile)
                        }
                    }
                }

                ACTION_USER_DELETED -> {
                    val deletedProfile: UserProfile? =
                        bundle.getParcelableFromBundle(DELETED_USER_PROFILE_KEY)
                    deletedProfile?.let {
                        viewModel.deleteUser(it)
                    }
                }
            }
        }
    }

    private fun initClickers() {
        binding?.toolbar?.setOnClickListener {
            val userShieldFragment = UserShieldFragment()
            userShieldFragment.show(childFragmentManager, userShieldFragment.javaClass.simpleName)
        }

        (binding?.reposRv?.adapter as GitReposAdapter).apply {
            copyLinkClickListener = { item ->
                val clipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText(item.name, item.htmlUrl)
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
                val currentUserLogin =
                    (viewModel.user.value as? UiState.Success)?.data?.first?.login
                if (!isLastPage && currentUserLogin != null) {
                    currentPage++
                    viewModel.loadReposForCurrentUser(currentUserLogin, currentPage)
                }
            }
        }
    }

    private fun setObserveListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speedKbps.collect { speed ->
                    binding?.networkSpeedKbps?.text =
                        requireContext().getString(R.string.network_speed, speed)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectLatest { combinedState ->
                binding?.refreshLayout?.isRefreshing = combinedState is UiState.Loading
                when (combinedState) {
                    is UiState.Loading -> {
                        showUserLoadingState(true)
                        (binding?.reposRv?.adapter as GitReposAdapter).clearData()
                        isLastPage = false
                        currentPage = 1
                    }

                    is UiState.Success -> {
                        val userResponse = combinedState.data.first
                        val activeProfile = combinedState.data.second

                        showUserLoadingState(false)
                        updateUserInfo(userResponse, activeProfile)
                        viewModel.loadReposForCurrentUser(userResponse.login, currentPage)
                    }

                    is UiState.Error -> {
                        showUserLoadingState(false)

                        binding?.refreshLayout?.isRefreshing = false
                        Log.e(
                            this.javaClass.simpleName,
                            "${combinedState.message}, ${combinedState.throwable}: stackTrace: "
                        )
                        combinedState.throwable?.printStackTrace()

                        Toast.makeText(
                            requireContext(),
                            combinedState.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repos.collect { repos ->
                val isLoading = repos is UiState.Loading
                (binding?.reposRv?.adapter as GitReposAdapter).setLoading(isLoading)

                when (repos) {
                    is UiState.Loading -> {
                        if (currentPage == 1) {
                            showReposLoadingState(true)
                        }

                        binding?.progressRv?.show()
                        (binding?.reposRv?.adapter as GitReposAdapter).setLoading(true)
                    }

                    is UiState.Success -> {
                        binding?.progressRv?.hide()
                        showReposLoadingState(false)

                        val data = repos.data
                        val reposAdapter = (binding?.reposRv?.adapter as GitReposAdapter)
                        reposAdapter.setLoading(false)

                        if (data.isNotEmpty()) {
                            if (currentPage == 1) {
                                reposAdapter.setData(data)
                            } else {
                                reposAdapter.addData(data)
                            }
                            isLastPage = false
                        } else {
                            isLastPage = true
                        }

                        binding?.refreshLayout?.isRefreshing = false
                    }

                    is UiState.Error -> {
                        binding?.progressRv?.hide()
                        binding?.refreshLayout?.isRefreshing = false
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

    private fun updateUserInfo(user: UserResponse, activeProfile: UserProfile?) {
        binding?.profileLogin?.text = user.login
        binding?.profileId?.text = getString(R.string.user_id, user.id.toString())
        binding?.profileCreatedDate?.text = user.createdAt.formatToDateYMD()

        binding?.profileToken?.apply {
            if (activeProfile?.accessToken.isNullOrEmpty()) {
                text = getString(R.string.token_not_specified)
                setTextColor(ContextCompat.getColor(context, R.color.error_default))
            } else {
                text = getString(R.string.token_specified)
                setTextColor(ContextCompat.getColor(context, R.color.element_active))
            }
        }
        Glide.with(requireContext()).load(user.avatarUrl).into(binding?.profileAvatar!!)
    }

    private fun showUserLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding?.shimmerToolbar?.visibility = View.VISIBLE
            binding?.shimmerToolbar?.startShimmer()
            binding?.profileCl?.visibility = View.GONE
            binding?.shimmerRepos?.visibility = View.VISIBLE
            binding?.shimmerRepos?.startShimmer()
            binding?.reposRv?.visibility = View.GONE
        } else {
            binding?.shimmerToolbar?.stopShimmer()
            binding?.shimmerToolbar?.visibility = View.GONE
            binding?.profileCl?.visibility = View.VISIBLE
        }
    }

    private fun showReposLoadingState(isLoading: Boolean) {
        if (isLoading && currentPage == 1) {
            binding?.shimmerRepos?.visibility = View.VISIBLE
            binding?.shimmerRepos?.startShimmer()
            binding?.reposRv?.visibility = View.GONE
        } else {
            binding?.shimmerRepos?.hideShimmer()
            binding?.shimmerRepos?.visibility = View.GONE
            binding?.reposRv?.visibility = View.VISIBLE
            binding?.progressRv?.hide()
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