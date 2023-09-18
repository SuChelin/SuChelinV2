package com.suchelin.android.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.suchelin.domain.model.StoreData
import com.suchelin.domain.model.StoreDetail

const val MARKER_ICON_HEIGHT = 60
const val MARKER_ICON_WEIGHT = 60
const val MAIN_GATE = 0

fun NaverMap.initMap() {
    apply {
        uiSettings.apply {
            isCompassEnabled = true
            isScaleBarEnabled = false
            isZoomControlEnabled = false
            isLocationButtonEnabled = false
        }
        cameraPosition = CameraPosition(
            // 초기 위치 정문
            LatLng(37.214185, 126.978792),
            18.0
        )
    }
}

fun NaverMap.initMarker(context: Context, storeList: List<StoreData>) {
    apply {
        //        val resource = R.drawable.premiumiconlocation1
//        val markerIconStart = OverlayImage.fromResource(R.drawable.home)
//        val markerIcon = OverlayImage.fromResource(resource)
        val storeDataList = mutableListOf(
            StoreData(MAIN_GATE, StoreDetail("수원대학교 정문", "", "", 37.214185, 126.978792, null, "default"))
        )
        val markerList = mutableListOf<Marker>()
        val infoWindowInstance = InfoWindow()

        markerList.add(Marker().apply {
            position = LatLng(37.214185, 126.978792)
//            icon = markerIconStart
            map = this@initMarker
            height = MARKER_ICON_HEIGHT
            width = MARKER_ICON_WEIGHT
        })

        markerList[MAIN_GATE].setOnClickListener {
            moveMarker(MAIN_GATE, storeDataList)
            Log.d("MAP", storeDataList[MAIN_GATE].storeDetailData.name)
            infoWindowInstance.setInfoWindow(context, markerList, MAIN_GATE, storeDataList[MAIN_GATE].storeDetailData.name)
            true
        }

        storeList.forEachIndexed { _, data ->
            val marker = Marker().apply {
                position = LatLng(data.storeDetailData.latitude, data.storeDetailData.longitude)
//                icon = markerIcon
                map = this@initMarker
                height = MARKER_ICON_HEIGHT
                width = MARKER_ICON_WEIGHT
            }

            marker.setOnClickListener {
                moveMarker(data.storeId, storeDataList)
                Log.d("MAP", storeDataList[data.storeId].storeDetailData.name)
                infoWindowInstance.setInfoWindow(context, markerList, data.storeId, data.storeDetailData.name)
                true
            }
            storeDataList.add(data.storeId, data)
            markerList.add(marker)
        }
    }
}

fun NaverMap.moveMarker(id: Int, storeDataList: List<StoreData>) {
    apply {
        Log.d("MAP", "${storeDataList} , ${storeDataList[id]}")
        moveCamera(
            CameraUpdate.scrollTo(
                LatLng(
                    storeDataList[id].storeDetailData.latitude,
                    storeDataList[id].storeDetailData.longitude
                )
            ).animate(CameraAnimation.Fly)
        )
    }
}

private fun InfoWindow.setInfoWindow(context: Context, markerList: List<Marker>, markerIndex: Int, infoString: String) {
    adapter = object : InfoWindow.DefaultTextAdapter(context) {
        override fun getText(infoWindow: InfoWindow): CharSequence {
            return infoString
        }
    }
    open(markerList[markerIndex])
}

fun setStoreData(
    name: String,
    mainMenu: String,
    imageUrl: String,
    menuImageUrl: String? = null,
    latitude: Double,
    longitude: Double,
    type: String,
) {
    val db = Firebase.firestore
    var path: Int = 0
    db.collection("store").get().addOnSuccessListener { result ->
        path = result.size() + 1
    }
    val docData = hashMapOf(
        "name" to name,
        "detail" to mainMenu,
        // imageUrl, menuImageUrl -> Copy Image Address
        "imageUrl" to imageUrl,
        "latitude" to latitude, // 30~
        "longitude" to longitude, // 120~
        "menuImageUrl" to menuImageUrl,
        "type" to type // restaurant or cafe
    )
    // 3까지 입력 됐음
    db.collection("store").document(path.toString())
        .set(docData)
        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully written!") }
        .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
}