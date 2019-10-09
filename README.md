[![](https://jitpack.io/v/rever-ai/goonj.svg)](https://jitpack.io/#rever-ai/goonj)

# Goonj 
### An all purpose music player library

## Goonj integrate to your android project in under a minute
### Step 1:
Add following in build.gradle (Project level):
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
} 
````

### Step 2: 
Add following in build.gradle (App level):
```
dependencies {
	implementation 'com.github.rever-ai:goonj:v0.3.+'
}
```

## Usage
### Register Goonj
To register Goonj, add following line:
```
Goonj.register<AudioPlayerActivity>(context)
````
where __AudioPlayerActivity__ is activity that will open on clicking Notification.

### Unregister Goonj
To unregister Goonj, add following line:
```
Goonj.unregister()
```

To use player actions, implement __GoonjPlayer__ interface or use __Goonj__ singleton instance

### GoonjPlayer Interface could be implemented in following way
```
class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer

    ....
    someMethod()
    val property = someProperty
    ....

```

### Goonj Singleton could be used in following way
```
Goonj.someMethod() or Goonj.someProperty
```

### Goonj(Singleton) or GoonjPlayer(Interface)
| Method/Property | Description |
| -------| ----------- |
|__startNewSession()__|Starts a new Audio Session. Clears existing playlist.|
|__resume()__| Resume the player.|
|__pause()__| Pauses the player.|
|__finishTrack()__|Manual finish track.|
|__seekTo(positionMs: Long)__|Seek by a certain position, could be used with trackPosition to move forward or backward.|
|__addTrack(track : Track, index: Int? = null)__| Add an audio to playlist.|
|__removeTrack(index : Int)__|Removes track from current Index in the playlist.|
|__moveTrack(currentIndex : Int, finalIndex : Int)__|Moves a particular track from one place in the playlist to another.|
|__skipToNext()__|Skip to next track.|
|__skipToPrevious()__|Skip to previous track.|
|__customiseNotification(useNavigationAction: Boolean, usePlayPauseAction: Boolean, fastForwardIncrementMs: Long, rewindIncrementMs: Long, smallIcon: Int)__|Customise notification which appear while playing.|
|__changeActivityIntentForNotification(intent: Intent)__|Set activity on notification click.|
|__removeNotification()__|Remove notification.|
|__register<ActivityType>(context: Context)__|Simplest way to register Goonj.|
|__register(context: Context, activityIntent: Intent)__|Extras with intent could be sent.|
|__register<S: GoonjService>(context: Context, activityIntent: Intent, audioServiceClass: Class<S>)__|Advanced registration for custom AudioService implementation.|
|__unregister()__|unregister Goonj|
|__autoplay__|Enable autoplay with auto-fetch tracks, could be used get current state of autoplay.|
|__trackList__|Get current playlist.|
|__playerState__|Get player state (GoonjPlayerState).|
|__currentTrack__|Get current track (Track).|
|__trackPosition__|Get current track position in milli-seconds (Long).|
|__trackProgress__|Get current track progress between 0 to 1 (Double).|
|__playerStateFlowable__|Flowable of (GoonjPlayerState) that tells if the Player state. Helps in updating Play/Pause Icon/Button in UI.|
|__currentTrackFlowable__|Flowable of (Track) that contains details of the Current Track that is playing.|
|__autoplayFlowable__|Flowable of (Track) that contains details of the Current Track that is playing.|
|__trackCompletionObservable__|Observable of (Track) that get completed after subscription.|


### Cast
Feature under development
