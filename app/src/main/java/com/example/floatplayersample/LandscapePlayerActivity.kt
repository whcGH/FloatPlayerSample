package com.example.floatplayersample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.InfoCode
import kotlinx.android.synthetic.main.activity_landscape_player.*

class LandscapePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landscape_player)
        bindService(Intent(this, PlayerService::class.java), conn, Context.BIND_AUTO_CREATE)
        FloatWindowUtils.destroy()
        tv_back.setOnClickListener {
            finish()
        }
    }

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerBinder) {
                service.setListeners(onPreparedListener = {
                    tv_total_time.text = formatTimeMMss(it)
                    sb_progress.max = (it / 1000).toInt()
                }, onInfoListener = {
                    when (it.code) {
                        InfoCode.CurrentPosition -> { // 当前进度
//                            timeProcess = it.extraValue
                            tv_current_time.text = formatTimeMMss(it.extraValue)
                            sb_progress.progress = (it.extraValue / 1000).toInt()
                        }
                        InfoCode.BufferedPosition -> { // 加载进度
                            sb_progress.secondaryProgress = (it.extraValue / 1000).toInt()
                        }
                    }
                }, onStateChange = {
                    //开始按钮变更
                    when (it) {
                        IPlayer.started -> {
                            btn_start.setImageResource(R.mipmap.video_pause)
                        }
                        else -> {
                            btn_start.setImageResource(R.mipmap.video_play)
                        }
                    }
                })

                btn_start.setOnClickListener {
                    service.startClick()
                }
                sb_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        service.playerPause()
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        service.playerSeekTo(((seekBar?.progress ?: 0) * 1000).toLong())
                        service.playerStart()
                    }
                })
                fl_container.addView(service.getView(this@LandscapePlayerActivity))
            }
        }
    }

    fun formatTimeMMss(time: Long): String {
        return String.format("%02d:%02d", (time / 1000) / 60, (time / 1000) % 60)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(
            conn
        )
        FloatWindowUtils.initFloatWindow()
    }
}