package ru.molluk.git_authorization.ui.fragments.userShield

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.databinding.UserShieldFragmentBinding
import ru.molluk.git_authorization.utils.UiState
import kotlin.math.max

@AndroidEntryPoint
class UserShieldFragment : BottomSheetDialogFragment() {

    private var binding: UserShieldFragmentBinding? = null
    private val viewModel: UsersShieldViewModel by viewModels()
    private var isFirstCreation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            isFirstCreation = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UserShieldFragmentBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstCreation) {
            dismissAllowingStateLoss()
            return
        }

        initViews()
        initClickListener()
        setObserveListener()
        setupRecyclerViewSwipeToDelete()

        viewModel.getAllUsers()
    }

    private fun initViews() {
        binding?.recyclerView?.adapter = UsersReposAdapter()
    }

    private fun initClickListener() {
        binding?.addUserButton?.setOnClickListener {
            val resultBundle = Bundle().apply {
                putString(ACTION_TYPE_KEY, ACTION_ADD_NEW_USER)
            }
            parentFragmentManager.setFragmentResult(REQUEST_USER_KEY, resultBundle)
            dismiss()
        }

        (binding?.recyclerView?.adapter as UsersReposAdapter).apply {
            selectProfileListener = {
                val resultBundle = Bundle().apply {
                    putString(ACTION_TYPE_KEY, ACTION_USER_SELECTED)
                    putParcelable(SELECTED_USER_PROFILE_KEY, it)
                }
                parentFragmentManager.setFragmentResult(REQUEST_USER_KEY, resultBundle)
                dismiss()
            }
            deleteUserListener = { profile, position ->
                deleteUser(profile, position)
            }
        }
    }

    private fun setObserveListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usersProfiles.collect { users ->
                when (users) {
                    is UiState.Loading -> {
                        binding?.progressCircular?.show()
                    }

                    is UiState.Success -> {
                        binding?.progressCircular?.hide()
                        (binding?.recyclerView?.adapter as UsersReposAdapter).addData(users.data)
                    }

                    is UiState.Error -> {
                        binding?.progressCircular?.hide()
                        Toast.makeText(
                            requireContext(),
                            "${users.message}, ${users.throwable?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerViewSwipeToDelete() {
        val recyclerView = binding?.recyclerView ?: return
        val adapter = recyclerView.adapter as? UsersReposAdapter ?: return

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (viewHolder is UsersReposAdapter.UsersReposViewHolder) {
                    val foregroundView = viewHolder.getForegroundView()
                    val itemView = viewHolder.itemView
                    val maxSwipeDistancePx = itemView.width / 4f

                    var limitedDX = dX

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        if (dX < 0) {
                            limitedDX = max(dX, -maxSwipeDistancePx)
                        } else if (dX > 0) {
                            limitedDX = 0f
                        }
                    }

                    getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        foregroundView,
                        limitedDX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (viewHolder != null && viewHolder is UsersReposAdapter.UsersReposViewHolder) {
                    getDefaultUIUtil().onSelected(viewHolder.getForegroundView())
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                if (viewHolder is UsersReposAdapter.UsersReposViewHolder) {
                    getDefaultUIUtil().clearView(viewHolder.getForegroundView())
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = adapter.getUser(position)
                deleteUser(itemToDelete, position)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 1.0f
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private fun deleteUser(userProfile: UserProfile, position: Int) {
        val recyclerView = binding?.recyclerView ?: return
        val adapter = recyclerView.adapter as? UsersReposAdapter ?: return

        if (position != RecyclerView.NO_POSITION) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.user_removed, userProfile.login), Toast.LENGTH_SHORT).show()
            adapter.deleteDataPosition(position)

            val resultBundle = Bundle().apply {
                putString(ACTION_TYPE_KEY, ACTION_USER_DELETED)
                putParcelable(DELETED_USER_PROFILE_KEY, userProfile)
            }
            parentFragmentManager.setFragmentResult(REQUEST_USER_KEY, resultBundle)
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.RoundedTopBottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout

            bottomSheet?.let {
                it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.background_default)
                BottomSheetBehavior.from(it).apply {
                    skipCollapsed = true
                    isFitToContents = false
                    halfExpandedRatio = 0.5f
                    state = BottomSheetBehavior.STATE_HALF_EXPANDED

                    val itemCount = binding?.recyclerView?.adapter?.itemCount ?: 0
                    when (itemCount) {
                        0 -> {
                            halfExpandedRatio = 0.1f
                        }
                        1 -> {
                            halfExpandedRatio = 0.15f
                        }
                        in 2..5 -> {
                            halfExpandedRatio = itemCount * 0.1f
                        }
                        else -> {
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {}

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            if (slideOffset > 0) {
                                bottomSheet.layoutParams.height =
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                bottomSheet.requestLayout()
                            }
                        }
                    })
                }
            }
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isFirstCreation = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val REQUEST_USER_KEY = "REQUEST_USER_KEY"
        const val ACTION_TYPE_KEY = "ACTION_TYPE_KEY"
        const val ACTION_ADD_NEW_USER = "ACTION_ADD_NEW_USER"
        const val ACTION_USER_SELECTED = "ACTION_USER_SELECTED"
        const val ACTION_USER_DELETED = "ACTION_USER_DELETED"
        const val SELECTED_USER_PROFILE_KEY = "SELECTED_USER_PROFILE_KEY"
        const val DELETED_USER_PROFILE_KEY = "DELETED_USER_DELETED_KEY"
    }

}