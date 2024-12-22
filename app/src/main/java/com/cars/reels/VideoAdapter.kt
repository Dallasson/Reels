package com.cars.reels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(private val videoPaths: List<VideoModel>, private val viewPager: ViewPager2) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val videoTitle: TextView = itemView.findViewById(R.id.vidTitle)
        val videoDesc: TextView = itemView.findViewById(R.id.vidDescription)

        // References for the views inside LinearLayout
        val like: ImageView = itemView.findViewById(R.id.like)
        val likesText: TextView = itemView.findViewById(R.id.likesText)
        val dislike: ImageView = itemView.findViewById(R.id.dislike)
        val report: ImageView = itemView.findViewById(R.id.report)
        val record: ImageView = itemView.findViewById(R.id.record)
        val next: ImageView = itemView.findViewById(R.id.next)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoModel = videoPaths[position]
        holder.videoTitle.text = videoModel.videoTitle
        holder.videoDesc.text = videoModel.videoDesc
        holder.videoView.setVideoPath(videoModel.videoUrl)

        // Set on prepared listener for scaling video view
        holder.videoView.setOnPreparedListener {
            it.start()

            val videoRatio = it.videoWidth / it.videoHeight.toFloat()
            val screenRatio = holder.videoView.width / holder.videoView.height.toFloat()
            val scale = videoRatio / screenRatio

            if (scale >= 1f) {
                holder.videoView.scaleX = scale
            } else {
                holder.videoView.scaleY = 1f / scale
            }
        }

        // Set on completion listener for looping video
        holder.videoView.setOnCompletionListener {
            it.start()
        }

        // Handle speech-based actions
        handleSpeechCommands(holder, position)

        // Set the "Next" button action directly inside the adapter
        holder.next.setOnClickListener {
            if (position < videoPaths.size - 1) {
                println("Next button clicked, moving to next clip.")
                viewPager.setCurrentItem(position + 1, true)
            } else {
                Toast.makeText(holder.itemView.context, "No more videos", Toast.LENGTH_SHORT).show()
            }
        }

        // Debugging: Log to see if click listeners are triggered
        holder.like.setOnClickListener {
            println("Like button clicked.")
            holder.likesText.text =  "1"
            highlightButton(holder.like)
        }

        holder.dislike.setOnClickListener {
            println("Dislike button clicked.")
            holder.likesText.text =  "0"
            highlightButton(holder.dislike)
        }

        holder.report.setOnClickListener {
            println("Report button clicked.")
            Toast.makeText(holder.itemView.context, "Clip reported", Toast.LENGTH_SHORT).show()
        }

        holder.record.setOnClickListener {
            println("Record button clicked.")
            highlightButton(holder.record)
            Toast.makeText(holder.itemView.context, "Saved clip", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSpeechCommands(holder: VideoViewHolder, position: Int) {
        // Assuming you have speech command logic where `command` is the detected speech
        val command = "" // Get the speech command, like "Like", "Dislike", "Report", etc.

        when (command.lowercase()) {
            "like" -> {
                println("Speech command: Like")
                highlightButton(holder.like)
            }
            "dislike" -> {
                println("Speech command: Dislike")
                highlightButton(holder.dislike)
            }
            "report" -> {
                println("Speech command: Report")
                Toast.makeText(holder.itemView.context, "Clip reported", Toast.LENGTH_SHORT).show()
            }
            "record" -> {
                println("Speech command: Record")
                highlightButton(holder.record)
                Toast.makeText(holder.itemView.context, "Saved clip", Toast.LENGTH_SHORT).show()
            }
            "next" -> {
                if (position < videoPaths.size - 1) {
                    println("Speech command: Next, moving to next clip.")
                    viewPager.setCurrentItem(position + 1, true)
                }
            }
        }
    }

    private fun highlightButton(button: ImageView) {
        button.setColorFilter(0xFF00FF00.toInt())
        button.postDelayed({ button.clearColorFilter() }, 500)
    }

    override fun getItemCount(): Int = videoPaths.size

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        // Optionally stop video playback when the view is recycled
        holder.videoView.stopPlayback()
    }
}
