package ru.molluk.git_authorization.ui.fragments.splash

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.domain.usecase.splash.FetchOctocatUseCase
import ru.molluk.git_authorization.domain.usecase.splash.GetActiveUserProfileUseCase
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val getActiveUserUseCase: GetActiveUserProfileUseCase,
    private val fetchOctocatUseCase: FetchOctocatUseCase
) : DefaultViewModel() {

    private val _octocat = MutableSharedFlow<UiState<String>>(1)
    val octocat = _octocat.asSharedFlow()

    private val _user = MutableSharedFlow<UiState<UserProfile>>()
    val user = _user.asSharedFlow()

    /**
     * Пробуем получить последнего активного пользователя
     */
    fun getUserActive() {
        viewModelScope.launch {
            getActiveUserUseCase.invoke().collect { result ->
                _user.emit(result)
            }
        }
    }

    fun fetchOctocat() {
        viewModelScope.launch {
            fetchOctocatUseCase().collect { _octocat.emit(it) }
        }
    }

}