package com.suchelin.android.feature.view_compose.feed

import android.view.View
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.suchelin.android.R
import com.suchelin.android.base.BaseFragment
import com.suchelin.android.container.MainViewModel
import com.suchelin.android.databinding.FragmentSuggestBinding
import com.suchelin.android.feature.compose.ui.AppTheme
import com.suchelin.android.feature.compose.ui.jamsil
import com.suchelin.android.util.AdManager
import com.suchelin.android.util.room.FeedDbInstance
import com.suchelin.android.util.sendMail
import com.suchelin.android.util.toastMessageShort
import com.suchelin.domain.model.PostData
import kotlinx.coroutines.launch

private const val TAG = "FEED"

class FeedFragment :
    BaseFragment<FragmentSuggestBinding, FeedViewModel>(R.layout.fragment_suggest) {
    override val viewModel: FeedViewModel by viewModels()
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun initView() {
        binding.adView.loadAd(AdManager.createAdRequest())
        initFeedWriteLimit()

        viewModel.isLimited.observe(viewLifecycleOwner) { isLimit ->
            if (isLimit) {
                binding.clEdit.visibility = View.GONE
            }
        }
        sharedViewModel.postData.observe(viewLifecycleOwner) { postList ->
            postList?.let {
                binding.loading.isVisible = false
                binding.composeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        AppTheme {
                            PostRecyclerView(postList)
                        }
                    }
                }
            }
        }

        val postAlert = PostAlertDialog(requireContext())
        binding.apply {
            binding.loading.isVisible = true
            contact.setOnClickListener {
                sendMail(TAG)
            }
            btnSuggest.setOnClickListener {
                etSuggestPost.text?.let { post ->
                    // post전에 욕설 필터링이 필요할 것 같음
                    if (post.length < 10) {
                        toastMessageShort(getString(R.string.post_min_len))
                    } else {
                        postAlert.showDialog()
                        postAlert.alertDialog.apply {
                            setOnCancelListener {
                                loading.isVisible = true
                                lifecycleScope.launch {
                                    viewModel.postData(
                                        post.toString(),
                                        FeedDbInstance.getDatabase(requireContext()).feedDao()
                                    ).join()
                                    sharedViewModel.postRefresh()
                                    loading.isVisible = false
                                    etSuggestPost.text.clear()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PostRecyclerView(postDataList: List<PostData>) {
        val posts by remember {
            mutableStateOf(
                postDataList
            )
        }

        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollInterop),
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 60.dp),
        ) {
            items(
                count = posts.size,
                itemContent = { PostListItem(posts[it]) }
            )
        }
    }

    @Composable
    fun PostListItem(post: PostData) {
        Card(
            shape = RoundedCornerShape(6.dp),
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 2.dp, 0.dp, 4.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (sharedViewModel.isManager) {
                                viewModel.deletePost(post.date)
                                sharedViewModel.postRefresh()
                            }
                        }
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 4.dp)
            ) {
                Column(Modifier.padding(8.dp, 2.dp)) {
                    Text(
                        text = post.post,
                        fontFamily = jamsil,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 4.dp, 0.dp, 0.dp)
                    ) {
                        Text(
                            text = post.date,
                            fontFamily = jamsil,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    private fun initFeedWriteLimit() {
        lifecycleScope.launch {
            viewModel.initFeedWriteState(FeedDbInstance.getDatabase(requireContext()).feedDao())
                .join()
            if (viewModel.isLimited.value!!) {
                binding.clEdit.visibility = View.GONE
                toastMessageShort(getString(R.string.feedLimit))
            }
        }
    }
}