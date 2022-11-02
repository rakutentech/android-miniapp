package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.FeaturesFragmentBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppByUrlActivity

class FeaturesFragment : BaseFragment() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private lateinit var binding: FeaturesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.features_fragment,
            container,
            false
        )
        binding.fragment = this
        return binding.root

    }

    fun switchToInput() {
        raceExecutor.run {
            startActivity(Intent(requireActivity(), MiniAppByUrlActivity::class.java))
        }
    }
}
