package com.example.utils

import com.example.data.ShortcutEntity

object PresetTemplates {

    val presets = listOf(
        ShortcutEntity(
            alias = "钉钉极速打卡 (含Extra参数)",
            intentUri = "intent://qr.dingtalk.com/ding/home.html#Intent;scheme=https;launchFlags=0x34000000;extendedLaunchFlags=0x4;package=com.alibaba.android.rimet;component=com.alibaba.android.rimet/.biz.LaunchHomeActivity;S.to_page=to_web;S.url=dingtalk://dingtalkclient/page/link?url=https%3A%2F%2Fattend.dingtalk.com%2Fattend%2Findex.html%3Ffrom%3DandroidDesktopWidget;end",
            iconName = "check",
            useRoot = true,
            tileSlot = 1,
            category = "应用跳转"
        ),
        ShortcutEntity(
            alias = "支付宝扫一扫 (Scheme)",
            intentUri = "alipays://platformapi/startapp?appId=10000007",
            iconName = "bolt",
            useRoot = true,
            tileSlot = 2,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "支付宝付款码 (Scheme)",
            intentUri = "alipays://platformapi/startapp?appId=20000056",
            iconName = "flash",
            useRoot = true,
            tileSlot = 3,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "支付宝乘车码 (Scheme)",
            intentUri = "alipays://platformapi/startapp?appId=200011235",
            iconName = "power",
            useRoot = false,
            tileSlot = 0,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "微信扫一扫 (Scheme)",
            intentUri = "weixin://dl/scan",
            iconName = "shield",
            useRoot = true,
            tileSlot = 4,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "高德地图一键导航回家 (Scheme)",
            intentUri = "androidamap://route/plan/?sourceApplication=QuickTile&dname=Home&dev=0&t=0",
            iconName = "star",
            useRoot = false,
            tileSlot = 0,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "哔哩哔哩动态 (Scheme)",
            intentUri = "bilibili://main/dynamic",
            iconName = "favorite",
            useRoot = false,
            tileSlot = 0,
            category = "常用Scheme"
        ),
        ShortcutEntity(
            alias = "MIUI/HyperOS 直连录音 (JumpReplay)",
            intentUri = "intent:#Intent;action=miui.intent.action.DIRECT_RECORD;launchFlags=0x14808000;extendedLaunchFlags=0x4;component=com.android.soundrecorder/.SoundRecorder;end",
            iconName = "mic",
            useRoot = true,
            tileSlot = 5,
            category = "JumpReplay"
        ),
        ShortcutEntity(
            alias = "系统-开发者选项 (Developer Options)",
            intentUri = "intent:#Intent;action=android.settings.APPLICATION_DEVELOPMENT_SETTINGS;end",
            iconName = "terminal",
            useRoot = false,
            tileSlot = 0,
            category = "系统设置"
        ),
        ShortcutEntity(
            alias = "小米电池与性能 (Power Settings)",
            intentUri = "intent:#Intent;action=android.intent.action.MAIN;component=com.miui.securitycenter/com.miui.powercenter.PowerSettings;end",
            iconName = "power",
            useRoot = true,
            tileSlot = 0,
            category = "系统工具"
        ),
        ShortcutEntity(
            alias = "系统应用管理 (Manage All Apps)",
            intentUri = "intent:#Intent;action=android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS;end",
            iconName = "wrench",
            useRoot = false,
            tileSlot = 0,
            category = "系统设置"
        ),
        ShortcutEntity(
            alias = "通知历史记录 (Notification History)",
            intentUri = "intent:#Intent;action=android.settings.NOTIFICATION_HISTORY;end",
            iconName = "notifications",
            useRoot = false,
            tileSlot = 0,
            category = "系统设置"
        ),
        ShortcutEntity(
            alias = "手机手电筒开关 (System Flashlight)",
            intentUri = "intent:#Intent;action=android.intent.action.MAIN;component=com.android.systemui/.flashlight.FlashlightHandler;end",
            iconName = "flash",
            useRoot = true,
            tileSlot = 0,
            category = "快捷开关"
        ),
        ShortcutEntity(
            alias = "系统权限管理 (App Permissions)",
            intentUri = "intent:#Intent;action=android.intent.action.MANAGE_APP_PERMISSIONS;end",
            iconName = "shield",
            useRoot = false,
            tileSlot = 0,
            category = "系统安全"
        ),
        ShortcutEntity(
            alias = "系统性能模式切换 (Performance Mode)",
            intentUri = "intent:#Intent;action=com.miui.powercenter.PERFORMANCE_MODE;component=com.miui.securitycenter/com.miui.powercenter.PowerModeReceiver;end",
            iconName = "flame",
            useRoot = true,
            tileSlot = 0,
            category = "系统优化"
        )
    )
}
