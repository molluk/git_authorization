package ru.molluk.git_authorization.ui.fragments.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.databinding.AdapterReposItemBinding

class GitReposAdapter() :
    ListAdapter<ReposResponse, GitReposAdapter.GitReposViewHolder>(GitReposDiffCallback()) {

    private var isLoading = false
    private val visibleThreshold = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitReposViewHolder {
        val binding =
            AdapterReposItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GitReposViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GitReposViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

        if (!isLoading && position >= itemCount - visibleThreshold && itemCount >= visibleThreshold) {
            loadNextPage?.invoke()
        }
    }

    fun setData(newItems: List<ReposResponse>) {
        submitList(newItems)
    }

    fun addData(newItems: List<ReposResponse>) {
        if (newItems.isEmpty()) {
            isLoading = false
            return
        }
        val currentList = currentList.toMutableList()
        currentList.addAll(newItems)
        submitList(currentList)
        isLoading = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        submitList(emptyList())
        isLoading = false
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    var loadNextPage: (() -> Unit)? = null
    var goToRepoClickListener: ((ReposResponse) -> Unit)? = null
    var copyLinkClickListener: ((ReposResponse) -> Unit)? = null

    inner class GitReposViewHolder(
        private val binding: AdapterReposItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReposResponse) {
            binding.reposName.text = item.name
            binding.reposId.text = item.id.toString()
            binding.reposVisibility.text = item.visibility
            binding.reposLink.text = item.htmlUrl
            binding.reposLink.setOnClickListener {
                copyLinkClickListener?.invoke(item)
            }
            binding.reposButton.setOnClickListener {
                goToRepoClickListener?.invoke(item)
            }
        }
    }

    class GitReposDiffCallback : DiffUtil.ItemCallback<ReposResponse>() {
        override fun areItemsTheSame(oldItem: ReposResponse, newItem: ReposResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReposResponse, newItem: ReposResponse): Boolean {
            return oldItem == newItem
        }
    }

}