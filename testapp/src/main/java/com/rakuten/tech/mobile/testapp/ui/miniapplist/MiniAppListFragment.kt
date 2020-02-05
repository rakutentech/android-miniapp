package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppListFragmentBinding
import com.rakuten.tech.mobile.testapp.adapter.IMiniAppList
import com.rakuten.tech.mobile.testapp.adapter.MiniAppListAdapter
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import kotlinx.coroutines.launch

class MiniAppListFragment : BaseFragment(), IMiniAppList {

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
        binding.rvMiniAppList.layoutManager = LinearLayoutManager(this.context)
        miniAppListAdapter = MiniAppListAdapter(miniapps, this)
        binding.rvMiniAppList.adapter = miniAppListAdapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(MiniAppListViewModel::class.java).apply {
                miniAppListData.observe(viewLifecycleOwner, Observer {
                    miniAppListAdapter.miniapps = it
                    miniAppListAdapter.notifyDataSetChanged()
                })
                errorData.observe(viewLifecycleOwner, Observer {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                })
                miniAppView.observe(viewLifecycleOwner, Observer {
                    //do something
                })
            }
        launch { viewModel.getMiniAppList() }
    }

    override fun onMiniAppItemClick(appId: String, versionId: String) {
        MiniAppDisplayActivity.start(context!!, appId, versionId)
    }
}
