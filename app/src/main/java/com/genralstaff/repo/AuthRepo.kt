package com.genralstaff.repo


import com.genralstaff.network.RetrofitInterface
import com.genralstaff.network.retrofitService

import okhttp3.MultipartBody
import okhttp3.RequestBody


class AuthRepo {
    private var retrofitInterface: RetrofitInterface = retrofitService

    suspend fun login(params: HashMap<String, String>) = retrofitInterface.login(params)
    suspend fun shuffle(params: HashMap<String, String>) = retrofitInterface.shuffle(params)
    suspend fun shopDetail(id: String) = retrofitInterface.shopDetail(id)

    suspend fun add_category(params: HashMap<String, String>) = retrofitInterface.add_category(params)
    suspend fun re_order(params: HashMap<String, String>) = retrofitInterface.re_order(params)
    suspend fun get_types(id:String) = retrofitInterface.get_types(id)
    suspend fun orderDetail(params: HashMap<String, String>) = retrofitInterface.orderDetail(params)
    suspend fun categories() = retrofitInterface.categories()
    suspend fun dashboardApi() = retrofitInterface.dashboardApi()
    suspend fun categoriesQuery(id: String) = retrofitInterface.categoriesQuery(id)
    suspend fun shopsItems(params: HashMap<String, String>) = retrofitInterface.shopsItems(params)

    suspend fun signup(params: HashMap<String, RequestBody>, imagePart: MultipartBody.Part, license: MultipartBody.Part) = retrofitInterface.signUp(params, imagePart,license)
    suspend fun signup(params: HashMap<String, RequestBody>, license: MultipartBody.Part) = retrofitInterface.signUp(params,license)
    suspend fun notificationList(params: HashMap<String, String>) = retrofitInterface.notificationList(params)
    suspend fun shopItems(params: HashMap<String, String>) = retrofitInterface.shopItems(params)
    suspend fun orders(params: HashMap<String, String>) = retrofitInterface.orders(params)
    suspend fun shops(params: HashMap<String, String>) = retrofitInterface.shops(params)
    suspend fun orders2(params: HashMap<String, String>) = retrofitInterface.orders2(params)
    suspend fun checkOrder(params: HashMap<String, String>) = retrofitInterface.checkOrder(params)
    suspend fun uploadFiles( imagePart: MultipartBody.Part) = retrofitInterface.uploadFiles( imagePart)

    suspend fun content(id:String) = retrofitInterface.content(id)
    suspend fun delete_type(id:String) = retrofitInterface.delete_type(id)
    suspend fun delete_product(id:String) = retrofitInterface.delete_product(id)
    suspend fun edit_type(
        id: String,
        name: String?,
        nameAr: String?,
        nameFr: String?
    ) = retrofitInterface.editType(id, name, nameAr, nameFr)

    suspend fun profile() = retrofitInterface.profile()
    suspend fun logout() = retrofitInterface.logout()
    suspend fun editProfile(params: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) =
        retrofitInterface.editProfile(params, imagePart)
    suspend fun addShop(params: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) = retrofitInterface.addShop(params,imagePart)

    suspend fun editProfileWithoutImage(params: HashMap<String, RequestBody>) = retrofitInterface.editProfileWithoutImage(params)
    suspend fun editShopWithoutImage(params: HashMap<String, RequestBody>) = retrofitInterface.editShopWithoutImage(params)
    suspend fun editShop(params: HashMap<String, RequestBody>, imagePart: MultipartBody.Part) = retrofitInterface.editShop(params,imagePart)
    suspend fun editShop(params: HashMap<String, RequestBody>) = retrofitInterface.editShop(params)
    suspend fun addProduct(params: HashMap<String, RequestBody>,imagePart: ArrayList<MultipartBody.Part>) = retrofitInterface.addProduct(params,imagePart)
    suspend fun editProduct(params: HashMap<String, RequestBody>,imagePart: ArrayList<MultipartBody.Part>) = retrofitInterface.editProduct(params,imagePart)
}