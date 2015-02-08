package com.reyurnible.syncronizeddrivind.controller.provider;import com.android.volley.Cache;import com.android.volley.NetworkResponse;import com.android.volley.toolbox.HttpHeaderParser; public class ImageHttpHeaderParser extends HttpHeaderParser {    /*     * ネットワークリソースの更新確認をせずに、キャッシュの鮮度のみでキャッシュが再利用される期間。     * この時間を過ぎた場合は、キャッシュが利用される一方で、ネットワークリソースの更新確認もされる。     * 期限を5日間に設定 5*(((1000)s*60)m*60)h*24)d     */    private static final long USE_CACHE_WITHOUT_CHECKING_FRESHNESS = 5 * 1000 * 60 * 60 * 24;    /*     * キャッシュエントリーが有効な期間     * この時間を過ぎると、キャッシュのエントリー（存在）自体が削除される。     * 期限を6日間に設定 6*(((1000)s*60)m*60)h*24)d     */    private static final long DURATION_OF_CACHE_ITSELF = 6 * 1000 * 60 * 60 * 24;     public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {        Cache.Entry entry = new Cache.Entry();         // Cache の TTL に関する情報        long now = System.currentTimeMillis();        entry.softTtl = now + USE_CACHE_WITHOUT_CHECKING_FRESHNESS;        entry.ttl = now + DURATION_OF_CACHE_ITSELF;         // サーバー側データのタイムスタンプ        String headerDate = response.headers.get("Date");        long headerDateAsEpoch = 0;        if (headerDate != null) {            headerDateAsEpoch = parseDateAsEpoch(headerDate);        }        entry.serverDate = headerDateAsEpoch;         // ETag        entry.etag = response.headers.get("ETag");         // ヘッダー部全体        entry.responseHeaders = response.headers;         // データ本体        entry.data = response.data;         return entry;    }}