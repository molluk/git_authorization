package ru.molluk.git_authorization.domain.usecase.home

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.repository.GitHubRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class LoadReposForCurrentUserUseCase @Inject constructor(
    private val application: Application,
    private val tokenManager: TokenManager,
    private val gitHubRepository: GitHubRepository
) {

    operator fun invoke(currentLogin: String, page: Int?): Flow<UiState<List<ReposResponse>>> =
        flow {
            emit(UiState.Loading)
            try {
                val repos = if (tokenManager.isActiveProfileAuthenticatable()) {
                    gitHubRepository.getCurrentAuthenticatedUserRepos(page)
                } else {
                    gitHubRepository.getUserReposByUsername(currentLogin, page)
                }
                val filteredRepos = repos.filter { repo -> repo.owner.login == currentLogin }
                emit(UiState.Success(filteredRepos))
            } catch (e: DomainException) {
                val errorMessage = parseDomainException(e)
                emit(UiState.Error(errorMessage, e))
            } catch (e: Exception) {
                emit(
                    UiState.Error(
                        application.getString(
                            R.string.repos_error_loading_user,
                            currentLogin,
                            e.message.toString()
                        ),
                        e
                    )
                )
            }
        }

}