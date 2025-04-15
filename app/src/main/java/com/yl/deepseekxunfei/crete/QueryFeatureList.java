package com.yl.deepseekxunfei.crete;

import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 查询音频特征列表
 */
public class QueryFeatureList {
    private String requestUrl;
    private String APPID;
    private String apiSecret;
    private String apiKey;
    private CreateLogotype createLogotype;

    //解析Json
    private static Gson json = new Gson();

    //构造函数,为成员变量赋值
    public QueryFeatureList(String requestUrl,String APPID,String apiSecret,String apiKey,CreateLogotype createLogotype){
        this.requestUrl=requestUrl;
        this.APPID=APPID;
        this.apiSecret=apiSecret;
        this.apiKey=apiKey;
        this.createLogotype = createLogotype;
    }

    //提供给主函数调用的方法
    public static void doQueryFeatureList(String requestUrl,String APPID,String apiSecret,String apiKey,CreateLogotype createLogotype){
        QueryFeatureList queryFeatureList = new QueryFeatureList(requestUrl,APPID,apiSecret,apiKey,createLogotype);
        try {
            //queryFeatureListRes
            String resp = queryFeatureList.doRequest();

            Log.d("查询","resp=>"+resp);
            JsonParse myJsonParse = json.fromJson(resp, JsonParse.class);
            String textBase64Decode= null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textBase64Decode = new String(Base64.getDecoder().decode(myJsonParse.payload.queryFeatureListRes.text), "UTF-8");
            }
            JSONArray jSONArray = JSON.parseArray(textBase64Decode);
            Log.d("查询","text字段Base64解码后=>"+jSONArray);
        } catch (Exception e) {
            Log.d("错误", "doQueryFeatureList: "+e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 请求主方法
     * @return 返回服务结果
     * @throws Exception 异常
     */
    public String doRequest() throws Exception {
        URL realUrl = new URL(buildRequetUrl());
        URLConnection connection = realUrl.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-type","application/json");

        OutputStream out = httpURLConnection.getOutputStream();
        String params = buildParam();
        System.out.println("params=>"+params);
        out.write(params.getBytes());
        out.flush();
        InputStream is = null;
        try{
            is = httpURLConnection.getInputStream();
        }catch (Exception e){
            is = httpURLConnection.getErrorStream();
            throw new Exception("make request error:"+"code is "+httpURLConnection.getResponseMessage()+readAllBytes(is));
        }
        return readAllBytes(is);
    }

    /**
     * 处理请求URL
     * 封装鉴权参数等
     * @return 处理后的URL
     */
    public String buildRequetUrl(){
        URL url = null;
        // 替换调schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String  httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://","https://" );
        try {
            url = new URL(httpRequestUrl);
            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());

            String host = url.getHost();
            if (url.getPort()!=80 && url.getPort() !=443){
                host = host +":"+String.valueOf(url.getPort());
            }
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append("POST ").append(url.getPath()).append(" HTTP/1.1");
            Charset charset = Charset.forName("UTF-8");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sha = Base64.getEncoder().encodeToString(hexDigits);
            }

            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            String authBase = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            }
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:"+e.getMessage());
        }
    }

    /**
     * 组装请求参数
     * 直接使用示例参数，
     * 替换部分值
     * @return 参数字符串
     */
    private String  buildParam() {
        String param = "{"+
                "    \"header\": {"+
                "        \"app_id\": \""+APPID+"\","+
                "        \"status\": 3"+
                "    },"+
                "    \"parameter\": {"+
                "        \"s782b4996\": {"+
                "            \"func\": \"queryFeatureList\","+
                //这里填上所需要的groupId，如多个组名注意变更
                "            \"groupId\": \""+createLogotype.getGroupId()+"\","+
                "            \"queryFeatureListRes\": {"+
                "                \"encoding\": \"utf8\","+
                "                \"compress\": \"raw\","+
                "                \"format\": \"json\""+
                "            }"+
                "        }"+
                "    }"+
                "}";
        return param;
    }

    /**
     * 读取流数据
     * @param is 流
     * @return 字符串
     * @throws IOException 异常
     */
    private String readAllBytes(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len = 0;
        while ((len = is.read(b)) != -1){
            sb.append(new String(b, 0, len, "utf-8"));
        }
        return sb.toString();
    }
    //Json解析
    class JsonParse {
        public Header header;
        public Payload payload;
    }
    class Header{
        public int code;
        public String message;
        public String sid;
        public int status;
    }
    class Payload{
        //根据model的取值不同,名字有所变动。
        public QueryFeatureListRes queryFeatureListRes;
    }
    class QueryFeatureListRes{
        public String compress;
        public String encoding;
        public String format;
        public String text;
    }
}
