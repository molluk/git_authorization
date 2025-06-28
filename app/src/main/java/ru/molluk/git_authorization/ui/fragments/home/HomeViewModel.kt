package ru.molluk.git_authorization.ui.fragments.home

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepositoryImpl
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val gitHubRepository: GitHubRepositoryImpl,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) : DefaultViewModel() {

    private val _userSaveProfile = MutableSharedFlow<UiState<UserProfile>>()
    val userProfile = _userSaveProfile.asSharedFlow()

    private val _user = MutableSharedFlow<UiState<UserResponse>>()
    val user = _user.asSharedFlow()

    private val _users = MutableSharedFlow<UiState<List<UserProfile>>>()
    val users = _users.asSharedFlow()

    private val _repos = MutableSharedFlow<UiState<List<ReposResponse>>>()
    val repos = _repos.asSharedFlow()

    fun saveUser(profile: UserProfile) {
        viewModelScope.launch {
            tokenManager.setActiveProfile(profile.id)
            profileRepository.deactivateAllProfiles()
            profileRepository.saveProfile(profile)
            profileRepository.setActiveProfile(profile)
        }
    }

    fun getUserActive() {
        viewModelScope.launch {
            _userSaveProfile.emit(UiState.Loading)

            try {
                val response = profileRepository.getActiveProfile()
                _userSaveProfile.emit(
                    if (response != null) {
                        UiState.Success(response)
                    } else {
                        UiState.Error(application.getString(R.string.user_error_save_loading))
                    }
                )
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _userSaveProfile.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {

            }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            _users.emit(UiState.Loading)

            try {
                val response = profileRepository.getAllProfiles()
                _users.emit(UiState.Success(response))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _users.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {

            }
        }
    }

    fun getUserResponse(userProfile: UserProfile, changeUser: Boolean = false) {
        viewModelScope.launch {
            _user.emit(UiState.Loading)
            try {
                val token = tokenManager.getDecryptedToken(userProfile.accessToken)
                val response = if (token.isNullOrEmpty()) {
                    gitHubRepository.getUserWithoutToken(userProfile.login)
                } else {
                    gitHubRepository.getUser(if (changeUser == true) token else "")
                }
                val newUser = UserProfile(
                    id = response.id.toString(),
                    login = response.login,
                    accessToken = userProfile.accessToken ?: "",
                    avatarUrl = userProfile.avatarUrl,
                    createdAt = userProfile.createdAt,
                    isActive = true
                )
                saveUser(newUser)
                _user.emit(UiState.Success(response))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _user.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                _user.emit(
                    UiState.Error(
                        application.getString(R.string.user_error_download, userProfile.login, e.message.toString()),
                        e
                    )
                )
            }
        }
    }

    fun getRepository(userProfile: UserProfile, page: Int? = null) {
        viewModelScope.launch {
            _repos.emit(UiState.Loading)
            try {
                val token = tokenManager.getDecryptedToken(userProfile.accessToken)
                val response = if (token.isNullOrEmpty()) {
                    gitHubRepository.getReposWithoutToken(userProfile.login, page)
                } else {
                    gitHubRepository.getRepos(page)
                }
                _repos.emit(UiState.Success(response))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _repos.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                _repos.emit(
                    UiState.Error(
                        application.getString(R.string.repos_error_loading_user, userProfile.login, e.message.toString()),
                        e
                    )
                )
            }
        }
    }

    fun deleteUser(profile: UserProfile) {
        viewModelScope.launch {
            try {
                val currentProfiles = profileRepository.getAllProfiles()
                val indexOfDeleted = currentProfiles.indexOfFirst { it.id == profile.id }
                profileRepository.deleteProfile(profile)
                val updatedProfiles = profileRepository.getAllProfiles()

                if (updatedProfiles.isNotEmpty()) {
                    var nextActiveProfile: UserProfile? = if (indexOfDeleted != -1 && updatedProfiles.isNotEmpty()) {
                        if (indexOfDeleted < updatedProfiles.size) {
                            updatedProfiles[indexOfDeleted]
                        } else {
                            updatedProfiles.last()
                        }
                    } else if (updatedProfiles.isNotEmpty()) {
                        updatedProfiles.first()
                    } else {
                        null
                    }

                    if (nextActiveProfile != null) {
                        profileRepository.setActiveProfile(nextActiveProfile)
                        val finalList = profileRepository.getAllProfiles()
                        _users.emit(UiState.Success(finalList))
                    } else {
                        _users.emit(UiState.Success(emptyList()))
                    }
                } else {
                    _users.emit(UiState.Success(emptyList()))
                }
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _users.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                _users.emit(
                    UiState.Error(
                        application.getString(
                            R.string.user_error_removed,
                            profile.login
                        ), e
                    )
                )
            }
        }
    }
}