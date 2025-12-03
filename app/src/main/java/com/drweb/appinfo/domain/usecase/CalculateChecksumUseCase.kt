package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow


class CalculateChecksumUseCase(
    private val repository: AppRepository
) {
    operator fun invoke(apkPath: String): Flow<String> {
        return repository.fetchChecksum(apkPath)
    }
}
