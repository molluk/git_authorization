package ru.molluk.git_authorization.domain.usecase.home

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepository
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class GetActiveUserResponseUseCase @Inject constructor(
    private val application: Application,
    private val profileRepository: ProfileRepository,
    private val gitHubRepository: GitHubRepository,
    private val tokenManager: TokenManager
) {
    operator fun invoke(): Flow<UiState<UserResponse>> = flow {
        emit(UiState.Loading)
        try {
            val activeProfile = profileRepository.getActiveProfile()
                ?: return@flow emit(UiState.Error(application.getString(R.string.user_error_download, "", ""), null))

            val user = if (tokenManager.isActiveProfileAuthenticatable()) {
                gitHubRepository.getCurrentAuthenticatedUser()
            } else {
                gitHubRepository.getUserByUsername(activeProfile.login)
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
                        "",
                        e.message.toString()
                    ),
                    e
                )
            )
        }
    }
}