package com.genralstaff.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.home.ui.FullScreenImageActivity
import com.genralstaff.responseModel.GetChatMessagesResponse
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils.getNotificationTime
import com.makeramen.roundedimageview.RoundedImageView
import com.rygelouv.audiosensei.player.AudioSenseiPlayerView
import com.genralstaff.utils.MessageTranslationHelper


class ChatAdapter(
    private val mContext: Context,
    private var arrayList: ArrayList<GetChatMessagesResponse.Message>,
    private val otherUserImage: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null
    private val TYPE_USER = 0
    private val TYPE_FRIEND = 1

    fun updateList(arrayList: ArrayList<GetChatMessagesResponse.Message>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserViewHolder(inflater.inflate(R.layout.item_chat_right, parent, false))
            TYPE_FRIEND -> FriendViewHolder(
                inflater.inflate(
                    R.layout.item_chat_left, parent, false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = arrayList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = arrayList[position]
        val timeFormat = getNotificationTime(message.createdAt) ?: ""

        when (holder) {
            is FriendViewHolder -> {
                holder.tvTimerleft.text = timeFormat

                when (message.message_type) {

                    3 -> {
                        holder.rl_audio_left.visibility = View.VISIBLE
                        holder.receiverText.visibility = View.GONE
                        holder.left_uploaded_image.visibility = View.GONE
                        val audio_url = profileBaseUrl+message.message
                        holder.left_audio_player.setAudioTarget(audio_url)
                    }

                    2 -> {
                        holder.rl_audio_left.visibility = View.GONE
                        holder.receiverText.visibility = View.GONE
                        holder.left_uploaded_image.visibility = View.VISIBLE
                        Glide.with(mContext).load(profileBaseUrl + message.message)
                            .placeholder(R.drawable.place_holder).into(holder.left_uploaded_image)
                        holder.left_uploaded_image.setOnClickListener {
                            mContext.startActivity(
                                Intent(mContext, FullScreenImageActivity::class.java)
                                .putExtra("url",profileBaseUrl + message.message)
                            )
                        }
                    }

                    else -> {
                        holder.left_uploaded_image.visibility = View.GONE
                        holder.rl_audio_left.visibility = View.GONE
                        holder.receiverText.visibility = View.VISIBLE
                        // Translate location messages
                        val fullText = if (MessageTranslationHelper.isLocationMessage(message.message)) {
                            MessageTranslationHelper.translateLocationMessage(mContext, message.message)
                        } else {
                            message.message
                        }

// Use regex to find URLs
                        val urlPattern = Patterns.WEB_URL
                        val matcher = urlPattern.matcher(fullText)

                        val spannable = SpannableString(fullText)

                        while (matcher.find()) {
                            val start = matcher.start()
                            val end = matcher.end()
                            val url = matcher.group()

                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    widget.context.startActivity(intent)
                                }

                                override fun updateDrawState(ds: TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = Color.BLUE  // Link color like iOS
                                    ds.isUnderlineText = false // Optional: remove underline
                                }
                            }

                            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        holder.receiverText.apply {
                            text = spannable
                            movementMethod = LinkMovementMethod.getInstance()
                            highlightColor = Color.TRANSPARENT // Removes link tap highlight
                        }


                    }
                }
                Glide.with(mContext).load(otherUserImage).placeholder(R.drawable.place_holder)
                    .into(holder.receiverImage)
            }

            is UserViewHolder -> {
                if (message.is_read == 0) {
                    // Set the unseen icon and apply gray tint
                    holder.ivSeen.setImageResource(R.drawable.unseen)
                    holder.ivSeen.setColorFilter(ContextCompat.getColor(mContext, R.color.grey), PorterDuff.Mode.SRC_IN)
                } else {
                    // Set the seen icon with no tint (default)
                    holder.ivSeen.setImageResource(R.drawable.seen)
                    holder.ivSeen.colorFilter = null // Removes any previous tint if necessary
                }
                //1- simple message,2,image,3 audio
                when (message.message_type) {
                    3 -> {
                        holder.rl_audio_right.visibility = View.VISIBLE
                        holder.senderText.visibility = View.GONE
                        holder.right_uploaded_image.visibility = View.GONE
                        val audio_url = profileBaseUrl+message.message
                        holder.right_audio_player.setAudioTarget(audio_url)
                        holder.tvTime.text = timeFormat
                    }

                    2 -> {
                        holder.rl_audio_right.visibility = View.GONE
                        holder.senderText.visibility = View.GONE
                        holder.right_uploaded_image.visibility = View.VISIBLE
                        Glide.with(mContext).load(profileBaseUrl + message.message)
                            .placeholder(R.drawable.place_holder).into(holder.right_uploaded_image)
                        holder.tvTime.text = timeFormat
                        holder.right_uploaded_image.setOnClickListener {
                            mContext.startActivity(Intent(mContext,FullScreenImageActivity::class.java)
                                .putExtra("url",profileBaseUrl + message.message)
                            )
                        }
                    }

                    else -> {
                        holder.right_uploaded_image.visibility = View.GONE
                        holder.rl_audio_right.visibility = View.GONE
                        holder.senderText.visibility = View.VISIBLE
                        // Translate location messages
                        val fullText = if (MessageTranslationHelper.isLocationMessage(message.message)) {
                            MessageTranslationHelper.translateLocationMessage(mContext, message.message)
                        } else {
                            message.message
                        }


// Check if there's any URL in the message
                        val urlPattern = Patterns.WEB_URL
                        val matcher = urlPattern.matcher(fullText)

                        val spannable = SpannableString(fullText)

                        while (matcher.find()) {
                            val start = matcher.start()
                            val end = matcher.end()
                            val url = matcher.group()

                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    widget.context.startActivity(intent)
                                }

                                override fun updateDrawState(ds: TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = Color.BLUE  // iOS-style blue color for link
                                    ds.isUnderlineText = false  // Optional: remove underline
                                }
                            }

                            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        holder.senderText.apply {
                            text = spannable
                            movementMethod = LinkMovementMethod.getInstance()
                            highlightColor = Color.TRANSPARENT // No background on click
                        }


                        holder.tvTime.text = timeFormat
                    }
                }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = arrayList[position]
        return if (prefs?.getString("userId")?.toInt() == message.sender_id) {
            TYPE_USER
        } else {
            TYPE_FRIEND
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderText: TextView = itemView.findViewById(R.id.tvMessageRight)
        val tvTime: TextView = itemView.findViewById(R.id.tvTimer)
        val ivSeen: ImageView = itemView.findViewById(R.id.ivSeen)
        val rl_audio_right: RelativeLayout = itemView.findViewById(R.id.rl_audio_right)
        val right_audio_player: AudioSenseiPlayerView =
            itemView.findViewById(R.id.right_audio_player)
        val right_uploaded_image: RoundedImageView =
            itemView.findViewById(R.id.right_uploaded_image)

    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTimerleft: TextView = itemView.findViewById(R.id.tvTimerleft)
        val receiverText: TextView = itemView.findViewById(R.id.tvMessage)
        val receiverImage: ImageView = itemView.findViewById(R.id.ivImage)
        val rl_audio_left: RelativeLayout = itemView.findViewById(R.id.rl_audio_left)
        val left_audio_player: AudioSenseiPlayerView = itemView.findViewById(R.id.left_audio_player)
        val left_uploaded_image: RoundedImageView = itemView.findViewById(R.id.left_uploaded_image)

    }
}
