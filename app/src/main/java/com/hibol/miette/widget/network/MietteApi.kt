package com.hibol.miette.widget.network

import com.hibol.miette.widget.model.Recipe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface MietteApi {
    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): Recipe
}

object MietteApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://chez-miette.xyz/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: MietteApi = retrofit.create(MietteApi::class.java)
}