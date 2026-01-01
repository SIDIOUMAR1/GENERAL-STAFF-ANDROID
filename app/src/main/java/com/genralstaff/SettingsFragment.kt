package com.genralstaff

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.auth.LoginActivity
import com.genralstaff.databinding.FragmentSettingsBinding
import com.genralstaff.home.ui.ChooseLanguageActivity
import com.genralstaff.home.ui.ContentActivity
import com.genralstaff.home.ui.MyProfileActivity
import com.genralstaff.home.ui.OrderHistoryActivity
import com.genralstaff.home.ui.ShopsActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils
import com.genralstaff.viewmodel.AuthViewModel



class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
    private val progressDialog by lazy { CustomProgressDialog() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicks()
    }

    private fun clicks() {
        _binding!!.rlAccount.setOnClickListener {
            blinkView(it, MyProfileActivity(), "")
        }
        _binding!!.rlShops.setOnClickListener {
            blinkView(it, ShopsActivity(), "")
        }
        _binding!!.rlOrder.setOnClickListener {
            blinkView(it, OrderHistoryActivity(), "orders")
        }
        _binding!!.rlCurrentOrder.setOnClickListener {
            blinkView(it, OrderHistoryActivity(), "current_orders")
        }
        _binding!!.rlHelp.setOnClickListener {
            blinkView(it, ContentActivity(), "help")
        }
        _binding!!.rlAbout.setOnClickListener {
            blinkView(it, ContentActivity(), "about")
        }
        _binding!!.rlPrivacy.setOnClickListener {
            blinkView(it, ContentActivity(), "privacy")
        }
        _binding!!.rlLanguage.setOnClickListener {
            blinkView(it, ChooseLanguageActivity(), "setting")
        }
        _binding!!.rlLogout.setOnClickListener {
            logout()
        }
        viewModelSetupAndResponse()
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
        authViewModel.onLogOutResponse().observe(viewLifecycleOwner) { response ->
            response?.let {
                if (it.code == 200) {
                    prefs?.clearSharedPreference()

                    MyApplication.mSocketManager = null
                    sessionExpire()
                }
            }
        }
    }

    private fun blinkView(view: View, activity: Activity, type: String) {
        requireActivity().startActivity(
            Intent(requireContext(), activity::class.java)
                .putExtra("type", type)
        )
//        val blinkDuration = 200L // milliseconds
//        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
//        alphaAnimation.duration = blinkDuration
//        alphaAnimation.repeatCount = 1
//        alphaAnimation.repeatMode = AlphaAnimation.REVERSE
//
//        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationStart(animation: Animation) {}
//            override fun onAnimationRepeat(animation: Animation) {}
//            override fun onAnimationEnd(animation: Animation) {
//                // Restore view alpha to fully opaque
//                view.alpha = 1.0f
//
//
//
//            }
//        })
//
//        view.startAnimation(alphaAnimation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Fragment.sessionExpire() {
        this.goToSelectUserWithClearFlag()

    }

    private fun logout() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logout_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setGravity(Gravity.BOTTOM)
//        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationPhone

        dialog.show()

        val tvYesL = dialog.findViewById<TextView>(R.id.btnYes)
        val tvNoL = dialog.findViewById<TextView>(R.id.btnNo)

        tvYesL.setOnClickListener {
            if (isAdded) {
//                sessionExpire()

                authViewModel.logout()
                dialog.dismiss()
            }
        }
        tvNoL.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun Fragment.goToSelectUserWithClearFlag() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        requireActivity().overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        return startActivity(intent)
    }
}