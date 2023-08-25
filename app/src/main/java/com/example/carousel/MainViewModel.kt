package com.example.carousel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale
import kotlin.random.Random

object MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel : ViewModel() {
    private val images = listOf(
        ImageItem(R.drawable.cat_1),
        ImageItem(R.drawable.cat_2),
        ImageItem(R.drawable.cat_3)
    )

    private var currentPage: Int = -1

    private val _vpImages = MutableLiveData<List<ImageItem>>()
    val vpImages: LiveData<List<ImageItem>> = _vpImages

    private val _listItems = MutableLiveData<List<ListItem>>()
    val listItems: LiveData<List<ListItem>> = _listItems

    private lateinit var listItemsDump: List<ListItem>
    private var searchQuery: String = ""

    init {
        _vpImages.postValue(getVPItems())
    }

    fun setCurrentPage(page: Int) {
        if (currentPage != page) {
            currentPage = page
            val listItems = getListItems()
            listItemsDump = listItems
            if (searchQuery.isNotEmpty()) {
                search(searchQuery)
            } else {
                _listItems.postValue(listItems)
            }
        }
    }

    fun search(searchQuery: String) {
        this.searchQuery = searchQuery
        val normalizedQuery = searchQuery.trim().lowercase(Locale.getDefault())
        val filteredListItems = listItemsDump.filter { listItem ->
            listItem.text.lowercase(Locale.getDefault()).contains(normalizedQuery)
        }
        _listItems.postValue(filteredListItems)
    }

    private fun getVPItems(): List<ImageItem> = List(VP_PAGE_COUNT) {
        ImageItem(
            resId = images.random().resId
        )
    }

    private fun getListItems(): List<ListItem> =
        List(Random.nextInt(LIST_SIZE_MIN, LIST_SIZE_MAX)) { _ ->
            ListItem(
                resId = images.random().resId,
                "Cat ${getRandomString()}"
            )
        }

    private fun getRandomString(length: Int = 5): String {
        val charset = "abcdefghiklmnopqrstuvwxyz"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    companion object {
        const val VP_PAGE_COUNT = 3

        const val LIST_SIZE_MIN = 15
        const val LIST_SIZE_MAX = 25
    }
}
