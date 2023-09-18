package com.suchelin.android.feature.view.map

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.suchelin.android.R
import com.suchelin.android.base.BaseFragment
import com.suchelin.android.container.MainViewModel
import com.suchelin.android.databinding.FragmentMapBinding
import com.suchelin.android.feature.view.mail.SendMailDialog
import com.suchelin.android.util.initMap
import com.suchelin.android.util.initMarker

const val TAG = "MAP"
private const val LOCATION_PERMISSION_REQUEST_CODE = 10002

class MapViewFragment : BaseFragment<FragmentMapBinding, MainViewModel>(R.layout.fragment_map),
    OnMapReadyCallback {
    override val viewModel: MainViewModel by activityViewModels()
    private lateinit var mapViewInstance: MapView
    private lateinit var naverMap: NaverMap

    override fun initView() {
        val sendMailDialog = SendMailDialog(requireContext())

        binding.apply {
            mapViewInstance = mapView
            mapViewInstance.getMapAsync { map ->
                naverMap = map
                naverMap.initMap()
                naverMap.initMarker(requireContext(), viewModel.storeData.value!!)
            }

            sendMail.setOnClickListener {
                sendMailDialog.showDialog()
            }
        }
    }

    override fun onMapReady(p0: NaverMap) {

    }


    override fun onStart() {
        super.onStart()
        mapViewInstance.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapViewInstance.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapViewInstance.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewInstance.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapViewInstance.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapViewInstance.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewInstance.onLowMemory()
    }
}
