@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.carousel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carousel.ui.theme.CarouselTheme
import com.example.carousel.ui.theme.Purple40
import com.example.carousel.ui.theme.PurpleGrey40
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarouselTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}


@Composable
fun MyApp() {
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory)

    val vpItems by viewModel.vpImages.observeAsState(emptyList())
    val listItems by viewModel.listItems.observeAsState(emptyList())

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ImagesViewPager(images = vpItems) { currentPage ->
                viewModel.setCurrentPage(currentPage)
            }
        }

        stickyHeader {
            SearchView(viewModel)
        }

        if (listItems.isEmpty()) {
            item {
                EmptyListPlaceholder()
            }
        } else {
            itemsIndexed(listItems) { _, item ->
                Spacer(modifier = Modifier.height(4.dp))
                ListItemView(item = item)
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = Color.LightGray)
            }
        }
    }
}

@Composable
fun ImagesViewPager(images: List<ImageItem>, onPageChanged: (Int) -> Unit) {
    val pagerState = rememberPagerState()

    ViewPager(
        images = images,
        pagerState = pagerState,
        onPageChanged = onPageChanged
    )

    HorizontalPagerIndicator(
        pageCount = images.size,
        currentPage = pagerState.currentPage,
        targetPage = pagerState.targetPage,
        currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
    )
}

@Composable
fun ViewPager(
    images: List<ImageItem>,
    pagerState: PagerState,
    onPageChanged: (Int) -> Unit
) {
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onPageChanged(page) }
    }

    HorizontalPager(
        pageCount = images.size,
        state = pagerState,
        contentPadding = PaddingValues(start = 8.dp),
        pageSize = object : PageSize {
            override fun Density.calculateMainAxisPageSize(
                availableSpace: Int,
                pageSpacing: Int
            ): Int = (availableSpace * 0.9).toInt()
        }
    ) { page ->
        val imageResId = images[page].resId
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .height(200.dp)
        )
    }
}

@Composable
private fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    targetPage: Int,
    currentPageOffsetFraction: Float,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.DarkGray,
    unselectedIndicatorSize: Dp = 10.dp,
    selectedIndicatorSize: Dp = 8.dp,
    indicatorPadding: Dp = 3.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        repeat(pageCount) { page ->
            // Calculate color and size of the indicator
            val (indicatorColorForPage, indicatorSize) =
                if (currentPage == page || targetPage == page) {
                    val pageOffset =
                        ((currentPage - page) + currentPageOffsetFraction).absoluteValue
                    val offsetPercentage = 1f - pageOffset.coerceIn(0f, 1f)

                    val size = unselectedIndicatorSize +
                            ((selectedIndicatorSize - unselectedIndicatorSize) * offsetPercentage)

                    indicatorColor.copy(alpha = offsetPercentage) to size
                } else {
                    indicatorColor.copy(alpha = 0.1f) to unselectedIndicatorSize
                }

            // Draw indicator
            Box(
                modifier = Modifier
                    .padding(
                        // Apply horizontal padding, so that each indicator has the same width
                        horizontal = ((selectedIndicatorSize + indicatorPadding * 2) - indicatorSize) / 2,
                        vertical = indicatorSize / 4
                    )
                    .clip(CircleShape)
                    .background(indicatorColorForPage)
                    .size(indicatorSize)
            )
        }
    }
}

@Composable
fun EmptyListPlaceholder() {
    Text(
        text = stringResource(R.string.empty_list_placeholder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun ListItemView(item: ListItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(id = item.resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun SearchView(viewModel: MainViewModel) {
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(LightGray)
    ) {
        OutlinedTextField(
            leadingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    tint = MaterialTheme.colorScheme.outline,
                    contentDescription = null
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Purple40,
                unfocusedBorderColor = PurpleGrey40,
                focusedLabelColor = Purple40,
                unfocusedLabelColor = White,
                cursorColor = Purple40,
                textColor = Purple40
            ),
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.search(searchText)
            },
            label = { Text(text = stringResource(R.string.search)) },
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CarouselTheme {
        MyApp()
    }
}
