package com.r4sh33d.musicslam.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class LastFmRetrofitClient {
    public static String API_KEY = "0bc709225c814b576681ff35ca7ea054";
    public static String Base_URL = "http://ws.audioscrobbler.com/";
    private static int VALID_CACHE_DURATION = 60 * 24 * 60 * 60; //60 days
    private static Retrofit sRetrofitInstance;

    public static Retrofit getsRetrofitInstance(Context context, long networkTimeout) {
        if (sRetrofitInstance == null) {
            sRetrofitInstance = new LastFmRetrofitClient().build(context, networkTimeout);
        }
        return sRetrofitInstance;
    }

    public static LastFmService getLastFmRetrofitService(Context context, long networkTimeout) {
        return LastFmRetrofitClient.getsRetrofitInstance(context, networkTimeout).create(LastFmService.class);
    }

    public static OkHttpClient getHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                //  .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("api_key", API_KEY)
                            .addQueryParameter("format", "json")
                            .build();
                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url);
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .build();
    }

    private Retrofit build(Context context, long networkTimeout) {
        return new Retrofit.Builder()
                .baseUrl(Base_URL)
                .client(getCacheEnabledHttClient(context, networkTimeout))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Nullable
    private static Cache getCache(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "/okhttp_cache/");
        if (cacheDirectory.mkdirs() || cacheDirectory.isDirectory()) {
            return new Cache(cacheDirectory, cacheSize);
        }
        return null;
    }

    public static void clearCache(Context context) {
        Cache cache = getCache(context);
        if (cache == null) {
            return;
        }
        try {
            cache.evictAll();
        } catch (IOException e) {
            Timber.d(e);
        }
    }

    private OkHttpClient getCacheEnabledHttClient(Context context, long networkTimeOut) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        @SuppressLint("DefaultLocale") OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(getCache(context))
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("api_key", API_KEY)
                            .addQueryParameter("format", "json")
                            .build();
                    Request.Builder requestBuilder = original.newBuilder().url(url);
                    requestBuilder.addHeader("Cache-Control", String.format("public, max-stale=%d, max-age=%d",
                            VALID_CACHE_DURATION, VALID_CACHE_DURATION));
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                });
        if (networkTimeOut > 0) {
            builder.readTimeout(networkTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(networkTimeOut, TimeUnit.MILLISECONDS)
                    .connectTimeout(networkTimeOut, TimeUnit.MILLISECONDS);
        }
        return builder.build();
    }
}
