package com.reyurnible.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.android.volley.toolbox.HurlStack;

public class MyHurlStack extends HurlStack {
	
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {

        //プロキシサーバーを指定して接続を開始します
        Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("set.your.proxy", 8080));
        HttpURLConnection returnThis = (HttpURLConnection) url.openConnection(proxy);

        //ユーザーエージェントを設定します
        returnThis.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        return returnThis;
    }
}