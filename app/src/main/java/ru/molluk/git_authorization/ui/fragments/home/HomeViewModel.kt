package ru.molluk.git_authorization.ui.fragments.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.domain.usecase.home.DeleteUserUseCase
import ru.molluk.git_authorization.domain.usecase.home.GetActiveUserResponseUseCase
import ru.molluk.git_authorization.domain.usecase.home.GetAllUsersUseCase
import ru.molluk.git_authorization.domain.usecase.home.LoadReposForCurrentUserUseCase
import ru.molluk.git_authorization.domain.usecase.home.NetworkSpeedUseCase
import ru.molluk.git_authorization.domain.usecase.home.SwitchActiveUserProfileUseCase
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val getActiveUserUseCase: GetActiveUserResponseUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val switchActiveUserProfileUseCase: SwitchActiveUserProfileUseCase,
    private val loadReposForCurrentUserUseCase: LoadReposForCurrentUserUseCase,
    networkSpeedUseCase: NetworkSpeedUseCase
) : DefaultViewModel() {

    private val _user = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)

    private val _users = MutableStateFlow<UiState<List<UserProfile>>>(UiState.Loading)
    val users = _users.asStateFlow()

    private val _repos = MutableSharedFlow<UiState<List<ReposResponse>>>()
    val repos = _repos.asSharedFlow()

    init {
        loadCurrentActiveUser()
        getAllUsers()
    }

    val speedKbps: StateFlow<String> = networkSpeedUseCase().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Н/д")

    val user: StateFlow<UiState<Pair<UserResponse, UserProfile?>>> =
        _user.combine(users) { userState, usersState ->
            when {
                userState is UiState.Loading || (userState is UiState.Success && usersState is UiState.Loading) -> UiState.Loading
                userState is UiState.Success && usersState is UiState.Success -> {
                    val currentUserResponse = userState.data
                    val correspondingProfile =
                        usersState.data.firstOrNull { it.id == currentUserResponse.id.toString() && it.isActive }
                    UiState.Success(Pair(currentUserResponse, correspondingProfile))
                }

                userState is UiState.Error -> UiState.Error(userState.message, userState.throwable)
                usersState is UiState.Error -> UiState.Error(
                    usersState.message,
                    usersState.throwable
                )

                else -> UiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /**
     * Получить список всех сохраненных пользователей
     */
    fun getAllUsers() {
        viewModelScope.launch {
            getAllUsersUseCase().collect { result ->
                _users.emit(result)
            }
        }
    }

    /**
     * Вызывается, когда пользователь выбирает другой профиль из списка.
     * @param userProfile Профиль, который нужно сделать активным.
     */
    fun switchActiveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            switchActiveUserProfileUseCase(userProfile).collect { result ->
                _user.emit(result)
                getAllUsers()
            }
        }
    }

    /**
     * Загружает данные для пользователя, который СЕЙЧАС активен в TokenManager.
     */
    fun loadCurrentActiveUser() {
        viewModelScope.launch {
            getActiveUserUseCase().collect { result ->
                _user.emit(result)
                getAllUsers()
            }
        }
    }

    /**
     * Загружает репозитории для ТЕКУЩЕГО АКТИВНОГО пользователя.
     */
    fun loadReposForCurrentUser(currentUserLogin: String, page: Int? = null) {
        viewModelScope.launch {
            loadReposForCurrentUserUseCase(currentUserLogin, page).collect { result ->
                _repos.emit(result)
            }
        }
    }

    /**
     * Удаляем юзера
     */
    fun deleteUser(profileToDelete: UserProfile) {
        viewModelScope.launch {
            val result = deleteUserUseCase(profileToDelete)
            _users.emit(result)

            if (result is UiState.Success && result.data.isNotEmpty()) {
                loadCurrentActiveUser()
            }
            if (result is UiState.Success && result.data.isEmpty()) {
                _user.emit(UiState.Error(application.getString(R.string.users_no_active), null))
            }
        }
    }
}