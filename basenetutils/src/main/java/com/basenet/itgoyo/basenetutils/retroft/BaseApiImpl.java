package com.basenet.itgoyo.basenetutils.retroft;

import android.util.Log;

import com.basenet.itgoyo.basenet.utils.DiyLoggingInterceptor;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/*
 * 项目名:    BaseFrame
 * 包名       com.zhon.baselib.retroft
 * 文件名:    BaseApiImpl
 * 创建者:    ZJB
 * 创建时间:  2017/9/7 on 10:12
 * 描述:     TODO
 */
public class BaseApiImpl implements BaseApi {
    private volatile static Retrofit retrofit = null;
    protected Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
//    protected OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
    protected OkHttpClient.Builder httpBuilder = getUnsafeOkHttpClient().newBuilder();

    public BaseApiImpl(String baseUrl) {

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        retrofitBuilder.addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setLenient()
                        .create()
                ))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpBuilder.addInterceptor(new DiyLoggingInterceptor().setLevel(DiyLoggingInterceptor.Level.BODY)).build())


                /*.client(httpBuilder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        //获得请求信息，此处如有需要可以添加headers信息
                        Request request = chain.request();
                        //添加Cookie信息
                        request.newBuilder().addHeader("Cookie","aaaa");
                        //打印请求信息
                        syso("url:" + request.url());
                        syso("method:" + request.method());
                        syso("request-body:" + request.body());

                        //记录请求耗时
                        long startNs = System.nanoTime();
                        okhttp3.Response response;
                        try {
                            //发送请求，获得相应，
                            response = chain.proceed(request);
                        } catch (Exception e) {
                            throw e;
                        }
                        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                        //打印请求耗时
                        syso("耗时:"+tookMs+"ms");
                        //使用response获得headers(),可以更新本地Cookie。
                        syso("headers==========");
                        Headers headers = response.headers();
                        syso(headers.toString());

                        //获得返回的body，注意此处不要使用responseBody.string()获取返回数据，原因在于这个方法会消耗返回结果的数据(buffer)
                        ResponseBody responseBody = response.body();

                        //为了不消耗buffer，我们这里使用source先获得buffer对象，然后clone()后使用
                        BufferedSource source = responseBody.source();
                        source.request(Long.MAX_VALUE); // Buffer the entire body.
                        //获得返回的数据
                        Buffer buffer = source.buffer();
                        //使用前clone()下，避免直接消耗
                        syso("response:" + buffer.clone().readString(Charset.forName("UTF-8")));
                        return response;
                    }
                }).build())*/
//                .client(httpBuilder.addInterceptor(getLoggerInterceptor()).build())
                .baseUrl(baseUrl);
    }

    private static void syso(String msg) {
        System.out.println(msg);
    }



    /**
     * 构建retroft
     *
     * @return Retrofit对象
     */
    @Override
    public Retrofit getRetrofit() {
        if (retrofit == null) {
            //锁定代码块
            synchronized (BaseApiImpl.class) {
                if (retrofit == null) {
                    retrofit = retrofitBuilder.build(); //创建retrofit对象
                }
            }
        }
        return retrofit;

    }


    @Override
    public OkHttpClient.Builder setInterceptor(Interceptor interceptor) {
        return httpBuilder.addInterceptor(interceptor);
    }

    @Override
    public Retrofit.Builder setConverterFactory(Converter.Factory factory) {
        return retrofitBuilder.addConverterFactory(factory);
    }

    /**
     * 日志拦截器
     * 将你访问的接口信息
     *
     * @return 拦截器
     */
    public HttpLoggingInterceptor getLoggerInterceptor() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("ApiUrl", "--->" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        return loggingInterceptor;
    }

    /**
     *  忽略证书
     * @return 访问Https
     */
    public static OkHttpClient getUnsafeOkHttpClient() {

        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
