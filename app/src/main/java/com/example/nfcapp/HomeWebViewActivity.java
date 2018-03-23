package com.example.nfcapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.nfcapp.config.AddressConfig;
import com.example.nfcapp.utils.GetDeviceMessage;

import java.util.List;


public class HomeWebViewActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //上次按下返回键的系统时间
    private long lastBackTime = 0;
    //当前按下返回键的系统时间
    private long currentBackTime = 0;

    private Button btnSlotCard;

    private WebView webView;

    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private String datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_web_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initNFC();
        btnSlotCard = (Button) findViewById(R.id.btn_slot_card);
        btnSlotCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nfcAdapter != null) {
                    //监听nfc设备
                    nfcAdapter.enableForegroundDispatch(HomeWebViewActivity.this, mPendingIntent, null, null);
                }
            }
        });


        String deviceTD = GetDeviceMessage.getDeviceTD(this);
        Log.i(TAG, "onCreate: deviceTD=" + deviceTD);
        Toast.makeText(this, "机型唯一ID：" + deviceTD, Toast.LENGTH_LONG).show();

        webView = (WebView) findViewById(R.id.webView);

        loadWebView(AddressConfig.homeWebViewAddress + "deviceTD");

       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadWebView("https://www.baidu.com/");
            }
        }, 4 * 1000);*/


    }


    private void initNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_SHORT).show();
            //tvTip.setText("设备不支持NFC！");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
            //tvTip.setText("请在系统设置中先启用NFC功能！");
            return;
        }
        //创建intent检测nfc
        mPendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //onResume
        if (this.nfcAdapter == null)
            return;
        if (!this.nfcAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
            //tvTip.setText("请在系统设置中先启用NFC功能！");
        }

        //监听nfc设备
        Log.i(TAG, "onPause: 开始查找");
        this.nfcAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, null);

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            Log.i(TAG, "onPause: 关闭查找");
            nfcAdapter.disableForegroundDispatch(this);
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);

    }

    private void loadWebView(String url) {
        webView.loadUrl(url);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //view.loadUrl(String.format(Locale.CHINA, "javascript:document.body.style.paddingTop='%fpx'; void 0", DensityUtil.px2dp(webView.getPaddingTop())));
            }
        });
    }

    protected void resolveIntent(Intent intent) {

        // 得到是否检测到TAG触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            // 处理该intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // 获取标签id数组
            byte[] bytesId = tag.getId();

            //获取消息内容
            NfcMessageParser nfcMessageParser = new NfcMessageParser(intent);
            List<String> tagMessage = nfcMessageParser.getTagMessage();

            if (tagMessage == null || tagMessage.size() == 0) {

                Toast.makeText(this, "NFC格式不支持...", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < tagMessage.size(); i++) {
                    Log.e("tag", tagMessage.get(i));
                }
                datas = tagMessage.get(0);
            }
            String info = "";
            String cardId="";
            if (datas != null) {
                info += "内容：" + datas + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
                cardId=bytesToHexString(bytesId);
            } else {
                info += "内容：空" + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
                cardId=bytesToHexString(bytesId);
            }


            String[] techList = tag.getTechList();

            //分析NFC卡的类型： Mifare Classic/UltraLight Info
            String cardType = "";


            for (String aTechList : techList) {

                if (TextUtils.equals(aTechList, "android.nfc.tech.Ndef")) {
                    Ndef ndef = Ndef.get(tag);
                    cardType += "最大数据尺寸:" + ndef.getMaxSize() + "字节";
                }
            }

            info += cardType;

            Toast.makeText(this, "NFC信息如下：" + info, Toast.LENGTH_SHORT).show();
            //loadWebView(AddressConfig.NFCWebViewAddress + cardId);
            // tvTip.setText("NFC信息如下：\n" + info);


        }
    }

    /**
     * 数组转换成十六进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        currentBackTime = System.currentTimeMillis();
        if (currentBackTime - lastBackTime > 2 * 1000) {
            Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();
            lastBackTime = currentBackTime;
        } else {
            super.onBackPressed();
        }

    }
}
