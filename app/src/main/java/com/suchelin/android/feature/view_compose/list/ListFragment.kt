package com.suchelin.android.feature.view_compose.list

import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.suchelin.android.R
import com.suchelin.android.base.BaseFragment
import com.suchelin.android.container.MainViewModel
import com.suchelin.android.databinding.FragmentListBinding
import com.suchelin.android.feature.compose.ui.jamsil
import com.suchelin.android.util.StoreFilter
import com.suchelin.android.util.parcelable.StoreDataArgs
import com.suchelin.android.util.sendMail
import com.suchelin.android.util.toastMessageShort
import com.suchelin.domain.model.StoreData


private const val TAG = "LIST"

class ListFragment : BaseFragment<FragmentListBinding, MainViewModel>(R.layout.fragment_list) {

    override val viewModel: MainViewModel by activityViewModels()
    private lateinit var sendStoreInfo: NavDirections
    private lateinit var storeListReference: List<StoreData>
    private lateinit var randomDialog: RandomDialog
    override fun initView() {
        randomDialog = RandomDialog(requireContext()) { store ->
            sendStoreInfo =
                ListFragmentDirections.actionNavigationMainToNavigationDetail(
                    StoreDataArgs(
                        store.storeId,
                        store.storeDetailData.name,
                        store.storeDetailData.imageUrl,
                        store.storeDetailData.latitude,
                        store.storeDetailData.longitude
                    )
                )
            findNavController().navigate(sendStoreInfo)
        }

        viewModel.storeData.observe(viewLifecycleOwner) { storeList ->
            storeList?.let {
                storeListReference = it
                setComposeView(it, StoreFilter.ALL)
                binding.loading.isVisible = false
                binding.filterBar.isVisible = true
                if (!viewModel.random.value!!) {
                    val randomStore = storeList.random()
                    randomDialog.showDialog(randomStore)
                    viewModel.random.value = true
                }
            }
        }

        binding.apply {
            loading.isVisible = true
            contact.setOnClickListener {
                sendMail(TAG)
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val filteredList = mutableListOf<StoreData>()
                    for(search in storeListReference){
                        if(search.storeDetailData.name.contains(query!!,true)){
                            filteredList.add(search)
                        }
                    }
                    if (filteredList.size > 0 ) {
                        setComposeView(filteredList, StoreFilter.SEARCH)
                    }else{
                        toastMessageShort(getString(R.string.search_none))
                        setComposeView(filteredList, StoreFilter.ALL)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText!!.isEmpty()){
                        setComposeView(storeListReference, StoreFilter.ALL)
                    }
                    return true
                }
            })

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
            mainSchoolMeal.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_main_to_schoolFragment)
            }
        }
    }

    private fun setComposeView(storeList: List<StoreData>, filter: StoreFilter) {
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                StoreRecyclerView(storeList, filter)
            }
        }
    }

    @Composable
    fun StoreRecyclerView(storeDataList: List<StoreData>, filter: StoreFilter) {
        val stores by remember { mutableStateOf(storeDataList) }
        val filteredStores = when (filter) {
            StoreFilter.CAFE -> stores.filter { it.storeDetailData.type == StoreFilter.CAFE.type }
            StoreFilter.RESTAURANT -> stores.filter { it.storeDetailData.type == StoreFilter.RESTAURANT.type }
            StoreFilter.PUB -> stores.filter { it.storeDetailData.type == StoreFilter.PUB.type }
            StoreFilter.ALL -> stores
            StoreFilter.SEARCH-> storeDataList
            StoreFilter.RANK -> stores
        }

        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollInterop),
            contentPadding = PaddingValues(12.dp, 0.dp, 12.dp, 60.dp)
        ) {
            items(
                count = filteredStores.size,
                itemContent = { StoreListItem(filteredStores[it]) }
            )
        }
    }

    @Composable
    fun StoreListItem(store: StoreData) {
        Box(modifier =
        Modifier
            .clickable {
                sendStoreInfo =
                    ListFragmentDirections.actionNavigationMainToNavigationDetail(
                        StoreDataArgs(
                            store.storeId,
                            store.storeDetailData.name,
                            store.storeDetailData.imageUrl,
                            store.storeDetailData.latitude,
                            store.storeDetailData.longitude
                        )
                    )
                findNavController().navigate(sendStoreInfo)
            }
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(80.dp), shape = RoundedCornerShape(5.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(store.storeDetailData.imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "img",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(Modifier.padding(8.dp, 0.dp)) {
                    Text(
                        text = store.storeDetailData.name,
                        fontFamily = jamsil,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Box(modifier = Modifier.size(4.dp))
                    Text(
                        text = store.storeDetailData.detail,
                        fontFamily = jamsil,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}