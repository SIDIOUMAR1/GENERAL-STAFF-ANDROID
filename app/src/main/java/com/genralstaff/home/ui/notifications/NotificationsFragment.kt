package com.genralstaff.home.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.adapter.NotificationListAdapter
import com.genralstaff.databinding.FragmentNotificationsBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.NotificationListResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var authViewModel: AuthViewModel
    private val progressDialog by lazy { CustomProgressDialog() }

    var notificationList = ArrayList<NotificationListResponse.Body.Data>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var limit = 100
    var page = 1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = NotificationListAdapter(requireContext(), notificationList)
        binding.rvNotifications.adapter = adapter
        viewModelSetupAndResponse()
//        binding.ivTrACK.setOnClickListener {
//            startActivity(Intent(requireContext(), TrackOrderActivity::class.java))
//        }
    }

    private fun viewModelSetupAndResponse() {

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.getError().observe(viewLifecycleOwner) {
            Utils.showToast(requireContext(), it)
        }
        authViewModel.progressDialogData().observe(viewLifecycleOwner) { isShowProgress ->
            if (isShowProgress) {
                progressDialog.show(requireActivity())
            } else {
                progressDialog.hide()
            }
        }
        authViewModel.onShowErrorCode().observe(viewLifecycleOwner) {
            when (it) {
                ErrorType.UNAUTHORIZED -> {
                    sessionExpire()
                }

                else -> {}
            }
        }
        authViewModel.onNotificationListResponse().observe(viewLifecycleOwner) { response ->
            response?.let {
                if (it.code == 200) {
                    notificationList.clear()
                    notificationList.addAll(it.body.data)
                    if (notificationList.isEmpty()) {
                        binding.llNoNewRequest.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE
                    } else {
                        binding.llNoNewRequest.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE
                        val adapter = NotificationListAdapter(requireContext(), notificationList)
                        binding.rvNotifications.adapter = adapter

                    }
                }
            }
        }
        val map = HashMap<String, String>()
        map["limit"] = limit.toString()
        map["page"] = page.toString()
        authViewModel.notificationList(map)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}