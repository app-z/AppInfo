package com.drweb.appinfo.core.di

import com.drweb.appinfo.data.datasource.AppDataSource
import com.drweb.appinfo.data.local.AppLocalDataSource
import com.drweb.appinfo.data.repositiry.AppIconRepository
import com.drweb.appinfo.data.repositiry.AppInstallRepositoryImpl
import com.drweb.appinfo.data.repositiry.AppInstallTracker
import com.drweb.appinfo.data.repositiry.AppInstallTrackerContentObserver
import com.drweb.appinfo.data.repositiry.AppRepositoryImpl
import com.drweb.appinfo.domain.repository.AppInstallRepository
import com.drweb.appinfo.domain.repository.AppRepository
import com.drweb.appinfo.domain.usecase.CalculateChecksumUseCase
import com.drweb.appinfo.domain.usecase.GetAppDetailUseCase
import com.drweb.appinfo.domain.usecase.GetAppIconUseCase
import com.drweb.appinfo.domain.usecase.GetInstalledAppsUseCase
import com.drweb.appinfo.domain.usecase.ObserveAppInstallUseCase
import com.drweb.appinfo.domain.usecase.ObserveContentAppInstall12UseCase
import com.drweb.appinfo.presentation.appdetail.AppDetailViewModel
import com.drweb.appinfo.presentation.applist.AppListViewModel
import com.drweb.appinfo.presentation.component.AppInstallHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Data Sources
    single<AppDataSource> { AppLocalDataSource(androidContext()) }

    // Repositories
    single<AppRepository> { AppRepositoryImpl(get()) }
    single<AppInstallRepository> { AppInstallRepositoryImpl(androidContext(), get()) }
    single { AppIconRepository(androidContext()) }
    single { AppInstallTracker(androidContext()) }

    single { AppInstallTrackerContentObserver(androidContext()) }
    single { AppInstallHelper(get(), get()) }

    // Use Cases
    factory { GetInstalledAppsUseCase(get()) }
    factory { GetAppDetailUseCase(get()) }
    factory { CalculateChecksumUseCase(get()) }
    factory { ObserveAppInstallUseCase(get()) }
    factory { GetAppIconUseCase(get()) }
    factory { ObserveContentAppInstall12UseCase(get()) }


    viewModelOf(::AppListViewModel)
    viewModelOf(::AppDetailViewModel)
}
