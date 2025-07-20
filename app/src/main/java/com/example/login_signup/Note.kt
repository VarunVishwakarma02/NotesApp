package com.example.login_signup

data class Note(val title: String, val description: String, val noteId: String) {
    // You can add additional properties or methods if needed
    constructor() : this("", "","") // Default constructor for Firebase compatibility
}
