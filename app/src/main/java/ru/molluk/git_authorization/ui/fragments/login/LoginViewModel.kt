package ru.molluk.git_authorization.ui.fragments.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepositoryImpl
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val gitHubRepository: GitHubRepositoryImpl,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _user = MutableSharedFlow<UiState<UserResponse>>()
    val user = _user.asSharedFlow()

    fun saveUser(profile: UserProfile) {
        viewModelScope.launch {
            tokenManager.saveProfile(profile)
            profileRepository.saveProfile(profile)
            profileRepository.setActiveProfile(profile)
        }
    }

    fun getNewUser(token: String) {
        viewModelScope.launch {
            _user.emit(UiState.Loading)
            try {
                val response = gitHubRepository.getUser(token)
                val newUser = UserProfile(id = response.id.toString(), name = response.name, accessToken = token, isActive = true)
                saveUser(newUser)
                _user.emit(UiState.Success(response))
            } catch (e: Exception) {
                _user.emit(UiState.Error(e.message.toString(), e))
            }
        }
    }

    fun getUserWithoutToken(name: String) {
        viewModelScope.launch {
            _user.emit(UiState.Loading)
            try {
                val response = gitHubRepository.getUserWithoutToken(name)
                val newUser = UserProfile(id = response.id.toString(), name = response.name, accessToken = "", isActive = true)
                saveUser(newUser)
                _user.emit(UiState.Success(response))
            } catch (e: Exception) {
                _user.emit(UiState.Error(e.message.toString(), e))
            }
        }
    }
}