package com.example.themessenger

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.themessenger.NewMessage.Companion.USER_KEY
import com.example.themessenger.baseApp.Companion.channel_id
import com.example.themessenger.models.Messages
import com.example.themessenger.models.User
import com.example.themessenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_letsmessage.*
import kotlinx.android.synthetic.main.letsmessage_row.view.*

class letsmessage : AppCompatActivity() {

    var username: String = ""
    var chatPartnerUser: User?= null

    companion object{
        var currentUser: User? = null
    }


    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letsmessage)

        recyclerview_home_page.adapter = adapter
        recyclerview_home_page.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener(object: OnItemClickListener{

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemClick(item: Item<*>, view: View) {


                view.setBackgroundColor(getColor(R.color.button))
                val intent = Intent(view.context,ChatLog::class.java)
                val row = item as LatestMessageRow
                intent.putExtra(USER_KEY, row.chatPartnerUser)
                startActivity(intent)

            }

        })
        fetchCurrentUser()
        runAnimation(recyclerview_home_page)
        listenForLatestMessages()
        verifyUserIsLoggedIn()
    }

    private fun fetchCurrentUser(){

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.keepSynced(true)

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                currentUser = p0.getValue(User::class.java)
                Log.d("letsmessage","current user ${currentUser?.username}")

            }
        })

    }

    val latestMessagesMap = HashMap<String, Messages>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){

        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId")
        ref.keepSynced(true)

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                val messages = p0.getValue(Messages::class.java) ?: return
                adapter.add(LatestMessageRow(messages))
                latestMessagesMap[p0.key!!] = messages

//                displayNotifications(messages)

                refreshRecyclerViewMessages()

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val messages = p0.getValue(Messages::class.java) ?: return
                adapter.add(LatestMessageRow(messages))
                latestMessagesMap[p0.key!!] = messages

//                displayNotifications(messages)
                refreshRecyclerViewMessages()



            }



//            private fun displayNotifications(messages: Messages) {
//                val chatPartnerId: String
//                if (messages.fromId == FirebaseAuth.getInstance().uid) {
//                    chatPartnerId = messages.toId
//                } else {
//                    chatPartnerId = messages.fromId
//                }
//
//                val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
//                ref.keepSynced(true)
//                ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//                        chatPartnerUser = p0.getValue(User::class.java)
//                        username = chatPartnerUser!!.username
//                    }
//                })
//
//                if (currentUser!!.uid != chatPartnerId) {
//
//
//                    val intent = Intent(this@letsmessage, letsmessage::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//                    val pendingIntent: PendingIntent = PendingIntent.getActivity(this@letsmessage, 0, intent, 0)
//
//                    val builder = NotificationCompat.Builder(this@letsmessage, channel_id)
//                        .setSmallIcon(R.drawable.ic_logo)
//                        .setContentTitle("${username}")
//                        .setContentText("${messages.text}")
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                        // Set the intent that will fire when the user taps the notification
//                        .setContentIntent(pendingIntent)
//                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                        .setAutoCancel(true)
//
//                    with(NotificationManagerCompat.from(this@letsmessage)) {
//                        // notificationId is a unique int for each notification that you must define
//                        notify(1, builder.build())
//                    }
//                }
//
//            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun runAnimation(recyclerView: RecyclerView){
        val animation = AnimationUtils.loadLayoutAnimation(this,R.anim.layout_from_right)
        recyclerView.layoutAnimation = animation
    }


    private fun verifyUserIsLoggedIn(){

        val uid = FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
}

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){

            R.id.menu_new_message -> {

                val intent = Intent(this, NewMessage::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
               intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}


