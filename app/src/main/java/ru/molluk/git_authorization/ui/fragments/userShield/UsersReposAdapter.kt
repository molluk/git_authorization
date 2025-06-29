package ru.molluk.git_authorization.ui.fragments.userShield

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.databinding.UsersReposItemBinding
import ru.molluk.git_authorization.ui.fragments.userShield.UsersReposAdapter.UsersReposViewHolder
import ru.molluk.git_authorization.utils.formatToDateYMD

class UsersReposAdapter() : RecyclerView.Adapter<UsersReposViewHolder>() {

    private var usersList = mutableListOf<UserProfile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersReposViewHolder {
        val binding =
            UsersReposItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersReposViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersReposViewHolder, position: Int) {
        val currentItem = usersList[position]
        holder.bind(currentItem, position)
    }

    override fun getItemCount() = usersList.size

    fun addData(newItems: List<UserProfile>) {
        usersList.addAll(newItems)
    }

    fun getUser(position: Int) = usersList[position]

    fun getSize() = usersList.size

    fun deleteDataPosition(position: Int) {
        usersList.removeAt(position)
        notifyItemRemoved(position)
    }

    var selectProfileListener: ((UserProfile) -> Unit)? = null
    var deleteUserListener: ((UserProfile, Int) -> Unit)? = null

    inner class UsersReposViewHolder(
        private val binding: UsersReposItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserProfile, position: Int) {
            binding.profileLogin.text = item.login
            binding.profileId.text = item.id
            binding.profileCreatedDate.text = item.createdAt.formatToDateYMD()
            Glide.with(binding.profileAvatar.context).load(item.avatarUrl)
                .into(binding.profileAvatar)

            checkProfileToken(item.accessToken)
            checkProfileActive(item.isActive)

            binding.parentCv.setOnClickListener {
                selectProfileListener?.invoke(item)
            }
            binding.deleteProfile.setOnClickListener {
                deleteUserListener?.invoke(item, position)
            }
        }

        private fun checkProfileToken(token: String?) {
            binding.profileToken.apply {
                if (token.isNullOrEmpty()) {
                    text = this.context.getString(R.string.token_not_specified)
                    setTextColor(ContextCompat.getColor(context, R.color.error_default))
                } else {
                    text = this.context.getString(R.string.token_specified)
                    setTextColor(ContextCompat.getColor(context, R.color.element_active))
                }
            }
        }

        private fun checkProfileActive(isActive: Boolean) {
            binding.profileActiveIndicator.visibility = if (isActive) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        fun getForegroundView() = binding.parentCv
    }
}