package com.avito.report.internal

import com.avito.http.FallbackInterceptor
import com.avito.http.RetryInterceptor
import com.avito.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.concurrent.TimeUnit

internal fun getHttpClient(verbose: Boolean, fallbackUrl: String, logger: Logger): OkHttpClient =
    OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(logger = logger))
        .addInterceptor(
            FallbackInterceptor(
                fallbackRequest = { request ->
                    request.newBuilder()
                        .url(fallbackUrl)
                        .build()
                },
                onFallback = { logger.debug("Fallback to ingress") })
        )
        .apply {
            addInterceptor(
                HttpLoggingInterceptor { logger.debug(it) }
                    .setLevel(if (verbose) Level.BODY else Level.BASIC)
            )
        }
        .build()
