package com.fevziomurtekin.deezer.ui.search

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fevziomurtekin.deezer.R
import com.fevziomurtekin.deezer.core.extensions.UIExtensions
import com.fevziomurtekin.deezer.core.ui.DataBindingFragment
import com.fevziomurtekin.deezer.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SearchFragment: DataBindingFragment() {

    @VisibleForTesting
    val viewModel: SearchViewModel by viewModels()
    lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = binding(inflater, R.layout.fragment_search, container)
        return binding.root
    }

    override fun getSafeArgs() = Unit

    override fun initBinding() {

        binding.apply {
            lifecycleOwner = this@SearchFragment
            recentAdapter = RecentSearchAdapter(object : RecentSearchAdapter.RecentSearchListener{
                override fun recentSearchListener(query: String) {
                    aetSearch.text = Editable.Factory.getInstance().newEditable(query)
                    binding.aetSearch.text = Editable.Factory.getInstance().newEditable(query)
                }
            })
            searchAdapter = SearchAlbumAdapter()
            vm = viewModel
        }

        lifecycleScope.launchWhenCreated {
            binding.aetSearch.textWatcher().collect {
                viewModel.fetchSearch(it.toString())
            }
        }

    }

    override fun setListeners() = Unit

    override fun observeLiveData() {
        viewModel.fetchingRecentSearch()

        viewModel.isNetworkError.observe(viewLifecycleOwner,{
            if(it){
                UIExtensions
                    .showSnackBar(
                        this@SearchFragment.cl_search,
                        this@SearchFragment.getString(R.string.network_error))
            }
        })
    }
}
