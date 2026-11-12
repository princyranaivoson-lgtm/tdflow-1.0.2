package com.tdflow.app

import android.os.Bundle
import android.webkit.ValueCallback
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bridge.webView.addJavascriptInterface(TDAndroidBridge(this), "TDAndroid")
        // Recharge une fois l'interface native enregistrée afin que les scripts la voient dès le démarrage.
        bridge.webView.post { bridge.webView.reload() }
    }

    @Deprecated("Deprecated in Android API 33; retained for compatibility with Capacitor's WebView back handling")
    override fun onBackPressed() {
        bridge.webView.evaluateJavascript("window.tdBack ? window.tdBack() : false", ValueCallback { handled ->
            if (handled != "true") defaultBack()
        })
    }

    private fun defaultBack() { super.onBackPressed() }
}
