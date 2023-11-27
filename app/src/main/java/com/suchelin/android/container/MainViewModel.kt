package com.suchelin.android.container

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.suchelin.android.base.BaseViewModel
import com.suchelin.android.util.docPostName
import com.suchelin.domain.model.PostData
import com.suchelin.domain.model.StoreData
import com.suchelin.domain.model.StoreDetail
import com.suchelin.domain.model.StoreMenuData
import com.suchelin.domain.model.StoreMenuDetail
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date

class MainViewModel : BaseViewModel() {
    private val _db = Firebase.firestore
    private val _isInit = MutableLiveData<Boolean>(false)
    val isInit: LiveData<Boolean> = _isInit
    val random = MutableLiveData<Boolean>(false)
    private val _storeList = mutableListOf<StoreData>()
    private val _storeData = MutableLiveData<List<StoreData>>()
    private val _menuList = hashMapOf<Int, StoreMenuData>()
    private val _menuData = MutableLiveData<HashMap<Int, StoreMenuData>>()
    val storeData: LiveData<List<StoreData>> = _storeData
    val menuData: LiveData<HashMap<Int, StoreMenuData>> = _menuData
    private val _postList = mutableListOf<PostData>()
    private val _postData = MutableLiveData<List<PostData>>()
    val postData: LiveData<List<PostData>> = _postData
    var isManager = false
    var totalStoreNumber: Int = 0

    fun checkManager(uid: String) {
        _db.collection("manager").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    for (id in document.data["id"] as List<String>) {
                        if (uid == id) isManager = true
                        Log.d("user","$isManager")
                    }
                }
                _menuData.value = _menuList
            }
            .addOnFailureListener { exception ->
                Timber.tag("TAG").w(exception, "Error getting documents.")
            }
    }

    private fun loadMenuData() {
        _db.collection("menu").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val menuDetailList = mutableListOf<Any>()
                    val isImage = document.data["image"] as Boolean
                    val tel = document.data["tel"] as String

                    if (!isImage) {
                        for (item in document.data["menu"] as List<HashMap<String, String>>) {
                            menuDetailList.add(
                                StoreMenuDetail(
                                    menuPrice = item["menuPrice"] as String,
                                    menuName = item["menuName"] as String
                                )
                            )
                        }
                    } else {
                        for (item in document.data["menu"] as List<String>) {
                            menuDetailList.add(
                                item
                            )
                        }
                    }

                    _menuList[document.id.toInt()] = StoreMenuData(
                        isImage,
                        menuDetailList,
                        tel
                    )
                }
                _menuData.value = _menuList
            }
            .addOnFailureListener { exception ->
                Timber.tag("TAG").w(exception, "Error getting documents.")
            }
    }

    private fun loadStoreData() {
        _db.collection("store")
            .orderBy("path", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    _storeList.add(
                        StoreData(
                            document.id.toInt(),
                            StoreDetail(
                                name = document.data["name"] as String,
                                detail = document.data["detail"] as String,
                                imageUrl = document.data["imageUrl"] as String,
                                latitude = document.data["latitude"] as Double,
                                longitude = document.data["longitude"] as Double,
                                menuImageUrl = document.data["menuImageUrl"] as String?,
                                type = document.data["type"] as String
                            )
                        )
                    )
                }
                _storeData.value = _storeList
                totalStoreNumber = _storeList.size
            }
            .addOnFailureListener { exception ->
                Timber.tag("TAG").w(exception, "Error getting documents.")
            }
    }

    private fun loadPostData() {
        _db.collection("suggest").document(docPostName.format(Date())).get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    val lines = result.data!!.asIterable().sortedByDescending { it.key }
                    for ((time, post) in lines) {
                        Timber.tag("TAG").d("$time : $post")
                        _postList.add(PostData(time, post.toString()))
                    }
                } else {
                    val initPostDate = SimpleDateFormat("yyyy년 MM월 dd일")
                    val initPost = "오늘은 " + initPostDate.format(Date()) + " 입니다"
                    val postTime = SimpleDateFormat("HH-mm-ss")
                    _postList.add(PostData("", post = initPost))
                    val docData = hashMapOf(
                        postTime.format(Date()) to initPost
                    )
                    _db.collection("suggest").document(docPostName.format(Date())).set(docData)
                }
                _postData.value = _postList
            }
    }

    fun postRefresh() {
        _postList.clear()
        loadPostData()
    }

    fun initData() {
        loadPostData()
        loadStoreData()
        loadMenuData()
        _isInit.value = true
    }

}