package com.pim.api;

import com.pim.model.ChatRequest;
import com.pim.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit interface for the PIM Backend API.
 */
public interface PimApiService {

    /**
     * Send a chat message to the backend for AI processing.
     *
     * @param request Contains sender and message
     * @return AI-generated response
     */
    @POST("chat")
    Call<ChatResponse> chat(@Body ChatRequest request);
}
