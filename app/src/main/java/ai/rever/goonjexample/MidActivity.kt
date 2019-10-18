package ai.rever.goonjexample

import ai.rever.goonj.Goonj
import ai.rever.goonj.download.GoonjDownloadManager.addDownload
import ai.rever.goonj.download.GoonjDownloadManager.isTrackDownloaded
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mid.*
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class MidActivity : AppCompatActivity() {
    val TAG = "MID_ACTIVITY"

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mid)

        button2.setOnClickListener {
            startActivity(Intent(this,AudioPlayerActivity::class.java))
        }

        setupDownloads()

        Goonj.downloadStateFlowable.subscribe {
            updateDownloadState()
        }.addTo(compositeDisposable)

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun setupDownloads(){
        activity_mid_download1_btn.setOnClickListener {
            addDownload(SAMPLES[0])
        }

        activity_mid_download2_btn.setOnClickListener {
            addDownload(SAMPLES[1])
        }
        activity_mid_download3_btn.setOnClickListener {
            addDownload(SAMPLES[2])
        }
        updateDownloadState()
    }

    private fun updateDownloadState(){
        if(isTrackDownloaded(SAMPLES[0].url)){
            activity_mid_download1_btn.visibility = View.GONE
            activity_mid_done1_btn.visibility = View.VISIBLE
        }
        if(isTrackDownloaded(SAMPLES[1].url)){
            activity_mid_download2_btn.visibility = View.GONE
            activity_mid_done2_btn.visibility = View.VISIBLE
        }
        if(isTrackDownloaded(SAMPLES[2].url)){
            activity_mid_download3_btn.visibility = View.GONE
            activity_mid_done3_btn.visibility = View.VISIBLE
        }
    }
}
