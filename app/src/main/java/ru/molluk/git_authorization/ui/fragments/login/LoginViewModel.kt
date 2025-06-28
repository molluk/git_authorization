package ru.molluk.git_authorization.ui.fragments.login

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepositoryImpl
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val gitHubRepository: GitHubRepositoryImpl,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) : DefaultViewModel() {

    private val _user = MutableSharedFlow<UiState<UserResponse>>()
    val user = _user.asSharedFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    fun saveUser(profile: UserProfile) {
        viewModelScope.launch {
            tokenManager.setActiveProfile(profile.id)
            profileRepository.deactivateAllProfiles()
            profileRepository.saveProfile(profile)
            profileRepository.setActiveProfile(profile)
            _userProfile.value = profile
        }
    }

    fun getUserResponse(login: String? = null, token: String? = null) {
        viewModelScope.launch {
            _user.emit(UiState.Loading)
            try {

                val response = if (token.isNullOrEmpty() && !login.isNullOrEmpty()) {
                    gitHubRepository.getUserWithoutToken(login)
                } else if (!token.isNullOrEmpty()) {
                    gitHubRepository.getUser(token)
                } else {
                    throw Exception(application.getString(R.string.login_filling_error))
                }
                val newUser = UserProfile(
                    id = response.id.toString(),
                    login = response.login,
                    avatarUrl = response.avatarUrl,
                    createdAt = response.createdAt,
                    accessToken = tokenManager.getEncryptedToken(token),
                    isActive = true
                )
                saveUser(newUser)
                _user.emit(UiState.Success(response))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _user.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                _user.emit(UiState.Error(e.message.toString(), e))
            }
        }
    }
}