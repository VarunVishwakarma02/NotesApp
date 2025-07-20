package com.example.login_signup

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login_signup.databinding.ActivityAllNotesBinding
import com.example.login_signup.databinding.DialogUpdateNotesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.jvm.java

class AllNotes : AppCompatActivity(), NoteAdapter.OnItemClickListener {

    private val binding: ActivityAllNotesBinding by lazy {
        ActivityAllNotesBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = binding.noteRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize Firebase database Reference and auth
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        currentUser.let { user ->
            val noteReferece = databaseReference.child("users").child(user!!.uid).child("notes")
            noteReferece.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   val noteList = mutableListOf<Note>()
                    for(noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.let{
                            noteList.add(it)
                        }
                    }
                    noteList.reverse()
                    val adapter = NoteAdapter(noteList,this@AllNotes)
                    recyclerView.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }


    }

    override fun onDeleteClick(noteId: String) {
        val currentUser = auth.currentUser
        currentUser?.let{ user ->
            val noteReference = databaseReference.child("users").child(user.uid).child("notes")
            noteReference.child(noteId).removeValue()
        }
    }

    override fun onUpdateClick(noteId: String, newTitle: String, newDescription: String) {

        val dialogBinding = DialogUpdateNotesBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root)
            .setTitle("Update Note")
            .setPositiveButton("Update") { dialog,_ ->
                val updatedTitle = dialogBinding.updateNoteTitle.text.toString()
                val updatedDescription = dialogBinding.updateNoteDescription.text.toString()
                if (updatedTitle.isNotEmpty() && updatedDescription.isNotEmpty()) {
                    updateNoteDatabase(noteId, updatedTitle, updatedDescription)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialogBinding.updateNoteTitle.setText(newTitle)
        dialogBinding.updateNoteDescription.setText(newDescription)

        dialog.show()

    }
    private fun updateNoteDatabase(
        noteId: String,
        updatedTitle: String,
        updatedDescription: String
    ) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val noteReference = databaseReference.child("users").child(user.uid).child("notes").child(noteId)
            val updatedNote = Note(updatedTitle, updatedDescription, noteId)
            noteReference.setValue(updatedNote).addOnSuccessListener {
                // Optionally, you can show a success message or update the UI
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                // Handle failure
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


