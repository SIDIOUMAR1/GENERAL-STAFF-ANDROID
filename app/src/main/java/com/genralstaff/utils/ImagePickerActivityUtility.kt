package com.genralstaff.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.genralstaff.R
import com.genralstaff.BuildConfig


import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

abstract class ImagePickerActivityUtility : AppCompatActivity() {

    private val REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private lateinit var mImageFile: File

    private var mActivity: Activity? = null
    private var mCode = 0

    @RequiresApi(Build.VERSION_CODES.M)
    open fun getImage(activity: Activity, code: Int) {
        mActivity = activity
        mCode = code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!cameraPermission(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                    )
                )
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    checkPermissionDenied(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    checkPermissionDenied(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                } else {
                    requestPermission2()
                }
            } else {
                imageDialog()

            }
        } else {
            if (!cameraPermission(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                )
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    checkPermissionDenied(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                } else {
                    requestPermission()
                }
            } else {
                imageDialog()

            }
        }
    }

    private fun imageDialog() {
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.view_gallery_camera)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val camera = dialog.findViewById<TextView>(R.id.tvCamera)
        val cancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val gallery = dialog.findViewById<TextView>(R.id.tvGallery)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            captureImage(mActivity!!)
        }

        gallery.setOnClickListener {
            dialog.dismiss()
            openGallery(mActivity!!)
        }
        dialog.show()
    }

    open fun captureImage(activity: Activity) {
        mActivity = activity


        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        try {
            createImageFile(mActivity!!, imageFileName, ".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            Objects.requireNonNull(activity.applicationContext),

            BuildConfig.APPLICATION_ID + ".provider",
            mImageFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String, extension: String) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mImageFile = File.createTempFile(
            name,
            extension,
            storageDir
        )
    }

    open fun openGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun cameraPermission(permissions: Array<String>): Boolean {
        return ContextCompat.checkSelfPermission(
            mActivity!!,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            mActivity!!,
            permissions[1]
        ) == PackageManager.PERMISSION_GRANTED
    }

    open fun requestPermission() {
        ActivityCompat.requestPermissions(
            mActivity!!, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    open fun requestPermission2() {
        ActivityCompat.requestPermissions(
            mActivity!!, arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            ), REQUEST_CODE
        )
    }

    private fun checkPermissionDenied(permissions: Array<out String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permissions[0])) {
                val mBuilder = AlertDialog.Builder(mActivity)
                val dialog: AlertDialog =
                    mBuilder.setTitle(R.string.alert).setMessage(R.string.permissionRequired)
                        .setPositiveButton(
                            R.string.ok
                        ) { dialog, which -> requestPermission() }
                        .setNegativeButton(
                            R.string.cancel
                        ) { dialog, which ->

                        }.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(
                            mActivity!!, R.color.app_theme_color
                        )
                    )
                }
                dialog.show()
            } else {
                try {
//                    val builder = AlertDialog.Builder(mActivity)
//                    val dialog: AlertDialog =
//                        builder.setTitle(R.string.alert).setMessage(R.string.permissionRequired)
//                            .setCancelable(
//                                false
//                            )
//                            .setPositiveButton(R.string.openSettings) { dialog, which ->
//                               finish()
//                                val intent = Intent(
//                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                                    Uri.fromParts("package", packageName, null)
//                                )
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                startActivity(intent)
//                            }.create()
//                    dialog.setOnShowListener {
//                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
//                            ContextCompat.getColor(
//                                mActivity!!, R.color.purple_200
//                            )
//                        )
//                    }
//                    dialog.show()
                } catch (e: Exception) {
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage(mActivity!!, mCode)
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                checkPermissionDenied(permissions)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = Uri.fromFile(mImageFile)
            val picturePath = getAbsolutePath(uri)
            selectedImage(picturePath, mCode)
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            val picturePath = getAbsolutePath(uri!!)
            selectedImage(picturePath, mCode)
        }
    }


    //------------------------Return Uri file to String Path ------------------//
    @SuppressLint("Recycle")
    fun getAbsolutePath(uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity?.contentResolver?.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    abstract fun selectedImage(imagePath: String?, code: Int?)

}