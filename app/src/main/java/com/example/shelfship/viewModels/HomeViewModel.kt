package com.example.shelfship.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfship.models.Match
import com.example.shelfship.models.SessionData
import com.example.shelfship.utils.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _matchResults = MutableStateFlow<List<Match>>(emptyList())
    val matchResults: StateFlow<List<Match>> = _matchResults

    fun startMatchMaking() {
        viewModelScope.launch {
            FirebaseUtils.addToQueue()
            val users = FirebaseUtils.getTestData()
            Log.d("users debugging", users.toString())
            _matchResults.value = users
        }
    }

}