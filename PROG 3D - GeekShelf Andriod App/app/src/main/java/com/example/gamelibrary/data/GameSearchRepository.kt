package com.example.gamelibrary.data

import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType

class GameSearchRepository(
    private val gameApiRepository: GameApiRepository = GameApiRepository()
) : MediaSearchRepository {
    // (The IIE, 2026)
    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaSearchResult>> =
        gameApiRepository.searchGames(query).map { games ->
            games.map {
                MediaSearchResult(
                    externalId = it.guid,
                    title = it.name,
                    imageUrl = it.image?.superUrl,
                    subtitle = it.releaseDate?.take(4),
                    mediaType = MediaType.GAME
                )
            }
        }
}

/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */