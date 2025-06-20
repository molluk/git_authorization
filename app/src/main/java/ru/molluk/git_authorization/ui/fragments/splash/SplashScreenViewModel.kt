package ru.molluk.git_authorization.ui.fragments.splash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.data.repository.OctocatRepositoryImpl
import ru.molluk.git_authorization.utils.NetworkMonitor
import ru.molluk.git_authorization.utils.UiState
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val octocatRepository: OctocatRepositoryImpl,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(networkMonitor.hasInternetConnection())
    val isNetworkAvailable = _isNetworkAvailable.asSharedFlow()

    private val _octocat = MutableSharedFlow<UiState<String>>(1)
    val octocat = _octocat.asSharedFlow()

    init {
        viewModelScope.launch {
            _isNetworkAvailable.emit(networkMonitor.hasInternetConnection())

            networkMonitor.observeNetworkChanges()
                .collect { isAvailable ->
                    _isNetworkAvailable.value = isAvailable
                }
        }
    }

    fun fetchOctocat() {
        viewModelScope.launch {
            if (!networkMonitor.hasInternetConnection()) {
                _octocat.emit(UiState.Error("Нет подключения к интернету"))
                return@launch
            }

            _octocat.emit(UiState.Loading)
            try {
                val response = octocatRepository.getOctocat()
                _octocat.emit(UiState.Success(response))
            } catch (e: Exception) {
                val errorMessage = if (e is UnknownHostException || e is IOException) {
                    "Ошибка сети. Не удалось загрузить данные"
                } else {
                    "Не удалось загрузить данные. Попробуйте позже"
                }
                _octocat.emit(UiState.Error(errorMessage, e))
            }
        }
    }

}