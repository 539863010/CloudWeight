package xm.cloudweight.camera.service;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by black on 2017/5/11.
 */
public interface CameraCgi {

    @FormUrlEncoded
    @POST("cgi-bin/time.cgi")
    Observable<ResponseBody> setDate(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("cgi-bin/osd.cgi")
    Observable<ResponseBody> updateChanel(@FieldMap(encoded = true) Map<String, String> fields);

    @FormUrlEncoded
    @POST("cgi-bin/osdoverlay.cgi")
    Observable<ResponseBody> updateDisplay(@FieldMap(encoded = true) Map<String, String> fields);

    @GET("onvif/snapshot")
    Observable<ResponseBody> screenshot();

//    @FormUrlEncoded
//    @POST("cgi-bin/time.cgi")
//    Flowable<ResponseBody> setDate(@FieldMap Map<String, String> fields);
//
//    @FormUrlEncoded
//    @POST("cgi-bin/osd.cgi")
//    Flowable<ResponseBody> updateChanel(@FieldMap(encoded = true) Map<String, String> fields);
//
//    @FormUrlEncoded
//    @POST("cgi-bin/osdoverlay.cgi")
//    Flowable<ResponseBody> updateDisplay(@FieldMap(encoded = true) Map<String, String> fields);
//
//    @GET("onvif/snapshot")
//    Flowable<ResponseBody> screenshot();
}
