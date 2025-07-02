package ru.molluk.git_authorization.ui.fragments.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.domain.usecase.login.AuthenticateUserUseCase
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase
) : DefaultViewModel() {

    private val _user = MutableSharedFlow<UiState<UserResponse>>()
    val user = _user.asSharedFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    /**
     * Пытается получить данные пользователя.
     * Если предоставлен `login` и `token` пуст, ищет публичный профиль.
     * Если предоставлен `token`, пытается аутентифицироваться с этим токеном,
     * получить данные пользователя, создать и сохранить профиль, сделав его активным.
     */
    fun attemptLoginOrFetchUser(login: String? = null, token: String? = null) {
        viewModelScope.launch {
            authenticateUserUseCase(login, token).collect { result ->
                when (result) {
                    is UiState.Loading -> _user.emit(UiState.Loading)
                    is UiState.Success -> {
                        _user.emit(UiState.Success(result.data.user))
                        _userProfile.value = result.data.profile
                    }
                    is UiState.Error -> _user.emit(result)
                }
            }
        }
    }
}