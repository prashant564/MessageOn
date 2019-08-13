package com.example.themessenger

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.themessenger.letsmessage.Companion.currentUser
import com.example.themessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class NewMessage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        fetchUsers()

    }

    companion object{
        val USER_KEY = "USER_KEY"
    }


    private fun fetchUsers(){

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{


            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach{
                    val user = it.getValue(User::class.java)

                    if(user!=null){

                        if(user.username != currentUser?.username){

                            adapter.add(UserItem(user))
                        }


                    }

                }

                adapter.setOnItemClickListener {item, view ->

                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLog::class.java)
                    intent.putExtra(USER_KEY,userItem.user)

                    startActivity(intent)

                    finish()
                }

                user_list_recyclerView.adapter = adapter

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.username_newmessage.text = user.username
        Log.d("MainActivity",user.image_url)
        Picasso.get().load(user.image_url).into(viewHolder.itemView.imageView_newmessage)


    }
    override fun getLayout(): Int {
        return R.layout.user_row_newmessage
    }
}