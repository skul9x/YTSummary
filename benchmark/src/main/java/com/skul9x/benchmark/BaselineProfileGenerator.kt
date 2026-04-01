package com.skul9x.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Thư mục đầu ra của profile:
 * benchmark/build/outputs/connected_android_test_additional_output/benchmark/BaselineProfileGenerator_generate-baseline-prof.txt
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generate() = baselineRule.collect(
        packageName = "com.skul9x.ytsummary",
        includeInStartupProfile = true
    ) {
        // 1. Phủ Startup
        pressHome()
        startActivityAndWait()

        // 2. Phủ luồng Main Screen
        device.wait(Until.hasObject(By.textContains("YT Summary AI")), 5000)
        device.waitForIdle()

        // 3. Phủ luồng History
        val historyBtn = device.findObject(By.desc("History"))
        historyBtn?.click()
        device.wait(Until.hasObject(By.text("History")), 3000)
        device.waitForIdle()

        // 4. Quay lại Main
        val backBtn = device.findObject(By.desc("Back"))
        backBtn?.click()
        device.waitForIdle()
        
        // 5. Phủ luồng Settings
        val settingsBtn = device.findObject(By.desc("Settings"))
        settingsBtn?.click()
        device.wait(Until.hasObject(By.text("Settings")), 3000)
        device.waitForIdle()
        
        // Quay lại
        val backBtnSettings = device.findObject(By.desc("Back"))
        backBtnSettings?.click()
        device.waitForIdle()
    }
}
