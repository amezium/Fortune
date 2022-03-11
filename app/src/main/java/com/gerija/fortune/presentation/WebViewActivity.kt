package com.gerija.fortune.presentation

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.webkit.WebSettings.LOAD_DEFAULT
import androidx.appcompat.app.AppCompatActivity
import com.gerija.fortune.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    lateinit var binding:ActivityWebViewBinding

    private var fileData: ValueCallback<Uri>? = null
    private var filePath: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 10000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView() = with(binding){

        webViewId.loadUrl("http://html5test.com")
        webViewId.webViewClient = WebViewClient()

        webViewId.settings.javaScriptEnabled = true

        webViewId.webChromeClient = ChromeClient()
        webViewId.clearCache(false)
        webViewId.settings.cacheMode = LOAD_DEFAULT

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webViewId, true)

    }

    private inner class ChromeClient : WebChromeClient() {

        // For Android >= 5.0
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            filePath = filePathCallback
            openImageChooserActivity()
            return true
        }
    }

    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(i, FILE_CHOOSER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (null == fileData && null == filePath) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (filePath != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (fileData != null) {
                fileData!!.onReceiveValue(result)
                fileData = null
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_REQUEST_CODE || filePath == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount){
                            i -> clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        filePath!!.onReceiveValue(results)
        filePath = null
    }

    override fun onBackPressed() {
        if (binding.webViewId.canGoBack()) {
            binding.webViewId.goBack()
        } else super.onBackPressed()
    }
}