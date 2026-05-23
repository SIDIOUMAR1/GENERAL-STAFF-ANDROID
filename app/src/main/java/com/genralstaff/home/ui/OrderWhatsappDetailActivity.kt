package com.genralstaff.home.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityOrderWhatsappDetailBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.rygelouv.audiosensei.player.AudioSenseiListObserver
import java.io.File
import java.net.URL
import android.content.Context
class OrderWhatsappDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderWhatsappDetailBinding
    private lateinit var authViewModel: AuthViewModel
    private val progressDialog by lazy { CustomProgressDialog() }

    private var shopId = ""
    private var shopName = ""
    private var audioUserLocation = ""
    private var audioSummary = ""
    private var whatsappNumber = ""
    private var deliveryCharge = ""
    private var shopAudioFileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderWhatsappDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AudioSenseiListObserver.getInstance().registerLifecycle(lifecycle)

        shopId = intent.getStringExtra("shop_id") ?: ""
        shopName = intent.getStringExtra("shop_name") ?: ""
        audioUserLocation = intent.getStringExtra("audio_user_location") ?: ""
        audioSummary = intent.getStringExtra("audio_summary") ?: ""
        whatsappNumber = intent.getStringExtra("whatsapp_number") ?: ""
        deliveryCharge = intent.getStringExtra("delivery_charge") ?: ""

        setupUI()
        setupViewModel()
        loadShopAudio()
    }

    private fun setupUI() {
        // Header
        binding.tvShopName.text = shopName
        binding.tvDeliveryCharge.text = "$deliveryCharge UM"

        // WhatsApp driver
        if (whatsappNumber.isNotEmpty()) {
            binding.tvWhatsappNumber.text = whatsappNumber
            binding.llWhatsappDriver.visibility = View.VISIBLE

            // ✅ Copier le numéro avec +222
            binding.ivCopyWhatsapp.setOnClickListener {
                val numberToCopy = if (whatsappNumber.startsWith("+")) {
                    whatsappNumber
                } else {
                    "+222$whatsappNumber"
                }
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("WhatsApp", numberToCopy)
                clipboard.setPrimaryClip(clip)
                Utils.showToast(this, "✅ Numéro copié: $numberToCopy")
            }

            binding.btnOpenWhatsapp.setOnClickListener {
                val number = whatsappNumber.replace("+", "")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number"))
                startActivity(intent)
            }
        } else {
            binding.llWhatsappDriver.visibility = View.GONE
        }

        // Vocal utilisateur
        if (audioUserLocation.isNotEmpty()) {
            binding.llUserAudio.visibility = View.VISIBLE
            val audioUrl = profileBaseUrl + audioUserLocation
            binding.playerUserAudio.setAudioTarget(audioUrl)
            binding.btnShareUserAudio.setOnClickListener {
                shareAudio(audioUrl, "vocal_utilisateur.m4a")
            }
        } else {
            binding.llUserAudio.visibility = View.GONE
        }

        // Bouton fermer
        binding.ivClose.setOnClickListener { finish() }
        binding.btnClose.setOnClickListener {
            startActivity(
                Intent(this, OrderHistoryActivity::class.java)
                    .putExtra("type", "current_orders")
                    .putExtra("types", "add_orders")
            )
            finish()
        }
    }

    private fun setupViewModel() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.onShowErrorCode().observe(this) {
            if (it == ErrorType.UNAUTHORIZED) sessionExpire()
        }
        authViewModel.onShopDetailResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200 && it.body != null) {
                    val shopAudio = it.body.shop_location_audio ?: ""
                    if (shopAudio.isNotEmpty()) {
                        shopAudioFileName = shopAudio
                        binding.llShopAudio.visibility = View.VISIBLE
                        val audioUrl = imageURL + shopAudio
                        binding.playerShopAudio.setAudioTarget(audioUrl)
                        binding.btnShareShopAudio.setOnClickListener {
                            shareAudio(audioUrl, "vocal_restaurant.m4a")
                        }
                    } else {
                        binding.llShopAudio.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun loadShopAudio() {
        if (shopId.isNotEmpty()) {
            authViewModel.shopDetail(shopId)
        }
    }

    private fun shareAudio(audioUrl: String, fileName: String) {
        // Télécharge et partage le fichier audio
        Thread {
            try {
                val url = URL(audioUrl)
                val file = File(cacheDir, fileName)
                url.openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )
                runOnUiThread {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Partager le vocal via..."))
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Utils.showToast(this, "Erreur lors du partage: ${e.message}")
                }
            }
        }.start()
    }
}