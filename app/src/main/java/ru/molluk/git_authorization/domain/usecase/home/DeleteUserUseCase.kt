package ru.molluk.git_authorization.domain.usecase.home

import android.app.Application
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val application: Application,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(profile: UserProfile): UiState<List<UserProfile>> {
        return try {
            val isActive = profileRepository.getActiveProfile()?.id == profile.id
            profileRepository.deleteProfile(profile)

            val updated = profileRepository.getAllProfiles()
            if (updated.isEmpty()) {
                tokenManager.clearActiveToken()
                UiState.Success(emptyList())
            } else {
                if (isActive) {
                    profileRepository.setActiveProfile(updated.first())
                }
                UiState.Success(updated)
            }
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            UiState.Error(errorMessage, e)
        } catch (e: Exception) {
                UiState.Error(
                    application.getString(
                        R.string.user_error_removed,
                        profile.login
                    ), e
                )
        }
    }
}