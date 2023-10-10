package com.suchelin.android.feature.view_compose.vote

import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suchelin.android.R
import com.suchelin.android.base.BaseFragment
import com.suchelin.android.container.MainViewModel
import com.suchelin.android.databinding.FragmentVoteBinding
import com.suchelin.android.feature.compose.ui.jamsil
import com.suchelin.android.feature.view_compose.list.StoreFilter
import com.suchelin.android.util.parcelable.StoreDataArgs
import com.suchelin.android.util.sendMail
import com.suchelin.domain.model.StoreData

class VoteFragment : BaseFragment<FragmentVoteBinding, VoteViewModel>(R.layout.fragment_vote) {
    override val viewModel: VoteViewModel by viewModels()
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val TAG = "VOTE"
    private lateinit var storeListReference: List<StoreData>
    private lateinit var sendStoreInfo: NavDirections


    override fun initView() {
        viewModel.readRTDB()

        sharedViewModel.storeData.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                storeListReference = it
            }
        }

        viewModel.rtData.observe(viewLifecycleOwner) { votedData ->
            votedData?.let {
                binding.loading.isVisible = false
                if (::storeListReference.isInitialized) {
                    setComposeView(storeListReference, StoreFilter.ALL)
                }
            }
        }

        binding.apply {
            binding.loading.isVisible = true

            contact.setOnClickListener {
                sendMail(TAG)
            }

            val filterButtons = mapOf(
                all to StoreFilter.ALL,
                restaurant to StoreFilter.RESTAURANT,
                pub to StoreFilter.PUB,
                cafe to StoreFilter.CAFE
            )

            filterButtons.forEach { (button, filter) ->
                button.setOnClickListener {
                    setComposeView(storeListReference, filter)
                }
            }
        }
    }

    private fun setComposeView(storeList: List<StoreData>, filter: StoreFilter) {
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                VoteGrid(storeList, filter)
            }
        }
    }

    @Composable
    fun VoteGrid(storeDataList: List<StoreData>, filter: StoreFilter) {
        val stores by remember { mutableStateOf(storeDataList) }
        val filteredStores = when (filter) {
            StoreFilter.CAFE -> stores.filter { it.storeDetailData.type == "cafe" }
            StoreFilter.RESTAURANT -> stores.filter { it.storeDetailData.type == "restaurant" }
            StoreFilter.PUB -> stores.filter { it.storeDetailData.type == "pub" }
            StoreFilter.ALL -> stores
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.nestedScroll(nestedScrollInterop),
        ) {
            items(count = filteredStores.size,
                itemContent = {
                    StoreListItem(filteredStores[it])
                }
            )
        }
    }

    @Composable
    fun StoreListItem(store: StoreData) {
        Box(
            modifier =
            Modifier
                .padding(8.dp, 0.dp, 8.dp, 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(80.dp)
                        .fillMaxWidth(), shape = RoundedCornerShape(5.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(store.storeDetailData.imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "img",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                sendStoreInfo =
                                    VoteFragmentDirections.actionNavigationVoteToNavigationDetail(
                                        StoreDataArgs(
                                            store.storeId,
                                            store.storeDetailData.name,
                                            store.storeDetailData.imageUrl
                                        )
                                    )
                                Toast
                                    .makeText(
                                        context,
                                        "${store.storeId}: ${store.storeDetailData.name}\n${
                                            sharedViewModel.menuData.value?.get(store.storeId)
                                        }",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                                findNavController().navigate(sendStoreInfo)
                            }
                    )
                }
                Column(Modifier.padding(0.dp, 8.dp)) {
                    Text(
                        text = store.storeDetailData.name,
                        fontFamily = jamsil,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(24.dp),
                        maxLines = 1
                    )
                }
                Row(modifier = Modifier.align(CenterHorizontally)) {
                    Text(
                        text = if (viewModel.rtData.value!![store.storeId.toString()] != null) {
                            viewModel.rtData.value!![store.storeId.toString()].toString()
                        } else {
                            "0"
                        },
                        fontFamily = jamsil,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(CenterVertically)
                            .padding(end = 8.dp)
                    )
                    vote(resources.getDrawable(R.drawable.heart, null), store.storeId.toString())
                }
            }
        }
    }

    @Composable
    fun vote(img: Drawable, path: String) {
        // 이미지 클릭 핸들러
        val onClick = {
            viewModel.addVote(path)
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.heart),
            contentDescription = "img",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(24.dp)
                .width(24.dp)
                .clickable(onClick = onClick)
        )
    }
}