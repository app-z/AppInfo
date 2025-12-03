package com.drweb.appinfo.core.di

import com.drweb.appinfo.data.datasource.AppDataSource
import com.drweb.appinfo.data.local.AppLocalDataSource
import com.drweb.appinfo.data.repositiry.AppRepositoryImpl
import com.drweb.appinfo.domain.repository.AppRepository
import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
import com.drweb.appinfo.presentation.appdetail.AppDetailViewModel
import com.drweb.appinfo.presentation.applist.AppListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Data Sources
    single<AppDataSource> { AppLocalDataSource(androidContext()) }

    // Repositories
    single<AppRepository> { AppRepositoryImpl(get()) }

    // Use Cases
    factory { GetInstalledAppsUseCase(get()) }
    factory { GetAppDetailUseCase(get()) }
    factory { CalculateChecksumUseCase(get()) }

    viewModelOf(::AppListViewModel)
    viewModelOf(::AppDetailViewModel)

}
