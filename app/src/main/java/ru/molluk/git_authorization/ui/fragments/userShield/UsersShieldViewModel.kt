package ru.molluk.git_authorization.ui.fragments.userShield

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DefaultViewModel
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

@HiltViewModel
class UsersShieldViewModel @Inject constructor(
    private val application: Application,
    val profileRepository: ProfileRepository,
) : DefaultViewModel() {

    private val _usersSaveProfiles = MutableSharedFlow<UiState<List<UserProfile>>>()
    val usersProfiles = _usersSaveProfiles.asSharedFlow()

    fun getAllUsers() {
        viewModelScope.launch {
            _usersSaveProfiles.emit(UiState.Loading)
            try {
                val response = profileRepository.getAllProfiles()
                _usersSaveProfiles.emit(UiState.Success(response))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                _usersSaveProfiles.emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                _usersSaveProfiles.emit(
                    UiState.Error(
                        application.getString(R.string.user_error_download),
                        e
                    )
                )
            }
        }
    }
}