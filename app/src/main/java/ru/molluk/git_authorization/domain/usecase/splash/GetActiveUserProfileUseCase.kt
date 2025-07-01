package ru.molluk.git_authorization.domain.usecase.splash

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class GetActiveUserProfileUseCase @Inject constructor(
    private val application: Application,
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<UiState<UserProfile>> = flow {
        emit(UiState.Loading)
        try {
            val profile = profileRepository.getActiveProfile()
            if (profile != null) {
                emit(UiState.Success(profile))
            } else {
                emit(UiState.Error(application.getString(R.string.users_no_active)))
            }
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            emit(UiState.Error(errorMessage, e))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(UiState.Error(e.message.toString(), e))
        }
    }
}