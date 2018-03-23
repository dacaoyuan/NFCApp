package com.example.nfcapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NFCReadActivity extends AppCompatActivity {
    private static final String TAG = "NFCReadActivity";

    private TextView tvTip;
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;

    String datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        tvTip = (TextView) findViewById(R.id.tv_tip);

        initNFC();

    }

    private void initNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            tvTip.setText("设备不支持NFC！");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            tvTip.setText("请在系统设置中先启用NFC功能！");
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
            tvTip.setText("请在系统设置中先启用NFC功能！");
        }

        //开启监听nfc设备
        this.nfcAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            Log.i(TAG, "onPause: 关闭监听nfc设备");
            nfcAdapter.disableForegroundDispatch(this);
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);

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
            if (datas != null) {
                info += "内容：" + datas + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
            } else {
                info += "内容：空" + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
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

            tvTip.setText("NFC信息如下：\n" + info);


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


}
