//
//package com.ctg.test;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.Test;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//
//import com.qsdi.gat1400.httpclient.okhttp.digest.Credentials;
//import com.qsdi.gat1400.httpclient.okhttp.digest.DigestAuthenticator;
//
//import cn.hutool.core.codec.Base64;
//import cn.hutool.core.util.XmlUtil;
//import cn.hutool.crypto.SecureUtil;
//import okhttp3.*;
//
//public class LoginTest {
//
//    OkHttpClient okHttpClient = new OkHttpClient.Builder()
//        .connectTimeout(5, TimeUnit.SECONDS)
//            .readTimeout(5,TimeUnit.SECONDS)
//            .writeTimeout(5,TimeUnit.SECONDS)
//            .authenticator(new DigestAuthenticator(new Credentials("admin", "123456")))
//            .build();
//    @Test
//    public void hikLogin() throws IOException {
//        Response response = null;
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .get()
//                                                .url("http://192.168.1.106/ISAPI/Security/sessionLogin/capabilities?username=admin")
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String body = response.body().string();
//        System.out.println("body="+body);
//
//        String username = "admin";
//        String password = "herostart2018";
//        Map<String, Object> stringObjectMap = XmlUtil.xmlToMap(body);
//        String sha256;
//        if(Boolean.valueOf(getIsIrreversible(stringObjectMap))){
//            sha256 = SecureUtil.sha256(SecureUtil.sha256(
//                username + getSalt(stringObjectMap) + password) + getChallenge(stringObjectMap));
//            for(int i = 2; i<Integer.parseInt(getIterations(stringObjectMap)); i++){
//                sha256 = SecureUtil.sha256(sha256);
//            }
//        }else {
//            sha256 = SecureUtil.sha256("herostart2018") + getChallenge(stringObjectMap);
//            for(int i = 1; i<Integer.parseInt(getIterations(stringObjectMap)); i++){
//                sha256 = SecureUtil.sha256(sha256);
//            }
//        }
//
//        String session = String.format("<SessionLogin><userName>%s</userName><password>%s</password><sessionID>%s</sessionID></SessionLogin>", username, sha256,getSessionID(stringObjectMap));
//
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),session))
//                                                .url("http://192.168.1.106/ISAPI/Security/sessionLogin")
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        body = response.body().string();
//        System.out.println(body);
//        stringObjectMap = XmlUtil.xmlToMap(body);
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .put(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                                                    + "<StreamingChannel version=\"2.0\" xmlns=\"http://www.hikvision.com/ver20/XMLSchema\">\n"
//                                                    + "<id>101</id>\n" + "<channelName>44</channelName>\n"
//                                                    + "<enabled>true</enabled>\n" + "<Transport>\n"
//                                                    + "<maxPacketSize>1000</maxPacketSize>\n"
//                                                    + "<ControlProtocolList>\n" + "<ControlProtocol>\n"
//                                                    + "<streamingTransport>RTSP</streamingTransport>\n"
//                                                    + "</ControlProtocol>\n" + "<ControlProtocol>\n"
//                                                    + "<streamingTransport>HTTP</streamingTransport>\n"
//                                                    + "</ControlProtocol>\n" + "<ControlProtocol>\n"
//                                                    + "<streamingTransport>SHTTP</streamingTransport>\n"
//                                                    + "</ControlProtocol>\n" + "</ControlProtocolList>\n"
//                                                    + "<Unicast>\n" + "<enabled>true</enabled>\n"
//                                                    + "<rtpTransportType>RTP/TCP</rtpTransportType>\n" + "</Unicast>\n"
//                                                    + "<Multicast>\n" + "<enabled>true</enabled>\n"
//                                                    + "<destIPAddress>0.0.0.0</destIPAddress>\n"
//                                                    + "<videoDestPortNo>8860</videoDestPortNo>\n"
//                                                    + "<audioDestPortNo>8862</audioDestPortNo>\n" + "</Multicast>\n"
//                                                    + "<Security>\n" + "<enabled>true</enabled>\n"
//                                                    + "<certificateType>digest/basic</certificateType>\n"
//                                                    + "</Security>\n" + "</Transport>\n" + "<Video>\n"
//                                                    + "<enabled>true</enabled>\n"
//                                                    + "<videoInputChannelID>1</videoInputChannelID>\n"
//                                                    + "<videoCodecType>H.264</videoCodecType>\n"
//                                                    + "<videoScanType>progressive</videoScanType>\n"
//                                                    + "<videoResolutionWidth>1920</videoResolutionWidth>\n"
//                                                    + "<videoResolutionHeight>1080</videoResolutionHeight>\n"
//                                                    + "<videoQualityControlType>CBR</videoQualityControlType>\n"
//                                                    + "<constantBitRate>4096</constantBitRate>\n"
//                                                    + "<fixedQuality>60</fixedQuality>\n"
//                                                    + "<maxFrameRate>2500</maxFrameRate>\n"
//                                                    + "<keyFrameInterval>2000</keyFrameInterval>\n"
//                                                    + "<snapShotImageType>JPEG</snapShotImageType>\n"
//                                                    + "<H264Profile>Main</H264Profile>\n"
//                                                    + "<GovLength>50</GovLength>\n" + "<PacketType>PS</PacketType>\n"
//                                                    + "<PacketType>RTP</PacketType>\n" + "<smoothing>50</smoothing>\n"
//                                                    + "</Video>\n" + "</StreamingChannel>"))
//                                                .url("http://192.168.1.106/ISAPI/Streaming/channels/101")
//                                                .header("Cookie", String.format("WebSession=%s",getSessionID(stringObjectMap)))
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(response.body().string());
//
//    }
//
//    private String getSessionID(Map<String, Object> stringObjectMap) {
//        return stringObjectMap.get("sessionID").toString();
//    }
//
//    private String getIterations(Map<String, Object> stringObjectMap) {
//        return stringObjectMap.get("iterations").toString();
//    }
//
//    private String getChallenge(Map<String, Object> stringObjectMap) {
//        return stringObjectMap.get("challenge").toString();
//    }
//
//    private String getSalt(Map<String, Object> stringObjectMap) {
//        return stringObjectMap.get("salt").toString();
//    }
//
//    private String getIsIrreversible(Map<String, Object> stringObjectMap) {
//        return stringObjectMap.get("isIrreversible").toString();
//    }
//
//    @Test
//    public void unviewLogin() throws IOException {
//        Response response = null;
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                          .get()
//                                          .url("http://192.168.1.101/LAPI/V1.0/Channel/0/System/Users?randomKey="+System.currentTimeMillis())
//                                          .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("body="+response.body().string());
//    }
//
//    @Test
//    public void dahuaLogin() throws IOException {
//        String username = "admin";
//        String password = "qsdi2020";
//        String loginType = "Direct";
//        String clientType = "Web3.0";
//        Response response = null;
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
//                                                                         String.format(
//                                                                             "{\"method\":\"global.login\",\"params\":{\"userName\":\"%s\",\"password\":\"\",\"clientType\":\"%s\",\"loginType\":\"%s\"},\"id\":4166}", username,clientType,loginType)))
//                                                .url("http://192.168.1.109/RPC2_Login")
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String body = response.body().string();
//        System.out.println("body="+body);
//
//        JSONObject jsonObject = JSON.parseObject(body);
//        String session = jsonObject.getString("session");
//        JSONObject params = jsonObject.getJSONObject("params");
//        String realm =params.getString("realm");
//        String random = params.getString("random");
//        String encryption =params.getString("encryption");
//
//        String encPassword;
//        if("Default".equals(encryption)){
//            encPassword = SecureUtil.md5(username+":"+random+":"+SecureUtil.md5(username+":"+realm+":"+password).toUpperCase()).toUpperCase();
//        }else if("Basic".equals(encryption)){
//            encPassword = Base64.encode(username+":"+password);
//        }else {
//            encPassword = password;
//        }
//
//        String req = String.format(
//            "{\"method\":\"global.login\",\"params\":{\"userName\":\"%s\",\"password\":\"%s\",\"clientType\":\"%s\",\"loginType\":\"%s\",\"authorityType\":\"%s\"},\"id\":8964,\"session\":\"%s\"}", username,encPassword,clientType,loginType,encryption,session);
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),req))
//                                                .url("http://192.168.1.109/RPC2_Login")
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        body = response.body().string();
//        System.out.println(body);
//
//        try {
//            response = okHttpClient.newCall(new Request.Builder()
//                                                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
//                                                                         String.format(
//                                                                             "{\"method\":\"userManager.getGroupInfoAll\",\"params\":null,\"id\":8650,\"session\":\"%s\"}", session)))
//                                                .url("http://192.168.1.109/RPC2")
//                                                .build()
//            ).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        body = response.body().string();
//        System.out.println(body);
//
//    }
//
//
//    @Test
//    public void test() throws IOException {
//
//        System.out.println(Math.random());
//        System.out.println(System.currentTimeMillis());
//
//        double v = Math.random() * System.currentTimeMillis();
//        System.out.println(v);
//
//    }
//
//}
