package com.pim.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Singleton API client for communicating with the PIM backend.
 */
public class PimApiClient {

    // For Android Emulator: 10.0.2.2 maps to host machine's localhost
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    // For physical device: use your computer's local IP (run 'ipconfig' to find it)
    // private static final String BASE_URL = "http://192.168.x.x:3000/";

    private static PimApiClient instance;
    private final PimApiService api;

    private PimApiClient() {
        // Logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(PimApiService.class);
    }

    public static synchronized PimApiClient getInstance() {
        if (instance == null) {
            instance = new PimApiClient();
        }
        return instance;
    }

    public PimApiService getApi() {
        return api;
    }
}
