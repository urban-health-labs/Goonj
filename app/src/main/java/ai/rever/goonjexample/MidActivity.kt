package ai.rever.goonjexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mid.*
import ai.rever.goonj.audioplayer.download.DownloadUtil.addDownload
import ai.rever.goonj.audioplayer.download.DownloadUtil.getAllDownloads
import ai.rever.goonj.audioplayer.download.DownloadUtil.isMediaDownloaded
import ai.rever.goonj.audioplayer.models.SAMPLES
import android.view.View

class MidActivity : AppCompatActivity() {
    val TAG = "MID_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mid)

        button2.setOnClickListener {
            startActivity(Intent(this,AudioPlayerActivity::class.java))
        }

        buttonDownload.setOnClickListener {
//            addDownload(this, SAMPLES[0].mediaId,SAMPLES[0].url)
//            addDownload(this,SAMPLES[1].mediaId,SAMPLES[1].url)
//            addDownload(this,SAMPLES[2].mediaId,SAMPLES[2].url)
        }

        setupDownloads()
        getAllDownloads(this)

    }

    fun setupDownloads(){
        activity_mid_download1_btn.setOnClickListener {
            addDownload(this, SAMPLES[0].mediaId,SAMPLES[0].url)
        }

        activity_mid_download2_btn.setOnClickListener {
            addDownload(this, SAMPLES[1].mediaId,SAMPLES[1].url)
        }
        activity_mid_download3_btn.setOnClickListener {
            addDownload(this, SAMPLES[2].mediaId,SAMPLES[2].url)
        }

        if(isMediaDownloaded(this,SAMPLES[0].mediaId)){
            activity_mid_download1_btn.visibility = View.GONE
            activity_mid_done1_btn.visibility = View.VISIBLE
        }
        if(isMediaDownloaded(this,SAMPLES[1].mediaId)){
            activity_mid_download2_btn.visibility = View.GONE
            activity_mid_done2_btn.visibility = View.VISIBLE
        }
        if(isMediaDownloaded(this,SAMPLES[2].mediaId)){
            activity_mid_download3_btn.visibility = View.GONE
            activity_mid_done3_btn.visibility = View.VISIBLE
        }
    }
}
