package com.example.login_signup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login_signup.databinding.ActivityAddNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNote : AppCompatActivity() {

    private val binding: ActivityAddNoteBinding by lazy {
        ActivityAddNoteBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.saveNoteButton.setOnClickListener {
            val noteText = binding.editTextTitle.text.toString()
            val descriptionText = binding.editTextDescription.text.toString()

            if (noteText.isEmpty() || descriptionText.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else {
                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    val noteKey = databaseReference.child("users").child(user.uid).child("notes").push()
                    val noteItem = Note(noteText, descriptionText, noteKey.key ?: "")
                    databaseReference.child("users").child(user.uid).child("notes").child(noteKey.key!!).setValue(noteItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save note: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}