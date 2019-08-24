package ai.rever.goonj.audioplayer.interfaces

import ai.rever.goonj.audioplayer.models.Samples

class Autoplay {

//    fun fetchMoreTracks() : List<Samples.Track>{
//
//    }
}

interface OnTrackFetchListener {
    fun onTracksFetched(trackList : List<Samples.Track>)
    fun fetchMoreTracks() : List<Samples.Track>
}
