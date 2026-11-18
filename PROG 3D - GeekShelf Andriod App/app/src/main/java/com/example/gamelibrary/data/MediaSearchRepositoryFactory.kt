package com.example.gamelibrary.data

import com.example.gamelibrary.model.MediaType

object MediaSearchRepositoryFactory {
    // (The IIE, 2026)
    fun forType(mediaType: MediaType): MediaSearchRepository = when (mediaType) {
        MediaType.GAME -> GameSearchRepository()
        MediaType.COMIC_BOOK -> ComicSearchRepository()
        MediaType.MOVIE, MediaType.TV_SERIES -> MovieTvSearchRepository()
        MediaType.ANIME, MediaType.MANGA -> AnimeMangaSearchRepository()
    }
}
/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */