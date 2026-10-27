package com.example.carvideostreamer

import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.car.app.connection.CarConnection
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    // Change this string to your desired video stream (HLS .m3u8, DASH .mpd, or direct .mp4)
    private val defaultStreamUrl =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.player_view)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        observeCarConnection()
    }

    private fun initializePlayer(streamUrl: String) {
        if (player == null) {
            player = ExoPlayer.Builder(this).build().also { exoPlayer ->
                playerView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    private fun observeCarConnection() {
        val carConnection = CarConnection(this)
        carConnection.type.observe(this) { connectionType ->
            val displayId = display?.displayId ?: Display.DEFAULT_DISPLAY
            val isProjectedToCar = (connectionType == CarConnection.CONNECTION_TYPE_PROJECTION) &&
                    (displayId != Display.DEFAULT_DISPLAY)

            if (isProjectedToCar) {
                // Adjust video scaling specifically for head unit display aspect ratios
                playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer(defaultStreamUrl)
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.release()
        }
        player = null
        playerView.player = null
    }
}
