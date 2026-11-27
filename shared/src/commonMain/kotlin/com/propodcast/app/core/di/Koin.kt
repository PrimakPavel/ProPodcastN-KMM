package com.propodcast.app.core.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Database, DataStores and other things that need the platform (Context on Android, Documents on iOS). */
expect val platformModule: Module

/** All app modules; tests swap [platformModule] for in-memory storage. */
fun appModules(platform: Module = platformModule): List<Module> = listOf(platform, networkModule, dataModule, featureModule)

/** Starts Koin once per process: from `Application.onCreate` on Android, from the SwiftUI app on iOS. */
fun initKoin(config: KoinAppDeclaration = {}): KoinApplication = startKoin {
    config()
    modules(appModules())
}
