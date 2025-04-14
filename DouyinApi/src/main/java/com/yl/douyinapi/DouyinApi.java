package com.yl.douyinapi;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//import com.aliyun.tea.TeaException;
import com.bytedance.sdk.open.aweme.authorize.model.Authorization;
import com.bytedance.sdk.open.aweme.init.DouYinOpenSDKConfig;
import com.bytedance.sdk.open.douyin.DouYinOpenApiFactory;
import com.bytedance.sdk.open.douyin.api.DouYinOpenApi;
//import com.douyin.openapi.client.Client;
//import com.douyin.openapi.client.models.CouponQueryCouponMetaRequest;
//import com.douyin.openapi.client.models.CouponQueryCouponMetaResponse;
//import com.douyin.openapi.client.models.OauthClientTokenRequest;
//import com.douyin.openapi.client.models.OauthClientTokenResponse;
//import com.douyin.openapi.credential.models.Config;

public class DouyinApi {

    private static final String DOUYIN_CLIENT_KEY = "awovf4ro2byxcgj1";
    private static boolean isUserAgreePrivacyPolicy = true; //用户是否同意隐私政策
    private DouYinOpenApi douYinOpenApi;

    public DouyinApi(Activity activity) {
        requestAuth(activity);
    }

    public static void init(Context context) {
        boolean isUserAgreePrivacyPolicy = true; //用户是否同意隐私政策，请替换成真实的逻辑判断

        DouYinOpenSDKConfig douYinOpenSDKConfig = new DouYinOpenSDKConfig.Builder()
                .context(context) //此处注入context
                .clientKey(DOUYIN_CLIENT_KEY) //此处填写申请的应用client_key
                .autoStartTracker(isUserAgreePrivacyPolicy) // 是否自动启动埋点上报
                .build();
        DouYinOpenApiFactory.initConfig(douYinOpenSDKConfig);

//        try {
//            Config config = new Config().setClientKey("awovf4ro2byxcgj1").setClientSecret("38ac8d9d42151824f390f42af838deac");
//            Client client = new Client(config);
//
//            CouponQueryCouponMetaRequest sdkRequest = new CouponQueryCouponMetaRequest()
//                    .setCouponMetaId("test_id")
//                    .setBizType(new Integer(1))
//                    .setAccessToken("test_access_token");
//            CouponQueryCouponMetaResponse sdkResponse = client.CouponQueryCouponMeta(sdkRequest);
//            System.out.println(sdkResponse);
//        } catch (TeaException e) {
//            System.out.println(e.getMessage());
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }

    }

    public static void requestAuth(Activity activity) {
//        DouYinOpenApi douYinOpenApi = DouYinOpenApiFactory.create(activity);
//        Authorization.Request request = new Authorization.Request();
//
//        douYinOpenApi.authorize(request);
//        try {
//            Config config = new Config().setClientKey("awovf4ro2byxcgj1").setClientSecret("38ac8d9d42151824f390f42af838deac"); // 改成自己的app_id跟secret
//            Client client = new Client(config);
//            OauthClientTokenRequest sdkRequest = new OauthClientTokenRequest();
//            sdkRequest.setClientKey("awovf4ro2byxcgj1");
//            sdkRequest.setClientSecret("38ac8d9d42151824f390f42af838deac");
//            sdkRequest.setGrantType("client_credential");
//            OauthClientTokenResponse sdkResponse = client.OauthClientToken(sdkRequest);
//            Log.e("TAG", "requestAuth: " + sdkResponse.getMessage() + sdkResponse.data.accessToken + ":: error: " + sdkResponse.data.getErrorCode());
//        } catch (TeaException e) {
//            System.out.println(e.getMessage());
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
    }

}
