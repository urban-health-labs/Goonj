package ai.rever.goonjexample

import ai.rever.goonj.audioplayer.download.DownloadUtil.Companion.addDownload
import ai.rever.goonj.audioplayer.download.DownloadUtil.Companion.getAllDownloads
import ai.rever.goonj.audioplayer.download.DownloadUtil.Companion.getDownloadState
import ai.rever.goonj.audioplayer.download.DownloadUtil.Companion.isMediaDownloaded
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mid.*
import ai.rever.goonj.audioplayer.models.SAMPLES
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer

class MidActivity : AppCompatActivity() {
    val TAG = "MID_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mid)

        button2.setOnClickListener {
            startActivity(Intent(this,AudioPlayerActivity::class.java))
        }

        setupDownloads()
        getAllDownloads(application).observe(this, Observer {
            for(track in it){
                Log.d(TAG,"TrackID: ${track.title} State: ${getDownloadState(track.downloadedState)}")
                updateDownloadState()
            }
        })

    }

    fun setupDownloads(){
        activity_mid_download1_btn.setOnClickListener {
            addDownload(application, SAMPLES[0])
        }

        activity_mid_download2_btn.setOnClickListener {
            addDownload(application, SAMPLES[1])
        }
        activity_mid_download3_btn.setOnClickListener {
            addDownload(application, SAMPLES[2])
        }
         updateDownloadState()
    }

    fun updateDownloadState(){
        if(isMediaDownloaded(application,SAMPLES[0].mediaId)){
            activity_mid_download1_btn.visibility = View.GONE
            activity_mid_done1_btn.visibility = View.VISIBLE
        }
        if(isMediaDownloaded(application,SAMPLES[1].mediaId)){
            activity_mid_download2_btn.visibility = View.GONE
            activity_mid_done2_btn.visibility = View.VISIBLE
        }
        if(isMediaDownloaded(application,SAMPLES[2].mediaId)){
            activity_mid_download3_btn.visibility = View.GONE
            activity_mid_done3_btn.visibility = View.VISIBLE
        }
    }
}
