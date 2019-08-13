package com.example.themessenger.views

import androidx.core.app.NotificationCompat
import com.example.themessenger.R
import com.example.themessenger.models.Messages
import com.example.themessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.letsmessage_row.view.*

class LatestMessageRow(val messages: Messages): Item<ViewHolder>(){

    var chatPartnerUser: User?= null


    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.textView_latest_message.text = messages.text

        val chatPartnerId: String

        if(messages.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = messages.toId
        }
        else{
            chatPartnerId = messages.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                chatPartnerUser = p0.getValue(User::class.java)


                viewHolder.itemView.textView_username.text = chatPartnerUser?.username
                Picasso.get().load(chatPartnerUser?.image_url).into(viewHolder.itemView.imageView_latestmessage_dp)
            }
        })


    }

    override fun getLayout(): Int {
        return R.layout.letsmessage_row
    }
}