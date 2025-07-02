package ru.molluk.git_authorization.domain.usecase.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.repository.ProfileRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<UiState<List<UserProfile>>> = flow {
        emit(UiState.Loading)
        try {
            val profiles = profileRepository.getAllProfiles()
            emit(UiState.Success(profiles))
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            emit(UiState.Error(errorMessage, e))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "", e))
        }
    }
}