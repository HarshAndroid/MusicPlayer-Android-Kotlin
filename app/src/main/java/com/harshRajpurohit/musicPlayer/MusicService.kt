package com.harshRajpurohit.musicPlayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int) {
        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(
            baseContext, NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen)
        }

        val notification =
            androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
                .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
                .setSmallIcon(R.drawable.music_icon).setLargeIcon(image)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.previous_icon, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Play", playPendingIntent)
                .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
                .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong()
                ).build()
            )

            mediaSession.setPlaybackState(getPlayBackState())
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                //called when play button is pressed
                override fun onPlay() {
                    super.onPlay()
                    handlePlayPause()
                }

                //called when pause button is pressed
                override fun onPause() {
                    super.onPause()
                    handlePlayPause()
                }

                //called when next button is pressed
                override fun onSkipToNext() {
                    super.onSkipToNext()
                    prevNextSong(increment = true, context = baseContext)
                }

                //called when previous button is pressed
                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    prevNextSong(increment = false, context = baseContext)
                }

                //called when headphones buttons are pressed
                //currently only pause or play music on button click
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    handlePlayPause()
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                //called when seekbar is changed
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer?.seekTo(pos.toInt())

                    mediaSession.setPlaybackState(getPlayBackState())
                }
            })
        }

        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            mediaPlayer?.prepare()

            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            showNotification(R.drawable.pause_icon)
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text =
                formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
            PlayerActivity.loudnessEnhancer = LoudnessEnhancer(mediaPlayer!!.audioSessionId)
            PlayerActivity.loudnessEnhancer.enabled = true
        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    fun getPlayBackState(): PlaybackStateCompat {
        val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F

        return PlaybackStateCompat.Builder().setState(
            if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .build()
    }

    fun handlePlayPause() {
        if (PlayerActivity.isPlaying) pauseMusic()
        else playMusic()

        //update playback state for notification
        mediaSession.setPlaybackState(getPlayBackState())
    }



    private fun prevNextSong(increment: Boolean, context: Context){

        setSongPosition(increment = increment)

        PlayerActivity.musicService?.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop())
            .into(PlayerActivity.binding.songImgPA)

        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop())
            .into(NowPlaying.binding.songImgNP)

        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        playMusic()

        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFavourite) PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
        else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)

        //update playback state for notification
        mediaSession.setPlaybackState(getPlayBackState())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            pauseMusic()
        }
//        else{
//            playMusic()
//        }
    }

    private fun playMusic(){
        //play music
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
        mediaPlayer?.start()
        showNotification(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        //pause music
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
        mediaPlayer!!.pause()
        showNotification(R.drawable.play_icon)
    }




    //for making persistent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

}