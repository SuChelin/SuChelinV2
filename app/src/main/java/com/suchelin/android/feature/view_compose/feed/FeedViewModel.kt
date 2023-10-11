package com.suchelin.android.feature.view_compose.feed

import android.annotation.SuppressLint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.suchelin.android.base.BaseViewModel
import com.suchelin.android.util.docPostName
import java.text.SimpleDateFormat
import java.util.Date

class FeedViewModel: BaseViewModel() {
    private val db = Firebase.firestore

    @SuppressLint("SimpleDateFormat")
    fun postData(
        post: String,
    ) {
        val postTime = SimpleDateFormat("HH-mm-ss")
        val docData = hashMapOf(
            postTime.format(Date()) to post
        )
        db.collection("suggest").document(docPostName.format(Date()))
            .update(postTime.format(Date()), post)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                db.collection("suggest").document(docPostName.format(Date())).set(docData)
            }
    }
}