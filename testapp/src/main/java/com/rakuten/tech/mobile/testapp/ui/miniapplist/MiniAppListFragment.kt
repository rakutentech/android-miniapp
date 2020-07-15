package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class MiniAppListFragment : BaseFragment(), MiniAppList {

    companion object {
        fun newInstance() = MiniAppListFragment()
    }

    private lateinit var viewModel: MiniAppListViewModel
    private lateinit var binding: MiniAppListFragmentBinding
    private lateinit var miniAppListAdapter: MiniAppListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                displayMiniAppList(it)
                MiniAppListStore.instance.saveMiniAppList(it)
            })
            errorData.observe(viewLifecycleOwner, Observer {
                val list = MiniAppListStore.instance.getMiniAppList()
                if (list.isEmpty())
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                else
                    displayMiniAppList(list)
                swipeRefreshLayout.isRefreshing = false
            })
        }

        swipeRefreshLayout.setOnRefreshListener { executeLoadingList() }
    }

    private fun displayMiniAppList(list: List<MiniAppInfo>) {
        miniAppListAdapter.addListWithSection(list)
    }

    private fun executeLoadingList() {
        viewModel.getMiniAppList()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run { MiniAppDisplayActivity.start(context!!, miniAppInfo) }
    }

    fun switchToInput() {
        raceExecutor.run { activity?.launchActivity<MiniAppInputActivity>() }
    }
}
