package ru.molluk.git_authorization.domain.model

import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.model.dto.UserResponse

data class AuthResult(
    val user: UserResponse,
    val profile: UserProfile
)