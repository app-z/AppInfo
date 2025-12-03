package com.drweb.appinfo.domain.usecase

import com.drweb.appinfo.domain.repository.AppRepository


class CalculateChecksumUseCase(
    private val repository: AppRepository
) {
    suspend operator fun invoke(apkPath: String): Result<String> {
        return repository.calculateChecksum(apkPath)
    }
}
