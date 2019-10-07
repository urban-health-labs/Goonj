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
Goonj.initialize(this)
````
where __pendingIntent__ is the Intent which defines what is activity that on click on Notification will take to. Example: 
```
val pendingIntent = Intent(applicationContext, AudioPlayerActivity::class.java)
Goonj.setPendingIntentForNotification(pendingIntent)
```

### Unregister Service
In your onDestroy(), add the following lines:
```
Goonj.unregister()
```

### Implement GoonjPlayer Interface
To use Audio player actions, implement __GoonjPlayer__ interface in your Activity/Fragment.
```
class AudioPlayerActivity : AppCompatActivity(), GoonjPlayer
```

### Methods
| Method | Description |
| -------| ----------- |
|__startNewSession()__|Starts a new Audio Session. Clears existing playlist.|
|__addTrack(track : Track, index: Int? = null)__| Add an audio to playlist.|
|__resume()__ | Resume the player|
|__pause()__| Pauses the player|
|__seekTo(positionMs: Long)__|Seek by a certain length. +ve values seek forward, -ve seek backward.|
|__setAutoplay(autoplay : Boolean)__ | Lets you enable Autoplay with auto-fetch tracks. |
|__session(context: Context) : List<Samples.Track>__|Returns current playlist|
|__removeTrack(index : Int)__|Removes track from current Index in the playlist|
|__moveTrack(currentIndex : Int, finalIndex : Int)__|Moves a prticular track from one place in the playlist to another.|
|__skipToNext()__|Skip to next track.|
|__skipToPrevious()__|Skip to previous track|
|__playerStateObservable__|Observable of (GoonjPlayerState) that tells if the Player state. Helps in updating Play/Pause Icon/Button in UI.|
|__currentPlayingTrack__|Returns a Observable of (Track) that contains details of the Current Track that is playing.|

### Cast
To support __ChromeCast__, add ``` CastButtonFactory.setUpMediaRouteButton(this, yourMediaRouterButtonInXML) ``` to your Activity/Fragment and add ```yourMediaRouterButtonInXML``` in your XML Layout.
** Note **: Cast doesn't supports Autoplay.
