package com.propodcast.app.core.di

import com.propodcast.app.feature.details.PodcastDetailsViewModel
import com.propodcast.app.feature.downloads.DownloadsViewModel
import com.propodcast.app.feature.filters.FiltersViewModel
import com.propodcast.app.feature.home.HomeViewModel
import com.propodcast.app.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Screen ViewModels; Koin passes the navigation entry's SavedStateHandle where a constructor asks for one. */
val featureModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::FiltersViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::PodcastDetailsViewModel)
    viewModelOf(::DownloadsViewModel)
}
