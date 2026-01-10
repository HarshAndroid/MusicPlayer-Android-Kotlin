package com.harshRajpurohit.musicPlayer

import Model.Track
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harshRajpurohit.musicPlayer.databinding.MusicViewBinding
import kotlin.jvm.java
import com.harshRajpurohit.musicPlayer.utils.formatDuration


class OnlineMusicAdapter(
    private val list: ArrayList<Track>
) : RecyclerView.Adapter<OnlineMusicAdapter.Holder>() {

    class Holder(val binding: MusicViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = MusicViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val track = list[position]
        val context = holder.itemView.context

        // ✅ Safe title
        holder.binding.songNameMV.text =
            track.name.ifEmpty { "Unknown Title" }

        // ✅ Safe artist
        holder.binding.songAlbumMV.text =
            track.artist_name.ifEmpty { "Unknown Artist" }

        // ✅ Duration (Jamendo gives seconds)
        holder.binding.songDuration.text =
            formatDuration(track.duration * 1000L)

        // ✅ Safe image loading
        Glide.with(context)
            .load(track.image)
            .placeholder(R.drawable.music_player_icon_slash_screen)
            .error(R.drawable.music_player_icon_slash_screen)
            .into(holder.binding.imageMV)

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, OnlinePlayerActivity::class.java)
            intent.putExtra("url", track.audio)
            intent.putExtra("title", track.name)
            intent.putExtra("artist", track.artist_name)
            intent.putExtra("image", track.image)
            intent.putExtra("duration", track.duration)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    fun setSongs(data: List<Track>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}

