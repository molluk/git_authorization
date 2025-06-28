package ru.molluk.git_authorization.data.model.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class ReposResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("private") val isPrivate: Boolean,
    @SerializedName("owner") val owner: RepositoryOwner,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("description") val description: String?,
    @SerializedName("fork") val isFork: Boolean,
    @SerializedName("url") val url: String,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("updated_at") val updatedAt: Date,
    @SerializedName("pushed_at") val pushedAt: Date,
    @SerializedName("git_url") val gitUrl: String,
    @SerializedName("ssh_url") val sshUrl: String,
    @SerializedName("clone_url") val cloneUrl: String,
    @SerializedName("svn_url") val svnUrl: String,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("size") val size: Int,
    @SerializedName("stargazers_count") val stargazersCount: Int,
    @SerializedName("watchers_count") val watchersCount: Int,
    @SerializedName("language") val language: String?,
    @SerializedName("has_issues") val hasIssues: Boolean,
    @SerializedName("has_projects") val hasProjects: Boolean,
    @SerializedName("has_downloads") val hasDownloads: Boolean,
    @SerializedName("has_wiki") val hasWiki: Boolean,
    @SerializedName("has_pages") val hasPages: Boolean,
    @SerializedName("has_discussions") val hasDiscussions: Boolean,
    @SerializedName("forks_count") val forksCount: Int,
    @SerializedName("mirror_url") val mirrorUrl: String?,
    @SerializedName("archived") val isArchived: Boolean,
    @SerializedName("disabled") val isDisabled: Boolean,
    @SerializedName("open_issues_count") val openIssuesCount: Int,
    @SerializedName("license") val license: License?,
    @SerializedName("allow_forking") val allowForking: Boolean,
    @SerializedName("is_template") val isTemplate: Boolean,
    @SerializedName("web_commit_signoff_required") val webCommitSignoffRequired: Boolean,
    @SerializedName("topics") val topics: List<String>,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("forks") val forks: Int,
    @SerializedName("open_issues") val openIssues: Int,
    @SerializedName("watchers") val watchers: Int,
    @SerializedName("default_branch") val defaultBranch: String,
    @SerializedName("permissions") val permissions: RepositoryPermissions?
)

data class RepositoryOwner(
    @SerializedName("login") val login: String,
    @SerializedName("id") val id: Long,
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("gravatar_id") val gravatarId: String,
    @SerializedName("url") val url: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("followers_url") val followersUrl: String,
    @SerializedName("following_url") val followingUrl: String,
    @SerializedName("gists_url") val gistsUrl: String,
    @SerializedName("starred_url") val starredUrl: String,
    @SerializedName("subscriptions_url") val subscriptionsUrl: String,
    @SerializedName("organizations_url") val organizationsUrl: String,
    @SerializedName("repos_url") val reposUrl: String,
    @SerializedName("events_url") val eventsUrl: String,
    @SerializedName("received_events_url") val receivedEventsUrl: String,
    @SerializedName("type") val type: String,
    @SerializedName("user_view_type") val userViewType: String,
    @SerializedName("site_admin") val siteAdmin: Boolean
)

data class License(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("spdx_id") val spdxId: String,
    @SerializedName("url") val url: String?,
    @SerializedName("node_id") val nodeId: String
)

data class RepositoryPermissions(
    @SerializedName("admin") val admin: Boolean,
    @SerializedName("maintain") val maintain: Boolean,
    @SerializedName("push") val push: Boolean,
    @SerializedName("triage") val triage: Boolean,
    @SerializedName("pull") val pull: Boolean
)