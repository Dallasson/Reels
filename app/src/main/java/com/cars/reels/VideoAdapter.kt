import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import android.widget.Switch
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.widget.SwitchCompat
import com.cars.reels.R
import com.cars.reels.VideoModel

class VideoAdapter(
    private val videoPaths: List<VideoModel>,
    private val viewPager: ViewPager2,
    private val context: Context
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isAutopilotEnabled = false
    private var isVoiceCommandTriggered = false

    init {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(context, "Speech recognition error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    val command = it[0].lowercase()
                    if (command.contains("enable autopilot")) {
                        isAutopilotEnabled = true
                        isVoiceCommandTriggered = true
                        // Safely update the adapter
                        viewPager.post {
                            notifyDataSetChanged()
                        }
                        Toast.makeText(context, "Autopilot Enabled", Toast.LENGTH_SHORT).show()
                    } else if (command.contains("disable autopilot")) {
                        isAutopilotEnabled = false
                        isVoiceCommandTriggered = true
                        // Safely update the adapter
                        viewPager.post {
                            notifyDataSetChanged()
                        }
                        Toast.makeText(context, "Autopilot Disabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command")
        }
        speechRecognizer?.startListening(recognizerIntent)
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

        updateIconsTint(holder)

        holder.videoView.setOnPreparedListener {
            it.start()

            val videoRatio = it.videoWidth / it.videoHeight.toFloat()
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
                if (position < videoPaths.size - 1) {
                    viewPager.setCurrentItem(position + 1, true)
                } else {
                    Toast.makeText(context, "No more videos", Toast.LENGTH_SHORT).show()
                }
            } else {
                it.start()
            }
        }

        holder.like.setOnClickListener {
            holder.likeText.text = "1"
            Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
            highlightIcon(holder.like)
        }

        holder.dislike.setOnClickListener {
            holder.likeText.text = "0"
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

        holder.enableAutoPilot.isChecked = isAutopilotEnabled
        holder.enableAutoPilot.setOnCheckedChangeListener { _, isChecked ->
            if (!isVoiceCommandTriggered) {
                isAutopilotEnabled = isChecked
            }
            isVoiceCommandTriggered = false
            notifyDataSetChanged()
            if (isChecked) {
                startListening()
            }
        }
    }

    private fun updateIconsTint(holder: VideoViewHolder) {
        val tintColor = if (isAutopilotEnabled) Color.parseColor("#FFA500") else Color.WHITE
        holder.like.setColorFilter(tintColor)
        holder.dislike.setColorFilter(tintColor)
        holder.comment.setColorFilter(tintColor)
        holder.share.setColorFilter(tintColor)
        holder.rotate.setColorFilter(if (isAutopilotEnabled) Color.parseColor("#FFA500") else Color.WHITE)
    }

    private fun highlightIcon(icon: ImageView) {
        icon.setColorFilter(Color.parseColor("#FFFF00")) // Highlight with yellow tint
        icon.postDelayed({
            icon.setColorFilter(if (isAutopilotEnabled) Color.parseColor("#FFA500") else Color.WHITE)
        }, 300)
    }

    override fun getItemCount(): Int = videoPaths.size

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.videoView.stopPlayback()
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val videoTitle: TextView = itemView.findViewById(R.id.vidTitle)
        val videoDesc: TextView = itemView.findViewById(R.id.vidDescription)
        val like: ImageView = itemView.findViewById(R.id.like)
        val dislike: ImageView = itemView.findViewById(R.id.dislike)
        val comment: ImageView = itemView.findViewById(R.id.comment)
        val share: ImageView = itemView.findViewById(R.id.share)
        val rotate: ImageView = itemView.findViewById(R.id.rotate)
        val likeText: TextView = itemView.findViewById(R.id.likesText)
        val enableAutoPilot: SwitchCompat = itemView.findViewById(R.id.switchButton)
    }
}
