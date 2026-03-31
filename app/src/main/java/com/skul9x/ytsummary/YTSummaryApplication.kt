package com.skul9x.ytsummary

import android.app.Application

/**
 * Application class để quản lý vòng đời ứng dụng.
 * 
 * Migration v5.0: Loại bỏ Python warm-up (không còn Chaquopy runtime).
 * NetworkModule vẫn được khởi tạo sớm để OkHttp sẵn sàng cho transcript requests.
 */
class YTSummaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Cấu hình mạng (Bật cache) - Phase 04
        com.skul9x.ytsummary.di.NetworkModule.initialize(this)
    }
}
