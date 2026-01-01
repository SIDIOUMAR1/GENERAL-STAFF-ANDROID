package com.genralstaff.network

import com.genralstaff.base.ADD_PRODUCT
import com.genralstaff.base.ADD_SHOP
import com.genralstaff.base.CATEGORIES
import com.genralstaff.base.CATEGORIES_Types
import com.genralstaff.base.CHECK_ORDER
import com.genralstaff.base.CONTENT
import com.genralstaff.base.DASHBOARD
import com.genralstaff.base.DRIVER_NEW_ORDERS
import com.genralstaff.base.EDIT_PRODUCT
import com.genralstaff.base.EDIT_PROFILE
import com.genralstaff.base.EDIT_SHOP
import com.genralstaff.base.LOGIN
import com.genralstaff.base.LOGOUT
import com.genralstaff.base.NOTIFICATION_LIST
import com.genralstaff.base.ORDERS
import com.genralstaff.base.ORDER_DETAIL
import com.genralstaff.base.PROFILE_DETAIL
import com.genralstaff.base.REGISTER
import com.genralstaff.base.RE_ORDER
import com.genralstaff.base.SHOPS
import com.genralstaff.base.SHOPS_ITEMS
import com.genralstaff.base.SHOP_DETAIL
import com.genralstaff.base.SUFFLE_TYPE
import com.genralstaff.base.UPLOAD_MEDIA
import com.genralstaff.base.add_type
import com.genralstaff.base.delete_product
import com.genralstaff.base.delete_type
import com.genralstaff.base.get_types
import com.genralstaff.responseModel.CategoriesListResponse
import com.genralstaff.responseModel.GetShopsResponse
import com.genralstaff.responseModel.LogOutResponse
import com.genralstaff.responseModel.LoginResponse
import com.genralstaff.responseModel.NotificationListResponse
import com.genralstaff.responseModel.OrderDetailResponse
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genraluser.responseModel.ContentsResponse
import com.genralstaff.responseModel.ProfileResponse
import com.genralstaff.responseModel.ShopAddResponse
import com.genralstaff.responseModel.UploadProfileResponse
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface RetrofitInterface {
    @POST(LOGIN)
    suspend fun login(@Body map: HashMap<String, String>): LoginResponse
    @POST(SUFFLE_TYPE)
    suspend fun shuffle(@Body map: HashMap<String, String>): CommonResponse
    @GET("$SHOP_DETAIL{id}")
    suspend fun shopDetail(@Path("id") id: String): ShopDetailResponse
    @POST(add_type)
    suspend fun add_category(@Body map: HashMap<String, String>): LoginResponse
    @PUT(RE_ORDER)
    suspend fun re_order(@Body map: HashMap<String, String>): CommonResponse

    @GET(get_types)
    suspend fun get_types(@Query("shop_id") id: String): CategoriesListResponse

    @GET("$delete_type{id}")
    suspend fun delete_type(@Path("id") id: String): ContentsResponse
    @GET("$delete_product{id}")
    suspend fun delete_product(@Path("id") id: String): ContentsResponse
    @FormUrlEncoded
    @POST("edit/type/{id}")
    suspend fun editType(
        @Path("id") id: String,
        @Field("name") name: String?,
        @Field("name_ar") nameAr: String?,
        @Field("name_fr") nameFr: String?
    ): ContentsResponse

    @Multipart
    @POST(REGISTER)
    suspend fun signUp(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part,
        @Part license: MultipartBody.Part,
    ): SignUpResponse
    @Multipart
    @POST(REGISTER)
    suspend fun signUp(
        @PartMap map: HashMap<String, RequestBody>,
        @Part license: MultipartBody.Part,
    ): SignUpResponse

    @GET(ORDER_DETAIL)
    suspend fun orderDetail(@QueryMap param: Map<String, String>): OrderDetailNewResponse
    @GET(CATEGORIES_Types)
    suspend fun categoriesQuery(@Query("shop_id") id: String): CategoriesResponseNew
    @GET(SHOPS_ITEMS)
    suspend fun shopsItems(@QueryMap param: Map<String, String>): ShopItemsResponse
    @GET(CATEGORIES)
    suspend fun categories(): CategoriesResponse
    @GET(DASHBOARD)
    suspend fun dashboardApi(): DashboardResponse

    @GET("$CONTENT{id}")
    suspend fun content(@Path("id") id: String): ContentsResponse

    @GET(PROFILE_DETAIL)
    suspend fun profile(): ProfileResponse
    @GET(LOGOUT)
    suspend fun logout(): LogOutResponse
    @Multipart
    @PUT(EDIT_PROFILE)
    suspend fun editProfile(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part,
    ): EditProfileResponse
    @Multipart
    @PUT(EDIT_PROFILE)
    suspend fun editProfileWithoutImage(
        @PartMap map: HashMap<String, RequestBody>
    ): EditProfileResponse
    @Multipart
    @POST(ADD_SHOP)
    suspend fun addShop(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part,
    ): ShopAddResponse
    @Multipart
    @PUT(EDIT_SHOP)
    suspend fun editShop(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part,
    ): ShopAddResponse
    @Multipart
    @PUT(EDIT_SHOP)
    suspend fun editShop(
        @PartMap map: HashMap<String, RequestBody>,
    ): ShopAddResponse

    @Multipart
    @POST(ADD_PRODUCT)
    suspend fun addProduct(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: ArrayList<MultipartBody.Part>
    ): ShopAddResponse
    @Multipart
    @PUT(EDIT_PRODUCT)
    suspend fun editProduct(
        @PartMap map: HashMap<String, RequestBody>,
        @Part image: ArrayList<MultipartBody.Part>
    ): ShopAddResponse


    @Multipart
    @PUT(EDIT_SHOP)
    suspend fun editShopWithoutImage(
        @PartMap map: HashMap<String, RequestBody>
    ): ShopAddResponse

    @Multipart
    @POST(UPLOAD_MEDIA)
    suspend fun uploadFiles(
        @Part image: MultipartBody.Part,
    ): UploadProfileResponse

    @GET(NOTIFICATION_LIST)
    suspend fun notificationList(@QueryMap param: Map<String, String>): NotificationListResponse

    @GET(SHOPS_ITEMS)
    suspend fun shopItems(@QueryMap param: Map<String, String>): ShopItemsResponse

    @GET(ORDERS)
    suspend fun orders(@QueryMap param: Map<String, String>): OrderHistoryResponse
    @GET(SHOPS)
    suspend fun shops(@QueryMap param: Map<String, String>): GetShopsResponse
    @GET(DRIVER_NEW_ORDERS)
    suspend fun orders2(@QueryMap param: Map<String, String>): OrderHistoryResponse
    @GET(CHECK_ORDER)
    suspend fun checkOrder(@QueryMap param: Map<String, String>): CheckOrderResponse



}
