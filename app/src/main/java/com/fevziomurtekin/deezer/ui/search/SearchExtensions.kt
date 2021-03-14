package com.fevziomurtekin.deezer.ui.search

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.fevziomurtekin.deezer.core.data.ApiResult
import com.fevziomurtekin.deezer.data.SearchData
import com.fevziomurtekin.deezer.entities.SearchEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import timber.log.Timber

private const val DEBOUNCE_TIME = 300L

@ExperimentalCoroutinesApi
fun EditText.textWatcher(): Flow<CharSequence?> =
    callbackFlow<CharSequence?> {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sendBlocking(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
             = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
             = Unit
        }
        this@textWatcher.addTextChangedListener(watcher)
        awaitClose { this@textWatcher.removeTextChangedListener(watcher) }
    }.buffer(Channel.CONFLATED)
        .distinctUntilChanged()
        .debounce(DEBOUNCE_TIME)

internal fun SearchViewModel.initSearchActionListener() {
    editorActionListener = TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            queryLiveData.value = v.text.toString()
            true
        } else false
    }
}

@BindingAdapter("adapterRecentSearch")
fun bindingRecentSeach(view: RecyclerView, results: LiveData<ApiResult<List<SearchEntity>>>) {
    Timber.d("binding recentData : ${results.value}")
    when(results.value){
        ApiResult.Loading, is ApiResult.Error-> Unit
        is ApiResult.Success -> {
            (view.adapter as RecentSearchAdapter)
                .addRecentSearch(
                    (((results.value as ApiResult.Success<List<SearchEntity>>).data)
                            as List<SearchEntity>))
        }
    }
}

@BindingAdapter("adapterSearchAlbum")
fun bindingSearchAlbum(view: RecyclerView, results: LiveData<ApiResult<Any>>) {
    when (results.value) {
        ApiResult.Loading, is ApiResult.Error -> {/* Nothing */ }
        is ApiResult.Success -> {
            Timber.d("adapterSearchAlbum")
            (view.adapter as SearchAlbumAdapter)
                .addAlbumSearch(
                    ((results.value) as ApiResult.Success<List<SearchData>>).data)
        }
    }
}

@BindingAdapter("onEditorActionListener")
fun bindOnEditorActionListener(editText: EditText, editorActionListener: TextView.OnEditorActionListener) {
    editText.setOnEditorActionListener(editorActionListener)
}

