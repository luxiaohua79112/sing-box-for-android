package io.nekohasekai.sfa.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.bg.BoxService
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.databinding.FragmentSimpleDashboardBinding
import io.nekohasekai.sfa.ktx.errorDialogBuilder
import io.nekohasekai.sfa.ui.MainActivity
import io.nekohasekai.sfa.utils.CommandClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SimpleDashboardFragment : Fragment(R.layout.fragment_simple_dashboard) {

    private val activity: MainActivity? get() = super.getActivity() as MainActivity?
    private var binding: FragmentSimpleDashboardBinding? = null

    private val cmdClientHandler =
        CommandClient(lifecycleScope, CommandClient.ConnectionType.Status, CmdClientHandler())

    /////////////////////////////////////////////////////////////////////
    ///////////////// Methods of Override Fragment /////////////////////
    ////////////////////////////////////////////////////////////////////
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
                    binding.connectContainer.isVisible = true // 显示连接后的状态
                    cmdClientHandler.connect()

                    binding.fab.setImageResource(R.drawable.ic_stop_24)
                    binding.fab.show()
                    binding.fab.isEnabled = true
                }

                Status.Stopping -> {
                    binding.fab.hide()
                }

                Status.Stopped -> {
                    binding.connectContainer.isVisible = false // 不显示连接状态

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

        binding.connectContainer.isVisible = false  // 默认不显示连接后状态

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                activity.autoConfigProfile()

            }.onFailure { e ->
                withContext(Dispatchers.Main) {

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val activityBinding = activity?.binding ?: return
        val binding = binding ?: return
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        cmdClientHandler.disconnect()
    }


    //////////////////////////////////////////////////////////////////////////
    ////////////////////////// UI Message or Events Handler //////////////////
//////////////////////////////////////////////////////////////////////////
    inner class CmdClientHandler : CommandClient.Handler {

        override fun onConnected() {
            val binding = binding ?: return
            lifecycleScope.launch(Dispatchers.Main) {
                binding.memoryText.text = getString(R.string.loading)
                binding.goroutinesText.text = getString(R.string.loading)
            }
        }

        override fun onDisconnected() {
            val binding = binding ?: return
            lifecycleScope.launch(Dispatchers.Main) {
                binding.memoryText.text = getString(R.string.loading)
                binding.goroutinesText.text = getString(R.string.loading)
            }
        }

        override fun updateStatus(status: StatusMessage) {
            val binding = binding ?: return
            lifecycleScope.launch(Dispatchers.Main) {
                binding.memoryText.text = Libbox.formatBytes(status.memory)
                binding.goroutinesText.text = status.goroutines.toString()
                val trafficAvailable = status.trafficAvailable
                binding.trafficContainer.isVisible = trafficAvailable
                if (trafficAvailable) {
                    binding.inboundConnectionsText.text = status.connectionsIn.toString()
                    binding.outboundConnectionsText.text = status.connectionsOut.toString()
                    binding.uplinkText.text = Libbox.formatBytes(status.uplink) + "/s"
                    binding.downlinkText.text = Libbox.formatBytes(status.downlink) + "/s"
                    binding.uplinkTotalText.text = Libbox.formatBytes(status.uplinkTotal)
                    binding.downlinkTotalText.text = Libbox.formatBytes(status.downlinkTotal)
                }
            }
        }
    }


}