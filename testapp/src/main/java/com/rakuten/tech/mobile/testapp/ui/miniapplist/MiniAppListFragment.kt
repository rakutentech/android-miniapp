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
import com.rakuten.tech.mobile.testapp.launchActivity
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppInputActivity
import kotlinx.android.synthetic.main.mini_app_list_fragment.*
import kotlinx.coroutines.launch

class MiniAppListFragment : BaseFragment(), MiniAppList {

    companion object {
        fun newInstance() = MiniAppListFragment()
    }

    private lateinit var viewModel: MiniAppListViewModel
    private lateinit var binding: MiniAppListFragmentBinding
    private lateinit var miniAppListAdapter: MiniAppListAdapter
    private var miniapps = listOf<MiniAppInfo>()

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
        miniAppListAdapter = MiniAppListAdapter(miniapps, this)
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
                miniAppListAdapter.miniapps = it
                miniAppListAdapter.notifyDataSetChanged()
            })
            errorData.observe(viewLifecycleOwner, Observer {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            })
        }

        swipeRefreshLayout.setOnRefreshListener { executeLoadingList() }
    }

    private fun executeLoadingList() {
        launch { viewModel.getMiniAppList() }
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run { MiniAppDisplayActivity.start(context!!, miniAppInfo) }
    }

    fun switchToInput() {
        raceExecutor.run { activity?.launchActivity<MiniAppInputActivity>() }
    }
}
