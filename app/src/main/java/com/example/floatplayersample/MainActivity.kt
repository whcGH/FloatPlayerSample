package com.example.floatplayersample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.aliyun.player.IPlayer
import com.aliyun.player.source.UrlSource
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.MoveType
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_show_player.setOnClickListener {
            bindService(Intent(this, PlayerService::class.java), conn, Context.BIND_AUTO_CREATE)
            FloatWindowUtils.show()
        }
        btn_hide_player.setOnClickListener {
            FloatWindowUtils.hide()
        }
    }

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v("playerService", "conn disconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerBinder) {
                val urlSource = UrlSource()
                urlSource.uri =
                    "https://cloud.video.taobao.com/play/u/104017515/p/1/e/6/t/1/50235454612.mp4"
                service.setSource(urlSource)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(conn)
        } catch (e: Exception) {
        }
    }
}

object FloatWindowUtils {
    fun initFloatWindow() {
        FloatWindow.with(MyApp.instance)
            .setView(R.layout.layout_float_player)
            .setMoveType(MoveType.active)
            .setMoveStyle(500, BounceInterpolator())
            .setWidth(400)
            .setHeight(200)
            .build()
        val view = FloatWindow.get().view
        view.findViewById<ImageView>(R.id.iv_close)
            .setOnClickListener {
                destroy()
            }
        FloatWindow.get().show()

        MyApp.instance.bindService(
            Intent(MyApp.instance, PlayerService::class.java),
            conn,
            Context.BIND_AUTO_CREATE
        )
    }

    fun show() {
        if (FloatWindow.get() != null && !FloatWindow.get().isShowing) {
            FloatWindow.get().show()
        } else if (FloatWindow.get() == null) {
            initFloatWindow()
        }
    }

    fun hide() {
        if (FloatWindow.get() != null && FloatWindow.get().isShowing) {
            FloatWindow.get().hide()
        }
    }

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v("playerService", "conn disconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v("playerService", "conn service connected")
            val view = FloatWindow.get().view
            if (service is PlayerService.PlayerBinder && view != null) {
                service.setListeners(
                    onPreparedListener = {
                        service.playerStart()
                    },
                    onCompletionListener = {
                        destroy()
                    }
                )

                view.findViewById<FrameLayout>(R.id.fl_container)
                    .addView(service.getView(view.context))

                view.setOnClickListener {
                    service.goPage(LandscapePlayerActivity::class.java)
                }
            }
        }
    }

    fun destroy() {
        if (FloatWindow.get() != null) {
            FloatWindow.get().view.context.unbindService(conn)
            FloatWindow.destroy()
        }
    }
}
