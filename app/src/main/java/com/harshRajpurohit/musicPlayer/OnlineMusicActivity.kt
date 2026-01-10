package com.harshRajpurohit.musicPlayer

import Network.RetrofitClient
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.harshRajpurohit.musicPlayer.databinding.ActivityMainBinding
import com.harshRajpurohit.musicPlayer.databinding.ActivityOnlineMusicBinding
import kotlinx.coroutines.launch


class OnlineMusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineMusicBinding
    private lateinit var adapter: OnlineMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnlineMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadOnlineSongs()
    }

    private fun setupRecyclerView() {
        adapter = OnlineMusicAdapter(arrayListOf())

        binding.onlineMusicRV.layoutManager = LinearLayoutManager(this)
        binding.onlineMusicRV.adapter = adapter
    }

    private fun loadOnlineSongs() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getTracks(
                    clientId = "1fbc4eed"
                )

                binding.progressBar.visibility = View.GONE

                if (response.results.isEmpty()) {
                    Toast.makeText(
                        this@OnlineMusicActivity,
                        "No online songs found",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    adapter.setSongs(response.results)
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE

                Toast.makeText(
                    this@OnlineMusicActivity,
                    "Error loading online music",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
