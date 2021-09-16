package com.l1sk1sh.vladikbot.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;

/**
 * @author l1sk1sh
 */
public final class AuthUtils {

    public static HttpHeaders createBasicAuthenticationHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
