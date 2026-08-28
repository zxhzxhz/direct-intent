package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.utils.IntentLauncher
import kotlinx.coroutines.launch

class ShortcutLauncherActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SHORTCUT_ID = "extra_shortcut_id"
        const val EXTRA_ALIAS = "extra_alias"
        const val EXTRA_INTENT_URI = "extra_intent_uri"
        const val EXTRA_USE_ROOT = "extra_use_root"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alias = intent.getStringExtra(EXTRA_ALIAS) ?: "快捷指令"
        val intentUri = intent.getStringExtra(EXTRA_INTENT_URI)
        val useRoot = intent.getBooleanExtra(EXTRA_USE_ROOT, true)

        if (intentUri.isNullOrBlank()) {
            Toast.makeText(this, "快捷指令 URI 为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val result = IntentLauncher.launch(
                context = applicationContext,
                intentUriString = intentUri,
                forceRoot = useRoot
            )

            if (result.success) {
                val mode = if (result.isRootUsed) "Root" else "标准"
                Toast.makeText(
                    applicationContext,
                    "⚡ [$alias] $mode 触发成功",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "❌ [$alias] 启动失败: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }
    }
}
