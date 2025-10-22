package com.allo.utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlUtils {
    public static String updateUrlParams(String url, String key, String value) {
        if (url == null || key == null || value == null) return url;

        // 1. 分离主 URL 和参数部分
        String[] parts = url.split("\\?");
        String baseUrl = parts[0];
        String paramString = parts.length > 1 ? parts[1] : "";

        // 2. 解析参数
        Map<String, String> paramMap = new LinkedHashMap<>();
        for (String param : paramString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            }
        }

        // 3. 添加或修改参数
        paramMap.put(key, URLEncoder.encode(value));

        // 4. 重新拼接 URL
        StringBuilder newUrl = new StringBuilder(baseUrl + "?");
        boolean first = true;
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (!first) {
                newUrl.append("&");
            }
            newUrl.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        // 去掉最后的 "&"
        return newUrl.toString();
    }
}
