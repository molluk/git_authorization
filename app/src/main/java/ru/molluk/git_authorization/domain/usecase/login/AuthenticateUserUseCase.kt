package ru.molluk.git_authorization.domain.usecase.login

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.repository.GitHubRepository
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.domain.model.AuthResult
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class AuthenticateUserUseCase @Inject constructor(
    private val application: Application,
    private val gitHubRepository: GitHubRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) {
    operator fun invoke(login: String?, token: String?): Flow<UiState<AuthResult>> = flow {
        emit(UiState.Loading)
        try {
            val userResponse: UserResponse
            val encryptedToken = tokenManager.getEncryptedToken(token)

            if (!token.isNullOrEmpty()) {
                tokenManager.setTemporaryToken(token)
                userResponse = gitHubRepository.getUserDataForToken()
            } else if (!login.isNullOrEmpty()) {
                userResponse = gitHubRepository.getUserByUsername(login)
            } else {
                emit(UiState.Error(application.getString(R.string.login_filling_error), null))
                return@flow
            }

            val profile = UserProfile(
                id = userResponse.id.toString(),
                login = userResponse.login,
                avatarUrl = userResponse.avatarUrl,
                createdAt = userResponse.createdAt,
                accessToken = encryptedToken,
                isActive = true
            )

            profileRepository.deactivateAllProfiles()
            profileRepository.saveProfile(profile)
            profileRepository.setActiveProfile(profile)
            tokenManager.setActiveProfile(profile.id, !encryptedToken.isNullOrEmpty())
            tokenManager.setTemporaryToken(null)

            emit(UiState.Success(AuthResult(userResponse, profile)))
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            emit(UiState.Error(errorMessage, e))
        } catch (e: Exception) {
            emit(UiState.Error(e.message.toString(), e))
        }
    }
}