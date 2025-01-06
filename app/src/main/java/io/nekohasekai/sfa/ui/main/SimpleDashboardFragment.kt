package io.nekohasekai.sfa.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.nekohasekai.libbox.DeprecatedNoteIterator
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.bg.BoxService
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.databinding.FragmentDashboardBinding
import io.nekohasekai.sfa.databinding.FragmentSimpleDashboardBinding
import io.nekohasekai.sfa.ktx.errorDialogBuilder
import io.nekohasekai.sfa.ktx.launchCustomTab
import io.nekohasekai.sfa.ui.MainActivity
import io.nekohasekai.sfa.ui.dashboard.GroupsFragment
import io.nekohasekai.sfa.ui.dashboard.OverviewFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleDashboardFragment : Fragment(R.layout.fragment_simple_dashboard) {

    private val activity: MainActivity? get() = super.getActivity() as MainActivity?
    private var binding: FragmentSimpleDashboardBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSimpleDashboardBinding.inflate(inflater, container, false)
        this.binding = binding
        onCreate()
        return binding.root
    }

    private fun onCreate() {
        val activity = activity ?: return
        val binding = binding ?: return

        activity.serviceStatus.observe(viewLifecycleOwner) {
            when (it) {
                Status.Starting -> {
                    binding.fab.hide()  // TODO 不显示 连接按钮，这里要调整
                }

                Status.Started -> {
                    binding.container.isVisible = true // 显示连接后的状态

                    binding.fab.setImageResource(R.drawable.ic_stop_24)
                    binding.fab.show()
                    binding.fab.isEnabled = true
                }

                Status.Stopping -> {
                    binding.fab.hide()
                }

                Status.Stopped -> {
                    binding.container.isVisible = false // 不显示连接状态

                    binding.fab.setImageResource(R.drawable.ic_play_arrow_24)
                    binding.fab.show()
                    binding.fab.isEnabled = true
                }

                else -> {}
            }
        }
        binding.fab.setOnClickListener {
            when (activity.serviceStatus.value) {
                Status.Stopped -> {
                    it.isEnabled = false
                    activity.startService()
                }

                Status.Started -> {
                    BoxService.stop()
                }

                else -> {}
            }
        }

    //    binding.container.isVisible = false  // 默认不显示连接后状态
    }

    override fun onStart() {
        super.onStart()
        val activityBinding = activity?.binding ?: return
        val binding = binding ?: return
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }




}