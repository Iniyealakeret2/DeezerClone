package com.fevziomurtekin.deezer.ui.search

import android.accounts.NetworkErrorException
import android.widget.TextView
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.fevziomurtekin.deezer.core.data.ApiResult
import com.fevziomurtekin.deezer.entities.SearchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class SearchViewModel @ViewModelInject constructor(
    private val repository: SearchRepository
): ViewModel(){

    var queryLiveData: MutableLiveData<String> = MutableLiveData()
    var result: LiveData<ApiResult<Any>> = MutableLiveData()
    var recentSearch:LiveData<ApiResult<List<SearchEntity>>> = MutableLiveData()
    var isNetworkError = MutableLiveData(false)
    lateinit var editorActionListener:TextView.OnEditorActionListener

    init {
        initSearchActionListener()
    }

    fun fetchingRecentSearch(){
        viewModelScope.launch {
            recentSearch = repository.fetchRecentSearch()
                    .asLiveData(viewModelScope.coroutineContext+ Dispatchers.Default)

        }
    }

    /**
     * Each query update enter to switchmap
     */
    fun fetchSearch(query: String){
        viewModelScope.launch {
                try{
                    result =  repository.fetchSearch(query)
                        .asLiveData(viewModelScope.coroutineContext + Dispatchers.Main)
                }catch (e: NetworkErrorException){
                    isNetworkError.value = true
                    Timber.e(e)
                }
        }
    }
}
