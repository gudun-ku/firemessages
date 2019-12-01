package com.beloushkin.firemessage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.beloushkin.firemessage.model.MessageType
import com.beloushkin.firemessage.model.TextMessage
import com.beloushkin.firemessage.util.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId)  { channelId ->
            messagesListenerRegistration =
                FirestoreUtil.addChatMessagesListener(channelId,this, this::updateRecyclerView)

            imageView_send.setOnClickListener {
                val messageToSend = TextMessage(editText_message.text.toString(),
                    Calendar.getInstance().time, FirebaseAuth.getInstance().currentUser!!.uid,
                    MessageType.TEXT)
                editText_message.setText("")
                FirestoreUtil.sendMessage(messageToSend, channelId)

            }

            fab_send_image.setOnClickListener {
                //TODO send image messages
            }

        }

    }

    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                }
            }

            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView) {
            init()
        } else {
            updateItems()
        }

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)

    }
}
