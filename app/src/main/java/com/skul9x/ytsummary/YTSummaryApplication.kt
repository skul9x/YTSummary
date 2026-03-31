package com.skul9x.ytsummary

import android.app.Application
import com.skul9x.ytsummary.manager.PythonManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Giai đoạn 01: Thiết lập Application class để quản lý vòng đời ứng dụng
 * và khởi chạy các tác vụ nặng (Warm up Python) sớm nhất có thể mà không block Main Thread.
 */
class YTSummaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Cấu hình mạng (Bật cache) - Phase 04
        com.skul9x.ytsummary.di.NetworkModule.initialize(this)
        
        // Tránh giật lag 2-5s khi mở app bằng cách Warm up Python ở background thread
        CoroutineScope(Dispatchers.Default).launch {
            PythonManager.warmUp(this@YTSummaryApplication)
        }
    }
}
