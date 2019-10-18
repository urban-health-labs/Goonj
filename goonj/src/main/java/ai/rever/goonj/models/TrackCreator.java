package ai.rever.goonj.models;

import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TrackCreator {

    /**
     * Weird but only implementation to access CREATOR of Parcelize, until
     * https://youtrack.jetbrains.com/issue/KT-19853
     * resolved
     */

    @NonNull
    public static Parcelable.Creator<Track> getCreator() {
        return Track.CREATOR;
    }
}
