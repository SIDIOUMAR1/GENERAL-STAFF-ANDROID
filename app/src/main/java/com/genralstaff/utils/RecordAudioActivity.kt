package com.genralstaff.utils

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.genralstaff.R
import java.io.IOException
import java.util.*

class RecordAudioActivity : AppCompatActivity() {


    var AudioSavePathInDevice : String? = null
    var countDownTimer : CountDownTimer? = null
    var mediaRecorder : MediaRecorder? = null
    var tvTime : TextView? = null
    var recordAudio : ImageView? = null
    var btnRestart : ImageView? = null
    var btnPlay : ImageView? = null
    var ivCheck : ImageView? = null
    var ivClear : ImageView? = null
    var gifImage : ImageView? = null
    var random : Random? = null
    var startRecording = false
    var RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    private var milisecont : Long = 0
    private var timerStart = false
    var mp : MediaPlayer? = null
    var playStatus = "0"
    var pause = "0"
    var ispause = false

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)
        btnPlay = findViewById(R.id.play)
        recordAudio = findViewById(R.id.recordAudio)
        gifImage = findViewById(R.id.gif_image_view)
        ivCheck = findViewById(R.id.ivCheck)
        ivClear = findViewById(R.id.ivClear)
        btnRestart = findViewById(R.id.restart)
        tvTime = findViewById<View>(R.id.timer) as TextView
        random = Random()

        Glide.with(this)
            .load(R.drawable.audio_player_gif_image)
            .into(gifImage!!)

        recordAudio!!.setOnClickListener {
            if (timerStart == true) {
                if (mp != null) {
                    mp!!.stop()
                }
            }
            startResording()
        }
        //
//
        btnPlay!!.setOnClickListener {
            if (playStatus == "0") {
                playStatus = "1"
                btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
                mp = MediaPlayer()
                try {
                    mp!!.setDataSource(AudioSavePathInDevice) //Write your location here
                    mp!!.prepare()
                    // mp.start();
                    playSound(mp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (pause == "0") {
                    mp!!.pause()
                    pause = "1"
                    btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
                } else {
                    playSound(mp)
                    pause = "0"
                    btnPlay!!.setBackgroundResource(R.drawable.ic_pause)
                }
            }
        }
        btnRestart!!.setOnClickListener(View.OnClickListener(fun(it : View) {
            if (timerStart == true) {
                if (mp != null) {
                    mp!!.stop()
                }
            }
            playStatus = "0"
            pause = "0"
            AudioSavePathInDevice = ""
            tvTime!!.text = "00"
            startRecording = false
            startResording()
        }))
        ivClear!!.setOnClickListener {
            if (mp != null) {
                mp!!.stop()
            }
            finish()
        }
        ivCheck!!.setOnClickListener {
            if (mp != null) {
                mp!!.stop()
            }
            setResult(RESULT_OK, intent.putExtra("data", AudioSavePathInDevice))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mp != null) {
            mp!!.pause()
            pause = "1"
            btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mp != null) {
            mp!!.stop()
        }

        if(countDownTimer!=null){
            countDownTimer!!.cancel()
            countDownTimer!!.onFinish()
        }


        finish()
    }

    private fun playSound(player : MediaPlayer?) {
        if (player != null) {
            if (timerStart == false) {
                player.start()
                timerStart = true
                val timer = Timer()
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            if (player.isPlaying) {
                                tvTime!!.post {
                                    tvTime!!.text = ( player.currentPosition / 1000).toString()
                                }
                            } else {
                                btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
                                timer.cancel()
                                timer.purge()
                                timerStart = false
                                //                                    if (mp != null) {
//                                        mp.stop();
//                                    }
                                //   pause="0";
                            }
                        }
                    }
                }, 0, 1000)
            }
        }
    }

    private fun startResording() {
        if (!startRecording) {
            if (checkPermission()) {

                var cw : ContextWrapper = ContextWrapper(getApplicationContext())
                var directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AudioSavePathInDevice = directory.absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.mp3"
                }
                else{
                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.mp3"
                }

                // AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.3gp"
                //   AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.mp3"

                mediaRecorderReady()
                try {
                    recordAudio!!.background = AppCompatResources.getDrawable(this,R.drawable.ic_pause)
                    //     recordAudio.setBackground(getDrawable(R.drawable.ic_stop_audio));
                    startRecording = true
                    playStatus = "0"
                    pause = "0"
                    mediaRecorder!!.prepare()
                    mediaRecorder!!.start()
                    ispause = true
                    btnRestart!!.visibility = View.GONE
                    btnPlay!!.visibility = View.GONE
                    ivCheck!!.visibility = View.GONE
                    ivClear!!.visibility = View.GONE
                    //   btnDone.setVisibility(View.GONE);
                    countDownTimer = object : CountDownTimer(60900, 1000) {
                        override fun onTick(millisUntilFinished : Long) {
                            milisecont = millisUntilFinished / 1000
                            tvTime!!.text = ( millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            startRecording = false
                            tvTime!!.text = milisecont.toString()
                            if(mediaRecorder!=null){
                                mediaRecorder!!.stop()

                            }
                            //   btnDone.setVisibility(View.VISIBLE);
                            recordAudio!!.background = AppCompatResources.getDrawable(this@RecordAudioActivity,R.drawable.aar_ic_rec)
                            btnRestart!!.visibility = View.VISIBLE
                            btnPlay!!.visibility = View.VISIBLE
                            ivCheck!!.visibility = View.VISIBLE
                            ivClear!!.visibility = View.VISIBLE
                            //  recordAudio.setBackground(getDrawable(R.drawable.ic_pause));
                        }
                    }.start()
                } catch (e : IllegalStateException) {
                    e.printStackTrace()
                } catch (e : IOException) {
                    e.printStackTrace()
                }
            } else {
                requestPermission()
            }
        } else {
            recordAudio!!.background = getDrawable(R.drawable.aar_ic_rec)
            countDownTimer!!.cancel()
            countDownTimer!!.onFinish()
            startRecording = false
        }
    }



    private fun mediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            // Set higher quality parameters
            mediaRecorder!!.setAudioEncodingBitRate(128000) // 128 kbps
            mediaRecorder!!.setAudioSamplingRate(44100)     // CD quality

            mediaRecorder!!.setOutputFile(AudioSavePathInDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing recorder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun CreateRandomAudioFileName(string : Int) : String {
        val stringBuilder = StringBuilder(string)
        var i = 0
        while (i < string) {
            stringBuilder.append(RandomAudioFileName[random!!.nextInt(RandomAudioFileName.length)])
            i++
        }
        return stringBuilder.toString()
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
        {
            ActivityCompat.requestPermissions(
                this@RecordAudioActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RequestPermissionCode
            )

        }
        else {

            ActivityCompat.requestPermissions(
                this@RecordAudioActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
                RequestPermissionCode
            )
        }

    }

    override fun onResume() {
        super.onResume()
//        try {
//            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
//        } catch (e : Exception) {
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode : Int,
        permissions : Array<String>,
        grantResults : IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {


            RequestPermissionCode -> if (grantResults.size > 0) {

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
                {
                    val RecordPermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED
                    if (RecordPermission) {
                        // Toast.makeText(this@RecordAudioActivity, "Permission Granted", Toast.LENGTH_LONG).show()

                        //   startRecording

                        if (playStatus == "0") {
                            playStatus = "1"
                            btnPlay!!.setBackgroundResource(R.drawable.ic_pause)
                            mp = MediaPlayer()
                            try {
                                mp!!.setDataSource(AudioSavePathInDevice) //Write your location here
                                mp!!.prepare()
                                // mp.start();
                                playSound(mp)
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            if (pause == "0") {
                                mp!!.pause()
                                pause = "1"
                                btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
                            } else {
                                playSound(mp)
                                pause = "0"
                                btnPlay!!.setBackgroundResource(R.drawable.ic_pause)
                            }
                        }

                    }
                    else {
                        Toast.makeText(this@RecordAudioActivity, "Permission Denied", Toast.LENGTH_LONG)
                            .show()
                    }

                }else{

                    val storagePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED
                    val RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED
                    if (storagePermission && RecordPermission) {
                        // Toast.makeText(this@RecordAudioActivity, "Permission Granted", Toast.LENGTH_LONG).show()

                        //   startRecording

                        if (playStatus == "0") {
                            playStatus = "1"
                            btnPlay!!.setBackgroundResource(R.drawable.ic_pause)
                            mp = MediaPlayer()
                            try {
                                mp!!.setDataSource(AudioSavePathInDevice) //Write your location here
                                mp!!.prepare()
                                // mp.start();
                                playSound(mp)
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            if (pause == "0") {
                                mp!!.pause()
                                pause = "1"
                                btnPlay!!.setBackgroundResource(R.drawable.aar_ic_play)
                            } else {
                                playSound(mp)
                                pause = "0"
                                btnPlay!!.setBackgroundResource(R.drawable.ic_pause)
                            }
                        }

                    }
                    else {
                        Toast.makeText(this@RecordAudioActivity, "Permission Denied", Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }
        }
    }

    fun checkPermission() : Boolean {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)
        {
            val result1 = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
            return result1 == PackageManager.PERMISSION_GRANTED
        }

        else {
            val result = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val result1 = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
            return result == PackageManager.PERMISSION_GRANTED &&
                    result1 == PackageManager.PERMISSION_GRANTED

        }

    }

    companion object {
        const val RequestPermissionCode = 100
    }


    fun checkAndRequestAudioPermissions() {
        val recordAudioPermission = Manifest.permission.RECORD_AUDIO

        if (ContextCompat.checkSelfPermission(this, recordAudioPermission) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(recordAudioPermission), RequestPermissionCode)
        }
        // Permission already granted, you can proceed with audio recording logic
    }




}