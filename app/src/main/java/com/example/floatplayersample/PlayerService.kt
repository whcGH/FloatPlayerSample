package com.example.floatplayersample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoBean
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.*

class PlayerService : Service() {

    lateinit var aliyunPlayer: AliPlayer
    private var playerState: Int = AliPlayer.idle

    override fun onCreate() {
        Log.i("playerService", "onCreate - Thread ID = " + Thread.currentThread().id)
        super.onCreate()
        aliyunPlayer = AliPlayerFactory.createAliPlayer(applicationContext)
        aliyunPlayer.setOnCompletionListener {
            aliyunPlayer.prepare()
            onCompletionListener?.invoke()
        }
        aliyunPlayer.setOnErrorListener {
            //播放错误
            onErrorListener?.invoke(it)
        }
        aliyunPlayer.setOnPreparedListener {
            //准备完成
            aliyunPlayer.redraw()

            onPreparedListener?.invoke(aliyunPlayer.duration)
        }
        aliyunPlayer.setOnVideoSizeChangedListener { width, height ->
            // 视频分辨率变化回调
            onVideoSizeChangedListener?.invoke(width, height)
        }
        aliyunPlayer.setOnRenderingStartListener {
            //首帧渲染事件
            onRenderingStartListener?.invoke()
        }
        aliyunPlayer.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
            onInfoListener?.invoke(it)
        }
        aliyunPlayer.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
            override fun onLoadingBegin() {
                //缓冲开始。
                onLoadingStatusListener?.onLoadingBegin()
            }

            override fun onLoadingProgress(percent: Int, kbps: Float) {
                //缓冲进度
                onLoadingStatusListener?.onLoadingProgress(percent, kbps)
            }

            override fun onLoadingEnd() {
                //缓冲结束
                onLoadingStatusListener?.onLoadingEnd()
            }
        })
        aliyunPlayer.setOnSeekCompleteListener {
            //拖动结束
            onSeekComplete?.invoke()
        }
        aliyunPlayer.setOnSubtitleDisplayListener(object : IPlayer.OnSubtitleDisplayListener {
            override fun onSubtitleShow(id: Long, data: String) {
                //显示字幕
                onSubtitleDisplayListener?.onSubtitleShow(id, data)
            }

            override fun onSubtitleHide(id: Long) {
                //隐藏字幕
                onSubtitleDisplayListener?.onSubtitleHide(id)
            }
        })
        aliyunPlayer.setOnTrackChangedListener(object : IPlayer.OnTrackChangedListener {
            override fun onChangedSuccess(trackInfo: TrackInfo) {
                //切换音视频流或者清晰度成功
                onTrackChangedListener?.onChangedSuccess(trackInfo)
            }

            override fun onChangedFail(trackInfo: TrackInfo, errorInfo: ErrorInfo) {
                //切换音视频流或者清晰度失败
                onTrackChangedListener?.onChangedFail(trackInfo, errorInfo)
            }
        })
        aliyunPlayer.setOnStateChangedListener {
            //播放器状态改变事件
            playerState = it
            onStateChange?.invoke(it)
        }
        aliyunPlayer.setOnSnapShotListener { bm, with, height ->
            //截图事件
            onSnapShot?.invoke(bm, with, height)
        }
    }

    var onCompletionListener: (() -> Unit)? = null
    var onErrorListener: ((ErrorInfo) -> Unit)? = null
    var onPreparedListener: ((Long) -> Unit)? = null
    var onVideoSizeChangedListener: ((Int, Int) -> Unit)? = null
    var onRenderingStartListener: (() -> Unit)? = null
    var onInfoListener: ((InfoBean) -> Unit)? = null
    var onLoadingStatusListener: IPlayer.OnLoadingStatusListener? = null
    var onSeekComplete: (() -> Unit)? = null
    var onSubtitleDisplayListener: IPlayer.OnSubtitleDisplayListener? = null
    var onTrackChangedListener: IPlayer.OnTrackChangedListener? = null
    var onStateChange: ((Int) -> Unit)? = null
    var onSnapShot: ((Bitmap, Int, Int) -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("playerService", "onBind - Thread ID = " + Thread.currentThread().id);
        return PlayerBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(
            "playerService",
            "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().id
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        onCompletionListener = null
        onErrorListener = null
        onPreparedListener = null
        onVideoSizeChangedListener = null
        onRenderingStartListener = null
        onInfoListener = null
        onLoadingStatusListener = null
        onSeekComplete = null
        onSubtitleDisplayListener = null
        onTrackChangedListener = null
        onStateChange = null
        onSnapShot = null

        aliyunPlayer.pause()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i("playerService", "onDestroy - Thread ID = " + Thread.currentThread().id)
        super.onDestroy()
    }

    inner class PlayerBinder : Binder() {
        fun setListeners(
            onCompletionListener: (() -> Unit)? = null,
            onErrorListener: ((ErrorInfo) -> Unit)? = null,
            onPreparedListener: ((Long) -> Unit)? = null,
            onVideoSizeChangedListener: ((Int, Int) -> Unit)? = null,
            onRenderingStartListener: (() -> Unit)? = null,
            onInfoListener: ((InfoBean) -> Unit)? = null,
            onLoadingStatusListener: IPlayer.OnLoadingStatusListener? = null,
            onSeekComplete: (() -> Unit)? = null,
            onSubtitleDisplayListener: IPlayer.OnSubtitleDisplayListener? = null,
            onTrackChangedListener: IPlayer.OnTrackChangedListener? = null,
            onStateChange: ((Int) -> Unit)? = null,
            onSnapShot: ((Bitmap, Int, Int) -> Unit)? = null
        ) {
            this@PlayerService.onCompletionListener = onCompletionListener
            this@PlayerService.onErrorListener = onErrorListener
            this@PlayerService.onPreparedListener = onPreparedListener
            this@PlayerService.onVideoSizeChangedListener = onVideoSizeChangedListener
            this@PlayerService.onRenderingStartListener = onRenderingStartListener
            this@PlayerService.onInfoListener = onInfoListener
            this@PlayerService.onLoadingStatusListener = onLoadingStatusListener
            this@PlayerService.onSeekComplete = onSeekComplete
            this@PlayerService.onSubtitleDisplayListener = onSubtitleDisplayListener
            this@PlayerService.onTrackChangedListener = onTrackChangedListener
            this@PlayerService.onStateChange = onStateChange
            this@PlayerService.onSnapShot = onSnapShot
        }

        fun getView(context: Context): SurfaceView {
            val surfaceView = SurfaceView(context)
            surfaceView.keepScreenOn = true
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
                    aliyunPlayer.redraw()
                }

                override fun surfaceDestroyed(p0: SurfaceHolder?) {
                    aliyunPlayer.setDisplay(null)
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    aliyunPlayer.setDisplay(holder)
                    aliyunPlayer.setSurface(surfaceView.holder.surface)
                }
            })
            return surfaceView
        }

        var playerStart = {
            aliyunPlayer.start()
        }
        var playerPause = {
            aliyunPlayer.pause()
        }
        var playerStop = {
            aliyunPlayer.stop()
        }
        var playerSeekTo: (Long) -> Unit = { position ->
            aliyunPlayer.seekTo(position)
        }
        var playerRelease = {
            aliyunPlayer.release()
        }
        var playerResume = {
            if (playerState == IPlayer.started) {
                playerStart()
            }
        }

        var startClick = {
            when (playerState) {
                IPlayer.started -> {
                    playerPause()
                }
                IPlayer.stopped, IPlayer.completion -> {
                    playerSeekTo(0)
                    playerStart()
                }
                IPlayer.paused, IPlayer.prepared -> {
                    playerStart()
                }
            }
        }

        fun setSource(source: UrlSource) {
            aliyunPlayer.reset()
            aliyunPlayer.setDataSource(source)
            afterSetSource(source)
        }

        fun setSource(source: VidSts) {
            aliyunPlayer.reset()
            aliyunPlayer.setDataSource(source)
            afterSetSource(source)
        }

        fun setSource(source: VidAuth) {
            aliyunPlayer.reset()
            aliyunPlayer.setDataSource(source)
            afterSetSource(source)
        }

        fun setSource(source: VidMps) {
            aliyunPlayer.reset()
            aliyunPlayer.setDataSource(source)
            afterSetSource(source)
        }

        private fun <T : SourceBase> afterSetSource(source: T) {
            source.title
            aliyunPlayer.prepare()
        }

        fun goPage(clz: Class<*>) {
            startActivity(
                Intent(
                    this@PlayerService,
                    clz
                ).apply { addFlags(FLAG_ACTIVITY_NEW_TASK) })
        }
    }
}