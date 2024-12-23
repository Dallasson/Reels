package com.cars.reels

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper

class VideoAdapter(
    private val videoPaths: List<VideoModel>,
    private val viewPager: ViewPager2,
    private val context: Context
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isAutopilotEnabled = false
    private var isVoiceCommandTriggered = false
    private lateinit var currentVideoHolder: VideoViewHolder

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

        currentVideoHolder = holder
        updateIconsAppearance()

        holder.videoView.setOnPreparedListener {
                mediaPlayer -> mediaPlayer.start()
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = holder.videoView.width / holder.videoView.height.toFloat()

            val scale = videoRatio / screenRatio

            if(scale >= 1f){
                holder.videoView.scaleX = scale
            } else {
                holder.videoView.scaleY = 1f / scale
            }
        }

        holder.videoView.setOnCompletionListener {
            if (isAutopilotEnabled) {
                val nextPosition = viewPager.currentItem + 1
                if (nextPosition < videoPaths.size) {
                    viewPager.setCurrentItem(nextPosition, true)
                }
            } else {
                it.start()
            }
        }

        holder.enableAutoPilot.isChecked = isAutopilotEnabled
        holder.enableAutoPilot.setOnCheckedChangeListener { _, isChecked ->
            isAutopilotEnabled = isChecked
            if (isChecked) {
                startListening(holder)
            } else {
                stopListening()
                updateIconsAppearance()
            }
        }

        holder.like.setOnClickListener {
            holder.likeTxt.text = "1"
            Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.like)
        }

        holder.dislike.setOnClickListener {
            holder.likeTxt.text = "0"
            Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.dislike)
        }

        holder.comment.setOnClickListener {
            Toast.makeText(context, "Comment Posted", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.comment)
        }

        holder.share.setOnClickListener {
            Toast.makeText(context, "Shared", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.share)
        }

        holder.rotate.setOnClickListener {
            Toast.makeText(context, "Rotated", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.rotate)
        }
    }

    private fun highlightIcon(icon: ImageView) {
        val orangeTint = 0xFFFFA500.toInt()
        icon.setColorFilter(orangeTint, android.graphics.PorterDuff.Mode.SRC_ATOP)
        icon.postDelayed({
            icon.clearColorFilter()
            if (isAutopilotEnabled) {
                icon.setColorFilter(orangeTint, android.graphics.PorterDuff.Mode.SRC_ATOP)
            }
        }, 300)
    }

    private fun updateIconsAppearance() {
        val orangeTint = 0xFFFFA500.toInt()
        val whiteTint = 0xFFFFFFFF.toInt()
        val tint = if (isAutopilotEnabled) orangeTint else whiteTint

        currentVideoHolder.like.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
        currentVideoHolder.dislike.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
        currentVideoHolder.comment.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
        currentVideoHolder.share.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
        currentVideoHolder.rotate.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_ATOP)
    }

    private fun startListening(holder: VideoViewHolder) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                holder.videoView.pause()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                holder.videoView.start()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val command = matches[0].lowercase()
                    if (command.contains("enable autopilot")) {
                        isAutopilotEnabled = true
                        isVoiceCommandTriggered = true
                        Toast.makeText(context, "Autopilot Enabled", Toast.LENGTH_SHORT).show()
                        updateIconsAppearance()
                    }
                }
                holder.videoView.start()
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command")
        }
        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun getItemCount(): Int = videoPaths.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val videoTitle: TextView = itemView.findViewById(R.id.vidTitle)
        val videoDesc: TextView = itemView.findViewById(R.id.vidDescription)
        val like: ImageView = itemView.findViewById(R.id.like)
        val likeTxt: TextView = itemView.findViewById(R.id.likesText)
        val dislike: ImageView = itemView.findViewById(R.id.dislike)
        val comment: ImageView = itemView.findViewById(R.id.comment)
        val share: ImageView = itemView.findViewById(R.id.share)
        val rotate: ImageView = itemView.findViewById(R.id.rotate)
        val enableAutoPilot: SwitchCompat = itemView.findViewById(R.id.switchButton)
    }
}
