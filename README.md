[![](https://jitpack.io/v/rever-ai/goonj.svg)](https://jitpack.io/#rever-ai/goonj)

# Goonj 
### An all purpose music player library

## How to add Goonj to your Android application
### Step 1:
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
} 
````

### Step 2: 
Add the dependency
```
dependencies {
	implementation 'com.github.rever-ai:goonj:v0.3'
	implementation 'com.google.android.exoplayer:exoplayer-core:2.10.3'
        implementation 'com.google.android.exoplayer:exoplayer-ui:2.10.3'
        implementation 'com.google.android.exoplayer:extension-mediasession:2.10.0'
}
```

## Usage
### Register Service
In your onCreate(), add the following lines:
```
PlaybackManager.getInstance(this).register(pendingIntent)
````
where __pendingIntent__ is the Intent which defines what is activity that on click on Notification will take to. Example: 
```
pendingIntent = Intent(applicationContext, AudioPlayerActivity::class.java)
```

### Unregister Service
In your onDestroy(), add the following lines:
```
PlaybackManager.getInstance(this).unregister()
```

### Implement GoonjPlayer Interface
To use Audio player actions, implement __GoonjPlayer__ interface in your Activity/Fragment.
```
class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer
```

### Methods
| Method | Description |
| -------| ----------- |
|__startNewSession(context:Context)__|Starts a new Audio Session. Clears existing playlist.|
|__addAudioToPlaylist(context: Context, audioTrack: Samples.Track, index: Int ?= -1)__| Add an audio to playlist.|
|__play(context: Context)__ | Resume the player|
|__pause(context: Context)__| Pauses the player|
|__seek(context: Context, positionMs: Long?)__|Seek by a certain length. +ve values seek forward, -ve seek backward.|
|__setAutoplay(context: Context, autoplay : Boolean, indexFromLast: Int, autoLoadListener: AutoLoadListener)__ | Lets you enable Autoplay with auto-fetch tracks. |
|__session(context: Context) : List<Samples.Track>__|Returns current playlist|
|__removeTrack(context: Context,index : Int)__|Removes track from current Index in the playlist|
|__moveTrack(context: Context, currentIndex : Int, finalIndex : Int)__|Moves a prticular track from one place in the playlist to another.|
|__skipToNext(context: Context)__| Skip to next track.|
|__skipToPrevious(context: Context)__|Skip to previous track|
|__isPlayingLiveData(context: Context)__|Returns a LiveData that tells if the Player is paused or playing. Helps in updating Play/Pause Icon/Button in UI.|
|__currentPlayingTrack(context: Context)__|Returns a LiveData that contains details of the Current Track that is playing.|
|__analyticsObservable__ : Observable<AnalyticsModel>| Add an Observer onto this to capture Analytics from the Player events.|

### Cast
To support __ChromeCast__, add ``` CastButtonFactory.setUpMediaRouteButton(this, yourMediaRouterButtonInXML) ``` to your Activity/Fragment and add ```yourMediaRouterButtonInXML``` in your XML Layout.
** Note **: Cast doesn't supports Autoplay.
