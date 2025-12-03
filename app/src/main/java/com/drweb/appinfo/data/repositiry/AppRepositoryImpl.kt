package com.drweb.appinfo.data.repositiry


import com.drweb.appinfo.core.common.ChecksumUtils
import com.drweb.appinfo.data.datasource.AppDataSource
import com.drweb.appinfo.domain.model.AppInfo
import com.drweb.appinfo.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppRepositoryImpl(
    private val dataSource: AppDataSource
) : AppRepository {

    override suspend fun getInstalledApps(): Result<List<AppInfo>> {
        return dataSource.getInstalledApps()
    }

    override suspend fun getAppDetail(packageName: String): Result<AppInfo> {
        return dataSource.getAppInfo(packageName)
    }

    override suspend fun calculateChecksum(apkPath: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(apkPath)
                if (file.exists()) {
                    val checksum = ChecksumUtils.calculateSHA256(file)
                    Result.success(checksum)
                } else {
                    Result.failure(Exception("APK файл не найден")) // TODO: Refact it later
                }
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }
}