package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppListFragmentBinding
import com.rakuten.tech.mobile.testapp.adapter.MiniAppListAdapter
import com.rakuten.tech.mobile.testapp.adapter.MiniAppListener
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel.MiniAppListViewModel
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel.MiniAppListViewModelFactory
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.OnSearchListener
import java.util.*


class MiniAppListFragment : BaseFragment(), MiniAppListener, OnSearchListener,
    SearchView.OnQueryTextListener, PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    companion object {
        val TAG = MiniAppListFragment::class.java.canonicalName
        fun newInstance(): MiniAppListFragment = MiniAppListFragment()
    }

    private lateinit var viewModel: MiniAppListViewModel
    private lateinit var binding: MiniAppListFragmentBinding
    private lateinit var miniAppListAdapter: MiniAppListAdapter
    private lateinit var searchView: SearchView
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(requireContext(), this) }

    private var fetchedMiniAppList: List<MiniAppInfo> = listOf()
    private var selectedMiniAppInfo: MiniAppInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.mini_app_list_fragment,
            container,
            false
        )
        binding.fragment = this
        binding.rvMiniAppList.layoutManager = LinearLayoutManager(this.context)
        binding.rvMiniAppList.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        miniAppListAdapter = MiniAppListAdapter(ArrayList(), this)
        binding.rvMiniAppList.adapter = miniAppListAdapter
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.swipeRefreshLayout.post {
            binding.swipeRefreshLayout.isRefreshing = true
            executeLoadingList()
        }
    }

    private fun executeLoadingList() {
        viewModel.getMiniAppList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = MiniAppListViewModelFactory(MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig))

        viewModel = ViewModelProvider(this, factory).get(MiniAppListViewModel::class.java)

        viewModel.miniAppListData.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            addMiniAppList(it)
        }
        viewModel.errorData.observe(viewLifecycleOwner) {
            updateEmptyView(emptyList())
            addMiniAppList(emptyList())
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            executeLoadingList()
            resetSearchBox()
        }
    }

    override fun onResume() {
        super.onResume()
        MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig)
        activity?.invalidateOptionsMenu()
    }

    private fun addMiniAppList(list: List<MiniAppInfo>) {
        fetchedMiniAppList = list
        updateMiniAppListState(list)
    }

    private fun updateMiniAppListState(list: List<MiniAppInfo>) {
        miniAppListAdapter.addListWithSection(list)
        updateEmptyView(list)
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run {
            selectedMiniAppInfo = miniAppInfo
            activity?.let {
                preloadMiniAppWindow.initiate(
                    miniAppInfo,
                    miniAppInfo.id,
                    miniAppInfo.version.versionId,
                    this
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.action_search -> activity?.onSearchRequested() ?: false
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear() // clearing the previous inflation of menus from MenuBaseActivity
        inflater.inflate(R.menu.main, menu)

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val itemSearch = menu.findItem(R.id.action_search)
        searchView = itemSearch.actionView as SearchView
        searchView.let {
            it.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            it.setOnQueryTextListener(this)
        }
        // by default, search menu is hidden, show search menu here
        itemSearch.isVisible = true

        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener { resetSearchBox() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        resetSearchBox()
    }

    override fun startSearch(query: String?) {
        updateMiniAppListState(produceSearchResult(query))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (searchView.isIconified)
            return true

        updateMiniAppListState(produceSearchResult(newText))
        binding.swipeRefreshLayout.isEnabled = newText.isNullOrEmpty()

        return true
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        if (isAccepted)
            selectedMiniAppInfo?.let { miniAppInfo ->
                val action =
                    MiniAppListFragmentDirections.actionMiniapplistFragmentToMiniappdisplayFragment(
                        miniAppInfo
                    )
                findNavController().graph.findNode(R.id.miniappdisplayFragment)?.label =
                    miniAppInfo.displayName
                findNavController().navigate(action)
            }
    }

    private fun produceSearchResult(newText: String?): List<MiniAppInfo> {
        return fetchedMiniAppList.filter { info ->
            val searchText = newText.toString().toLowerCase(Locale.ROOT)
            info.displayName.toLowerCase(Locale.ROOT).contains(searchText) ||
                    info.id.toLowerCase(Locale.ROOT).contains(searchText)
        }
    }

    private fun resetSearchBox() {
        if (searchView.query.isNotEmpty() || !searchView.isIconified) {
            searchView.onActionViewCollapsed()
            binding.swipeRefreshLayout.isEnabled = true
        }
    }

    private fun updateEmptyView(collection: List<MiniAppInfo>) {
        if (collection.isEmpty())
            binding.emptyView.visibility = View.VISIBLE
        else
            binding.emptyView.visibility = View.GONE
    }
}
