package com.harshRajpurohit.musicPlayer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.harshRajpurohit.musicPlayer.databinding.ActivityOnlinePlayerBinding

class OnlinePlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityOnlinePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnlinePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Safe intent values
        val url = intent.getStringExtra("url")
        val title = intent.getStringExtra("title") ?: "Unknown Title"
        val artist = intent.getStringExtra("artist") ?: "Unknown Artist"
        val image = intent.getStringExtra("image")

        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "Audio URL not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🎵 UI
        binding.songTitle.text = title
        binding.songArtist.text = artist

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.music_player_icon_slash_screen)
            .into(binding.songImage)

        // ▶ ExoPlayer
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}

