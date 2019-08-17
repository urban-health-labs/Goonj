package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.util.AUDIO_CHANNEL_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity(){

    val TAG = "BASE_ACTIVITY"


    fun logEvent(event : String){
        Log.i(TAG,event)
    }

    fun makeShortToast(message : String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    fun makeLongToast(message : String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(AUDIO_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.setSound(null,null)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}