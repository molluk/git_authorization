package ru.molluk.git_authorization.domain.usecase.home

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepository
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class SwitchActiveUserProfileUseCase @Inject constructor(
    private val application: Application,
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository,
    private val gitHubRepository: GitHubRepository
) {
    operator fun invoke(userProfile: UserProfile): Flow<UiState<UserResponse>> = flow {
        emit(UiState.Loading)
        try {
            tokenManager.setActiveProfile(userProfile.id, !userProfile.accessToken.isNullOrEmpty())
            profileRepository.deactivateAllProfiles()
            profileRepository.setActiveProfile(userProfile)

            val user = if (tokenManager.isActiveProfileAuthenticatable()) {
                gitHubRepository.getCurrentAuthenticatedUser()
            } else {
                gitHubRepository.getUserByUsername(userProfile.login)
            }
            emit(UiState.Success(user))
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            emit(UiState.Error(errorMessage, e))
        } catch (e: Exception) {
            emit(
                UiState.Error(
                    application.getString(
                        R.string.user_error_download,
                        userProfile.login,
                        e.message.toString()
                    ),
                    e
                )
            )
        }
    }
}