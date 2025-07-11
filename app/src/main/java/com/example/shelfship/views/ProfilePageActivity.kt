package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.shelfship.R
import com.example.shelfship.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var aboutMeInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private var isEditing = false

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_scaffold)
        composeView.setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
        }

        profileImageView = findViewById(R.id.imageSlot1)
        aboutMeInput = findViewById(R.id.aboutMeInput)
        locationInput = findViewById(R.id.locationInput)
        ageInput = findViewById(R.id.ageInput)
        interestsInput = findViewById(R.id.interestsInput)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)

        setEditMode(false)

        btnEditProfile.setOnClickListener {
            if (isEditing) {
                saveProfile()
                setEditMode(false)
                Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
            } else {
                setEditMode(true)
            }
        }

        btnLogout.setOnClickListener {
            FirebaseUtils.logout()
            finishAffinity()
        }

        loadUserProfileFromFirestore()
    }

    private fun loadUserProfileFromFirestore() {
        val uid = currentUserId
        if (uid == null) {
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profilePictureUrl = document.getString("profilePictureUrl")
                    val aboutMe = document.getString("aboutMe") ?: ""
                    val location = document.getString("location") ?: ""
                    val age = document.getLong("age")?.toString() ?: ""
                    val interests = document.getString("interests") ?: ""

                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePictureUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }

                    aboutMeInput.setText(aboutMe)
                    locationInput.setText(location)
                    ageInput.setText(age)
                    interestsInput.setText(interests)
                }
            }
            .addOnFailureListener {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
            }
    }

    private fun setEditMode(enabled: Boolean) {
        isEditing = enabled
        aboutMeInput.isEnabled = enabled
        locationInput.isEnabled = enabled
        ageInput.isEnabled = enabled
        interestsInput.isEnabled = enabled
        btnEditProfile.text = if (enabled) "Save Profile" else "Edit Profile"
    }

    private fun saveProfile() {
        val uid = currentUserId ?: return

        val aboutMe = aboutMeInput.text.toString()
        val location = locationInput.text.toString()
        val ageText = ageInput.text.toString()
        val age = ageText.toLongOrNull() ?: 0L
        val interests = interestsInput.text.toString()

        val userUpdates = hashMapOf<String, Any>(
            "aboutMe" to aboutMe,
            "location" to location,
            "age" to age,
            "interests" to interests
        )

        firestore.collection("users")
            .document(uid)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
