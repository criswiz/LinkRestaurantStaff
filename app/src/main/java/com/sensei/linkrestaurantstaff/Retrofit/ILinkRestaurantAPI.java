package com.sensei.linkrestaurantstaff.Retrofit;

import com.sensei.linkrestaurantstaff.Model.HotFoodModel;
import com.sensei.linkrestaurantstaff.Model.MaxOrderModel;
import com.sensei.linkrestaurantstaff.Model.OrderDetailModel;
import com.sensei.linkrestaurantstaff.Model.OrderModel;
import com.sensei.linkrestaurantstaff.Model.RestaurantOwnerModel;
import com.sensei.linkrestaurantstaff.Model.ShipperOrderModel;
import com.sensei.linkrestaurantstaff.Model.TokenModel;
import com.sensei.linkrestaurantstaff.Model.UpdateOrderModel;
import com.sensei.linkrestaurantstaff.Model.UpdateRestaurantModel;


import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ILinkRestaurantAPI {
    //GET KEY
    @GET("restaurantowner")
    Observable<RestaurantOwnerModel> getRestaurantOwner(@Query("key") String key
                                                        , @Query("fbid") String fbid);

    @POST("restaurantowner")
    @FormUrlEncoded
    Observable<UpdateRestaurantModel> updateRestaurantOwner(@Field("key") String key,
                                                            @Field("userPhone") String userPhone,
                                                            @Field("userName") String userName,
                                                            @Field("fbid") String fbid);

    @GET("orderbyrestaurant")
    Observable<OrderModel> getOrder(@Query("key") String key,
                                    @Query("restaurantId") String restaurantId,
                                    @Query("from") int from,
                                    @Query("to") int to);

    @GET("maxorderbyrestaurant")
    Observable<MaxOrderModel> getMaxOrder(@Query("key") String key,
                                          @Query("restaurantId") String restaurantId);

    @GET("orderdetailbyrestaurant")
    Observable<OrderDetailModel> getOrderDetail(@Query("key") String key,
                                                @Query("orderId") int orderId);

    @GET("token")
    Observable<TokenModel> getToken (@Query("key") String key,
                                     @Query("fbid") String fbid);

    @GET("hotfood")
    Observable<HotFoodModel> getHotFood (@Query("key") String key);

    @GET("shippingorder")
    Observable<ShipperOrderModel> getShippingOrder(@Query("key") String key,
                                                   @Query("restaurantId") int restaurantId,
                                                   @Query("from") int from,
                                                   @Query("to") int to);

    @GET("maxorderneedshipbyrestaurant")
    Observable<MaxOrderModel> getMaxOrderNeedShip(@Query("key") String key,
                                          @Query("restaurantId") int restaurantId);

    //POST
    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@Field("key") String key,
                                               @Field("fbid") String fbid,
                                               @Field("orderPhone") String token);

    @POST("shippingorder")
    @FormUrlEncoded
    Observable<ShipperOrderModel> setShippingOrder(@Field("key") String key,
                                                   @Field("orderId") int orderId,
                                                   @Field("restaurantId") int restaurantId);

    //PUT
    @PUT("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrderModel(@Field("key") String key,
                                                  @Field("orderId") int orderId,
                                                  @Field("orderStatus") int orderStatus);

}
