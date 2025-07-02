package ru.molluk.git_authorization.domain.usecase.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.utils.UiState
import javax.inject.Inject

class GetActiveUserDetailsUseCase @Inject constructor(
    private val getActiveUserResponseUseCase: GetActiveUserResponseUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) {

    operator fun invoke(): Flow<UiState<Pair<UserResponse, UserProfile?>>> {
        // Получаем "холодные" потоки от других UseCase'ов или репозиториев
        val userResponseFlow: Flow<UiState<UserResponse>> = getActiveUserResponseUseCase()
        val allUsersFlow: Flow<UiState<List<UserProfile>>> = getAllUsersUseCase()
        
        return userResponseFlow.combine(allUsersFlow) { userState, usersState ->
            when {
                userState is UiState.Loading || (userState is UiState.Success && usersState is UiState.Loading) -> UiState.Loading
                userState is UiState.Success && usersState is UiState.Success -> {
                    val currentUserResponse = userState.data
                    val correspondingProfile =
                        usersState.data.firstOrNull { it.id == currentUserResponse.id.toString() && it.isActive }
                    UiState.Success(Pair(currentUserResponse, correspondingProfile))
                }
                userState is UiState.Error -> UiState.Error(userState.message, userState.throwable)
                usersState is UiState.Error -> UiState.Error(usersState.message, usersState.throwable)
                else -> UiState.Loading
            }
        }
    }
    
}