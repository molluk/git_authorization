package ru.molluk.git_authorization.ui.fragments.splash

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.repository.OctocatRepositoryImpl
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val octocatRepository: OctocatRepositoryImpl,
    private val profileRepository: ProfileRepository
) : DefaultViewModel() {

    private val _octocat = MutableSharedFlow<UiState<String>>(1)
    val octocat = _octocat.asSharedFlow()

    private val _user = MutableSharedFlow<UiState<UserProfile>>()
    val user = _user.asSharedFlow()

    fun getUserActive() {
        viewModelScope.launch {
            _user.emit(UiState.Loading)
            try {
                val response = profileRepository.getActiveProfile()
                _user.emit(
                    if (response != null) {
                        UiState.Success(response)
                    } else {
                        UiState.Error(application.getString(R.string.users_no_active))
                    }
                )
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _user.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchOctocat() {
        viewModelScope.launch {
            _octocat.emit(UiState.Loading)
            try {
                val response = octocatRepository.getOctocat()
                _octocat.emit(UiState.Success(response))
            }
            catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _octocat.emit(UiState.Error(errorMessage, e))
            }
            catch (e: Exception) {
                _octocat.emit(UiState.Error(DomainException.Unknown(cause = e).message ?: application.getString(R.string.error_unknown), e))
            }
        }
    }

}