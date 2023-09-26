package com.suchelin.android.feature.compose.detail

import android.content.Context
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.suchelin.android.R
import com.suchelin.android.base.BaseFragment
import com.suchelin.android.container.MainViewModel
import com.suchelin.android.databinding.FragmentDetailBinding
import com.suchelin.android.util.parcelable.StoreDataArgs

class DetailFragment :
    BaseFragment<FragmentDetailBinding, DetailViewModel>(R.layout.fragment_detail) {
    override val viewModel: DetailViewModel by viewModels()
    val sharedViewModel: MainViewModel by activityViewModels()
    private val args: DetailFragmentArgs by navArgs()
    override fun initView() {
        val storeInfo: StoreDataArgs = args.storeInfo
        Log.d("id","${storeInfo}")

        sharedViewModel.menuData.observe(viewLifecycleOwner){ menuData->
            menuData?.let {
                // rv에 데이터 값 넣기
                val currentStoreMenu = menuData[storeInfo.storeId]?.storeMenu
                val storeTel = menuData[storeInfo.storeId]?.tel
                binding.detailToTel.text = storeTel
                binding.detailStoreName.text = storeInfo.storeName
                Glide.with(this).load(storeInfo.imageUrl).centerCrop().into(binding.detailStoreImg)
                Log.d("Tag","${currentStoreMenu}, ${storeTel}")
                // rv에 넣을때 item 타입이 StoreMenuDetail 인지, String인지 확인해서 rv 돌리기
            }
        }

        binding.apply {

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            detailToTel.setOnClickListener {
                // 전화로 연결
            }
        }
    }
}