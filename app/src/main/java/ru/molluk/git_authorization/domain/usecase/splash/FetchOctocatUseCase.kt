package ru.molluk.git_authorization.domain.usecase.splash

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.repository.OctocatRepository
import ru.molluk.git_authorization.utils.DomainException
import ru.molluk.git_authorization.utils.UiState
import ru.molluk.git_authorization.utils.parseDomainException
import javax.inject.Inject

class FetchOctocatUseCase @Inject constructor(
    private val application: Application,
    private val octocatRepository: OctocatRepository
) {
    operator fun invoke(): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val response = octocatRepository.getOctocat()
            emit(UiState.Success(response))
        } catch (e: DomainException) {
            val errorMessage = parseDomainException(e)
            emit(UiState.Error(errorMessage, e))
        }
        catch (e: Exception) {
            emit(UiState.Error(DomainException.Unknown(cause = e).message ?: application.getString(R.string.error_unknown), e))
        }
    }
}