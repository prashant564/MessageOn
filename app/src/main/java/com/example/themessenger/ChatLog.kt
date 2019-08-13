package com.example.themessenger

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.example.themessenger.models.Messages
import com.example.themessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_letsmessage.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLog : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chatlog.adapter = adapter

        var user: User = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        supportActionBar?.title = user.username
        listenForMessages()

        button_send_message.setOnClickListener {
            Log.d("ChatLog","Trying to send amessage")
            performSendMessage()
        }

    }

    private fun listenForMessages(){

        val fromId = FirebaseAuth.getInstance().uid
        val user: User = intent.getParcelableExtra(NewMessage.USER_KEY)
        val toid = user.uid


        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toid")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val messages = p0.getValue(Messages::class.java)


                if(messages!=null){

                    Log.d("ChatLog",messages.text)


                    if(messages.fromId == FirebaseAuth.getInstance().uid) {
                        val currentuser = letsmessage.currentUser
                        adapter.add(ChatFromItem(messages.text, currentuser!!))
                    } else{

                        val touser: User = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
                        adapter.add(ChatToItem(messages.text, touser))

                    }

                }

                recyclerView_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun performSendMessage(){
        //send message to firebase

        val text = editText_enter_message.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user: User = intent.getParcelableExtra(NewMessage.USER_KEY)
        val toId = user.uid

//        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toId").push()
        val to_ref = FirebaseDatabase.getInstance().getReference("/user_messages/$toId/$fromId").push()

        if(fromId==null) return

        //the whole message
        val messages = Messages(ref.key!!, text, fromId,toId, System.currentTimeMillis()/1000)


        ref.setValue(messages)
            .addOnSuccessListener {
                Log.d("ChatLog","message saved:")
                editText_enter_message.text.clear()
                recyclerView_chatlog.scrollToPosition(adapter.itemCount-1)
            }

        to_ref.setValue(messages)

        val referenceLatestMessage = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId/$toId")
        referenceLatestMessage.setValue(messages)


        val referenceToLatestMessage = FirebaseDatabase.getInstance().getReference("/latest_messages/$toId/$fromId")
        referenceToLatestMessage.setValue(messages)
    }
}


class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_from.text = text

        val url = user.image_url
        val image_from_row = viewHolder.itemView.imageView_from_row
        Picasso.get().load(url).into(image_from_row)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_chat_to.text = text

        //add image of our user on the chatlog screen
        val url = user.image_url
        val image_to_row = viewHolder.itemView.imageView_to_row
        Picasso.get().load(url).into(image_to_row)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}