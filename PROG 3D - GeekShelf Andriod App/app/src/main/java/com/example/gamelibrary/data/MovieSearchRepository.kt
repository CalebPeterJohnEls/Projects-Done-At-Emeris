package com.example.gamelibrary.data

import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType

class MovieSearchRepository : MediaSearchRepository {
    // (The IIE, 2026)
    override suspend fun search(
        query: String,
        mediaType: MediaType
    ): Result<List<MediaSearchResult>> =
        Result.failure(Exception("Movie search not yet wired up"))
}
/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */