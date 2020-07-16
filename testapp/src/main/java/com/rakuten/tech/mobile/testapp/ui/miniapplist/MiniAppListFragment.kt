package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppListFragmentBinding
import com.rakuten.tech.mobile.testapp.adapter.MiniAppList
import com.rakuten.tech.mobile.testapp.adapter.MiniAppListAdapter
import com.rakuten.tech.mobile.testapp.helper.MiniAppListStore
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppInputActivity
import kotlinx.android.synthetic.main.mini_app_list_fragment.*
import java.util.*
import kotlin.collections.ArrayList

class MiniAppListFragment : BaseFragment(), MiniAppList, SearchView.OnQueryTextListener {

    companion object {
        fun newInstance() = MiniAppListFragment()
    }

    private lateinit var viewModel: MiniAppListViewModel
    private lateinit var binding: MiniAppListFragmentBinding
    private lateinit var miniAppListAdapter: MiniAppListAdapter
    private lateinit var searchView: SearchView

    private var downloadedList: List<MiniAppInfo> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.mini_app_list_fragment,
            container,
            false
        )
        binding.fragment = this
        binding.rvMiniAppList.layoutManager = LinearLayoutManager(this.context)
        miniAppListAdapter = MiniAppListAdapter(ArrayList(), this)
        binding.rvMiniAppList.adapter = miniAppListAdapter
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
            executeLoadingList()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider.NewInstanceFactory().create(MiniAppListViewModel::class.java).apply {
            miniAppListData.observe(viewLifecycleOwner, Observer {
                swipeRefreshLayout.isRefreshing = false
                addMiniAppList(it)
                MiniAppListStore.instance.saveMiniAppList(it)
            })
            errorData.observe(viewLifecycleOwner, Observer {
                val list = MiniAppListStore.instance.getMiniAppList()
                if (list.isEmpty())
                    updateEmptyView(list)
                else
                    addMiniAppList(list)
                    swipeRefreshLayout.isRefreshing = false
            })
        }

        swipeRefreshLayout.setOnRefreshListener {
            executeLoadingList()
            resetSearchBox()
        }
    }

    private fun addMiniAppList(list: List<MiniAppInfo>) {
        downloadedList = list
        updateMiniAppListState(list)
    }

    private fun updateMiniAppListState(list: List<MiniAppInfo>) {
        miniAppListAdapter.addListWithSection(list)
        updateEmptyView(list)
    }

    private fun executeLoadingList() {
        viewModel.getMiniAppList()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        resetSearchBox()
        raceExecutor.run {
            context?.let { MiniAppDisplayActivity.start(it, miniAppInfo) }
        }
    }

    fun switchToInput() {
        raceExecutor.run { activity?.launchActivity<MiniAppInputActivity>() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        addSearchBox(menu)
    }

    private fun addSearchBox(menu: Menu) {
        val itemSearch = menu.findItem(R.id.action_search)
        searchView = itemSearch.actionView as SearchView
        searchView.queryHint = getString(R.string.menu_hint_search_miniapps)
        searchView.setOnQueryTextListener(this)

        // show search box in mini app list screen
        itemSearch.isVisible = true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        updateMiniAppListState(produceSearchResult(newText))
        return true
    }

    private fun produceSearchResult(newText: String?): List<MiniAppInfo> {
        return downloadedList.filter { info ->
            info.displayName.toLowerCase(Locale.ROOT)
                .contains(newText.toString().toLowerCase(Locale.ROOT))
        }
    }

    private fun resetSearchBox() {
        searchView.setQuery("", false)
        searchView.isIconified = true
        hideSoftKeyboard()
    }

    private fun updateEmptyView(collection: List<MiniAppInfo>) {
        if (collection.isEmpty())
            emptyView.visibility = View.VISIBLE
        else
            emptyView.visibility = View.GONE
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager: InputMethodManager =
            activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }
}
