package com.example.shelfship.views

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R

class ProfilePageActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageSlots: Array<ImageView?>
    private var currentSlotIndex = 0

    private lateinit var sharedPrefs: SharedPreferences

    private lateinit var aboutMeInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var btnEditProfile: Button

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.compose_scaffold)
        composeView.setContent {
            LargeDropdownMenuScaffold(context = this, screenTitle = "")
        }

        sharedPrefs = getSharedPreferences("userProfile", MODE_PRIVATE)



        imageSlots = arrayOfNulls(6)
        imageSlots[0] = findViewById(R.id.imageSlot1)
        imageSlots[1] = findViewById(R.id.imageSlot2)
        imageSlots[2] = findViewById(R.id.imageSlot3)
        imageSlots[3] = findViewById(R.id.imageSlot4)
        imageSlots[4] = findViewById(R.id.imageSlot5)
        imageSlots[5] = findViewById(R.id.imageSlot6)

        for (i in imageSlots.indices) {
            imageSlots[i]?.setOnClickListener {
                if (isEditing) {
                    currentSlotIndex = i
                    openGallery()
                } else {
                    Toast.makeText(this, "Enable edit mode to change images", Toast.LENGTH_SHORT).show()
                }
            }
        }

        aboutMeInput = findViewById(R.id.aboutMeInput)
        locationInput = findViewById(R.id.locationInput)
        ageInput = findViewById(R.id.ageInput)
        interestsInput = findViewById(R.id.interestsInput)
        btnEditProfile = findViewById(R.id.btnEditProfile)

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

        loadProfile()
    }

    private fun setEditMode(enabled: Boolean) {
        isEditing = enabled
        aboutMeInput.isEnabled = enabled
        locationInput.isEnabled = enabled
        ageInput.isEnabled = enabled
        interestsInput.isEnabled = enabled
        btnEditProfile.text = if (enabled) "Save Profile" else "Edit Profile"

        if (enabled) {
            Toast.makeText(this, "Tap images to change them", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile() {
        val editor = sharedPrefs.edit()
        editor.putString("aboutMe", aboutMeInput.text.toString())
        editor.putString("location", locationInput.text.toString())
        editor.putString("age", ageInput.text.toString())
        editor.putString("interests", interestsInput.text.toString())

        for (i in imageSlots.indices) {
            val uriString = imageSlots[i]?.tag as? String ?: ""
            editor.putString("image$i", uriString)
        }
        editor.apply()
    }

    private fun loadProfile() {
        aboutMeInput.setText(sharedPrefs.getString("aboutMe", ""))
        locationInput.setText(sharedPrefs.getString("location", ""))
        ageInput.setText(sharedPrefs.getString("age", ""))
        interestsInput.setText(sharedPrefs.getString("interests", ""))

        for (i in imageSlots.indices) {
            val uriString = sharedPrefs.getString("image$i", null)
            if (!uriString.isNullOrEmpty()) {
                val uri = Uri.parse(uriString)
                imageSlots[i]?.setImageURI(uri)
                imageSlots[i]?.tag = uriString
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            val imageUri = data.data!!
            imageSlots[currentSlotIndex]?.setImageURI(imageUri)
            imageSlots[currentSlotIndex]?.tag = imageUri.toString()
        }
    }
}