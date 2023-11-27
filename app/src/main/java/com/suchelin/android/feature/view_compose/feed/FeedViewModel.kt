package com.suchelin.android.feature.view_compose.feed

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.suchelin.android.base.BaseViewModel
import com.suchelin.android.util.docPostName
import com.suchelin.android.util.room.dao.FeedDao
import com.suchelin.android.util.room.dto.FeedData
import com.suchelin.android.util.todayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class FeedViewModel : BaseViewModel() {
    private val db = Firebase.firestore
    private val _isLimited = MutableLiveData<Boolean>(false)
    val isLimited: LiveData<Boolean> = _isLimited

    private suspend fun checkFeedWriteState(feedDao: FeedDao) {
        withContext(Dispatchers.IO) {
            val todayFeed = feedDao.getAll()
            val today = todayDate()
            var feedWrite = false
            todayFeed.forEach { dates ->
                if (dates.date == today) {
                    feedWrite = dates.feed ?: false
                }
            }
            when (feedWrite) {
                false -> {
                    feedDao.insert(FeedData(today, true))
                }

                true -> {
                    withContext(Dispatchers.Main) {
                        _isLimited.value = true
                    }
                }
            }
            Log.d("DbFeed", "${todayFeed}")
        }
    }

    fun initFeedWriteState(feedDao: FeedDao): Job{
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val todayFeed = feedDao.getAll()
                val today = todayDate()
                var feedWrite = false
                todayFeed.forEach { dates ->
                    if (dates.date == today) {
                        feedWrite = dates.feed ?: false
                    }
                }
                if(feedWrite){
                    withContext(Dispatchers.Main) {
                        _isLimited.value = true
                    }
                }
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    fun deletePost(postTime: String){
        val deletePostData = hashMapOf<String, Any>(
            postTime to FieldValue.delete(),
        )
        db.collection("suggest").document(docPostName.format(Date()))
            .update(deletePostData)
    }

    @SuppressLint("SimpleDateFormat")
    fun postData(
        post: String, feedDao: FeedDao
    ): Job {
        val job = viewModelScope.launch {
            checkFeedWriteState(feedDao)
            if (!_isLimited.value!!) {
                val postTime = SimpleDateFormat("HH-mm-ss")
                val docData = hashMapOf(
                    postTime.format(Date()) to post
                )
                db.collection("suggest").document(docPostName.format(Date()))
                    .update(postTime.format(Date()), post)
                    .addOnSuccessListener {
                        _isLimited.value = true
                    }
                    .addOnFailureListener { _ ->
                        db.collection("suggest").document(docPostName.format(Date()))
                            .set(docData)
                    }
            }
        }
        return job
    }
}