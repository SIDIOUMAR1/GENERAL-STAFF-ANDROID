package com.genralstaff.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.genralstaff.base.BaseViewModel
import com.genralstaff.base.UNUSUAL_ERROR
import com.genralstaff.network.ApiCallsHandler
import com.genralstaff.repo.AuthRepo
import com.genralstaff.responseModel.CategoriesListResponse
import com.genralstaff.responseModel.GetShopsResponse
import com.genralstaff.responseModel.LogOutResponse
import com.genralstaff.responseModel.LoginResponse
import com.genralstaff.responseModel.NotificationListResponse
import com.genralstaff.responseModel.OrderDetailResponse
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.responseModel.ProfileResponse
import com.genralstaff.responseModel.ShopAddResponse
import com.genralstaff.responseModel.UploadProfileResponse
import com.genraluser.responseModel.ContentsResponse
import com.genraluser.responseModel.EditProfileResponse
import com.genralstaff.responseModel.SignUpResponse
import com.genralstaff.responseModel.CategoriesResponse
import com.genralstaff.responseModel.CategoriesResponseNew
import com.genralstaff.responseModel.CheckOrderResponse
import com.genralstaff.responseModel.CommonResponse
import com.genralstaff.responseModel.DashboardResponse
import com.genralstaff.responseModel.GetShopItemsNewResponse
import com.genralstaff.responseModel.OrderDetailNewResponse
import com.genralstaff.responseModel.ShopDetailResponse
import com.genralstaff.responseModel.ShopItemsResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class AuthViewModel : BaseViewModel() {
    private val authRepo = AuthRepo()
    private val _loginResponse = MutableLiveData<LoginResponse?>()
    fun onLoginResponse(): LiveData<LoginResponse?> = _loginResponse

    private val _commonResponse = MutableLiveData<CommonResponse?>()
    fun onCommonResponse(): LiveData<CommonResponse?> = _commonResponse
    private val _shopDetailResponse = MutableLiveData<ShopDetailResponse?>()
    fun onShopDetailResponse(): LiveData<ShopDetailResponse?> = _shopDetailResponse

    private val _categorieslistResponse = MutableLiveData<CategoriesListResponse?>()
    fun onCategoriesListResponse(): LiveData<CategoriesListResponse?> = _categorieslistResponse


    private val _shopAddResponse = MutableLiveData<ShopAddResponse?>()
    fun onShopAddResponse(): LiveData<ShopAddResponse?> = _shopAddResponse
    private val _signUpResponse = MutableLiveData<SignUpResponse?>()
    fun onSignUp(): LiveData<SignUpResponse?> = _signUpResponse
    private val _contentsResponse = MutableLiveData<ContentsResponse?>()
    fun onContentsResponse(): LiveData<ContentsResponse?> = _contentsResponse
    private val _profileResponse = MutableLiveData<ProfileResponse?>()
    private val _logOutResponse = MutableLiveData<LogOutResponse?>()
    fun onLogOutResponse(): LiveData<LogOutResponse?> = _logOutResponse
    fun onProfileResponse(): LiveData<ProfileResponse?> = _profileResponse
    private val _editProfileResponse = MutableLiveData<EditProfileResponse?>()
    fun onEditProfileResponse(): LiveData<EditProfileResponse?> = _editProfileResponse
    private val _notificationListResponse = MutableLiveData<NotificationListResponse?>()
    fun onNotificationListResponse(): LiveData<NotificationListResponse?> =
        _notificationListResponse

    private val _shopItemsResponse = MutableLiveData<ShopItemsResponse?>()
    fun onShopItemsResponse(): LiveData<ShopItemsResponse?> =
        _shopItemsResponse

    private val _categoriesResponse = MutableLiveData<CategoriesResponse?>()
    fun onCategoriesResponse(): LiveData<CategoriesResponse?> = _categoriesResponse
    private val _dashboardResponse = MutableLiveData<DashboardResponse?>()
    fun onDashboardResponse(): LiveData<DashboardResponse?> = _dashboardResponse


    private val _categoriesResponseNew = MutableLiveData<CategoriesResponseNew?>()
    fun onCategoriesResponseNew(): LiveData<CategoriesResponseNew?> = _categoriesResponseNew
//    private val _GetShopItemsNewResponse = MutableLiveData<GetShopItemsNewResponse?>()
//    fun onGetShopItemsNewResponse(): LiveData<GetShopItemsNewResponse?> = _GetShopItemsNewResponse

    private val _orderHistoryResponse = MutableLiveData<OrderHistoryResponse?>()
    fun onOrderHistoryResponse(): LiveData<OrderHistoryResponse?> = _orderHistoryResponse
    private val _getShopsResponse = MutableLiveData<GetShopsResponse?>()
    fun onGetShopsResponse(): LiveData<GetShopsResponse?> = _getShopsResponse

    private val _orderHistoryResponse2 = MutableLiveData<OrderHistoryResponse?>()
    fun onOrderHistoryResponse2(): LiveData<OrderHistoryResponse?> = _orderHistoryResponse2
    private val _checkOrderResponse = MutableLiveData<CheckOrderResponse?>()
    fun onCheckOrderResponse(): LiveData<CheckOrderResponse?> = _checkOrderResponse
    private val _uploadProfileResponse = MutableLiveData<UploadProfileResponse?>()
    fun onUploadProfileResponse(): LiveData<UploadProfileResponse?> = _uploadProfileResponse
    private val _orderDetailResponse = MutableLiveData<OrderDetailNewResponse?>()
    fun onOrderDetailResponse(): LiveData<OrderDetailNewResponse?> = _orderDetailResponse

    fun uploadFiles(imagePart: MultipartBody.Part) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val uploadProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.uploadFiles(imagePart)
            }
            hideProgress()
            _uploadProfileResponse.value = uploadProfileResponse
        }
    }

    fun categories() {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val profileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.categories()
            }
            hideProgress()
            _categoriesResponse.value = profileResponse
        }
    }

    fun categoriesQuery(id: String) {
//        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val profileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.categoriesQuery(id)
            }
            hideProgress()
            _categoriesResponseNew.value = profileResponse
        }
    }

    fun shopsItems(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.shopsItems(hashMap)
            }
            hideProgress()
            _shopItemsResponse.value = getShopsResponse
        }
    }

    fun dashboardApi() {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val profileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.dashboardApi()
            }
            hideProgress()
            _dashboardResponse.value = profileResponse
        }
    }

    fun orderDetail(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.orderDetail(hashMap)
            }
            hideProgress()
            _orderDetailResponse.value = getShopsResponse
        }
    }

    fun login(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val otpVerifyResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.login(hashMap)
            }
            hideProgress()
            _loginResponse.value = otpVerifyResponse
        }
    }

    fun shuffle(hashMap: HashMap<String, String>) {
//        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val otpVerifyResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.shuffle(hashMap)
            }
            hideProgress()
            _commonResponse.value = otpVerifyResponse
        }
    }
    fun shopDetail(id: String) {
//        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val shopDetailResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.shopDetail(id)
            }
            hideProgress()
            _shopDetailResponse.value = shopDetailResponse
        }
    }

    fun add_category(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val otpVerifyResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.add_category(hashMap)
            }
            hideProgress()
            _loginResponse.value = otpVerifyResponse
        }
    }

    fun re_order(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val commonResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.re_order(hashMap)
            }
            hideProgress()
            _commonResponse.value = commonResponse
        }
    }

    fun get_types(id: String) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val otpVerifyResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.get_types(id)
            }
            hideProgress()
            _categorieslistResponse.value = otpVerifyResponse
        }
    }

    fun notificationList(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val notificationListResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.notificationList(hashMap)
            }
            hideProgress()
            _notificationListResponse.value = notificationListResponse
        }
    }

    fun shopItems(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val notificationListResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.shopItems(hashMap)
            }
            hideProgress()
            _shopItemsResponse.value = notificationListResponse
        }
    }

    fun orders(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.orders(hashMap)
            }
            hideProgress()
            _orderHistoryResponse.value = getShopsResponse
        }
    }

    fun shops(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.shops(hashMap)
            }
            hideProgress()
            _getShopsResponse.value = getShopsResponse
        }
    }

    fun orders2(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.orders2(hashMap)
            }
            hideProgress()
            _orderHistoryResponse2.value = getShopsResponse
        }
    }

    fun checkOrder(hashMap: HashMap<String, String>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val getShopsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.checkOrder(hashMap)
            }
            hideProgress()
            _checkOrderResponse.value = getShopsResponse
        }
    }

    fun signup(
        hashMap: HashMap<String, RequestBody>,
        imagePart: MultipartBody.Part,
        license: MultipartBody.Part
    ) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val loginResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.signup(hashMap, imagePart, license)
            }
            hideProgress()
            _signUpResponse.value = loginResponse
        }
    }

    fun signup(hashMap: HashMap<String, RequestBody>, license: MultipartBody.Part) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val loginResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.signup(hashMap, license)
            }
            hideProgress()
            _signUpResponse.value = loginResponse
        }
    }

    fun content(id: String) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val contentsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.content(id)
            }
            hideProgress()
            _contentsResponse.value = contentsResponse
        }
    }

    fun delete_type(id: String) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val contentsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.delete_type(id)
            }
            hideProgress()
            _contentsResponse.value = contentsResponse
        }
    }

    fun edit_type(id: String, name: String?, nameAr: String?, nameFr: String?) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val contentsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.edit_type(id, name, nameAr, nameFr)
            }
            hideProgress()
            _contentsResponse.value = contentsResponse
        }
    }

    fun delete_product(id: String) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val contentsResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.delete_product(id)
            }
            hideProgress()
            _contentsResponse.value = contentsResponse
        }
    }

    fun profile() {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val profileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.profile()
            }
            hideProgress()
            _profileResponse.value = profileResponse
        }
    }

    fun logout() {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val logOutResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.logout()
            }
            hideProgress()
            _logOutResponse.value = logOutResponse
        }
    }

    fun editProfile(hashMap: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editProfile(hashMap, imagePart)
            }
            hideProgress()
            _editProfileResponse.value = editProfileResponse
        }
    }

    fun addShop(hashMap: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val otpVerifyResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.addShop(hashMap, imagePart)
            }
            hideProgress()
            _shopAddResponse.value = otpVerifyResponse
        }
    }

    fun editProfileWithoutImage(hashMap: HashMap<String, RequestBody>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editProfileWithoutImage(hashMap)
            }
            hideProgress()
            _editProfileResponse.value = editProfileResponse
        }
    }

    fun editShopWithoutImage(hashMap: HashMap<String, RequestBody>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editShopWithoutImage(hashMap)
            }
            hideProgress()
            _shopAddResponse.value = editProfileResponse
        }
    }

    fun editShop(hashMap: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editShop(hashMap, imagePart)
            }
            hideProgress()
            _shopAddResponse.value = editProfileResponse
        }
    }

    fun editShop(hashMap: HashMap<String, RequestBody>) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editShop(hashMap)
            }
            hideProgress()
            _shopAddResponse.value = editProfileResponse
        }
    }

    fun addProduct(
        hashMap: HashMap<String, RequestBody>,
        imagePart: ArrayList<MultipartBody.Part>
    ) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.addProduct(hashMap, imagePart)
            }
            hideProgress()
            _shopAddResponse.value = editProfileResponse
        }
    }

    fun editProduct(
        hashMap: HashMap<String, RequestBody>,
        imagePart: ArrayList<MultipartBody.Part>
    ) {
        showProgress()
        viewModelScope.launch(homeExceptionHandler) {
            val editProfileResponse = ApiCallsHandler.safeApiCall(this@AuthViewModel) {
                authRepo.editProduct(hashMap, imagePart)
            }
            hideProgress()
            _shopAddResponse.value = editProfileResponse
        }
    }

    private val homeExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        var errorMessage = throwable.message
        if (throwable is UnknownHostException || throwable is SocketTimeoutException) {
            errorMessage = "Internet is not available"
        }

        errorMessage = errorMessage ?: UNUSUAL_ERROR
        hideProgress()
        showErrorMessage(errorMessage)
    }

}