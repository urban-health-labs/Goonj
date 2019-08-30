package ai.rever.goonjexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mid.*
import ai.rever.goonj.audioplayer.download.DownloadUtil.addDownload
import ai.rever.goonj.audioplayer.interfaces.PlaybackManager
import ai.rever.goonj.audioplayer.models.Samples.SAMPLES

class MidActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mid)

        button2.setOnClickListener {
            startActivity(Intent(this,AudioPlayerActivity::class.java))
        }

        buttonDownload.setOnClickListener {
            addDownload(this,"0",SAMPLES[0].url)
            addDownload(this,"1",SAMPLES[1].url)
            addDownload(this,"2",SAMPLES[2].url)
        }
    }
}
