package com.example.gamelibrary.data

import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaType

interface MediaSearchRepository {
    suspend fun search(query: String, mediaType: MediaType): Result<List<MediaSearchResult>> // (The IIE, 2026)
}
/* Reference List:
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
* */
