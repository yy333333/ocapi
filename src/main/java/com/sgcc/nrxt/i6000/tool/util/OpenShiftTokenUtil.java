package com.sgcc.nrxt.i6000.tool.util;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * This guy is lazy, nothing left.
 *
 * @author John Zhang
 */
@Component
public class OpenShiftTokenUtil {

    private static Logger log = LoggerFactory.getLogger(OpenShiftTokenUtil.class);

    public static final String ACCESS_TOKEN_STRING = "access_token=";

    private static final TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        @Override
        public final boolean isTrusted(final X509Certificate[] certificate, final String authType) {
            return true;
        }
    };

    private static SSLContext sslContext;

    private static String ocUrl;

    static  {
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    @Value("${openshift.url}")
    public void setOcUrl(String ocUrl) {
        OpenShiftTokenUtil.ocUrl = ocUrl;
    }


    public static String getOpenShiftToken(String userName, String password) {
        final CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).setSSLContext(sslContext).build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(client);

        HttpHeaders headers = new HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.AUTHORIZATION, OpenShiftTokenUtil.authorizationHeader(userName, password));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String url = OpenShiftTokenUtil.ocUrl + "/oauth/authorize?client_id=openshift-challenging-client&response_type=token";
        URI response = new RestTemplate(requestFactory).postForLocation(url, entity);

        String frag = response.getFragment();
        int s = frag.indexOf(OpenShiftTokenUtil.ACCESS_TOKEN_STRING);
        int e = frag.indexOf("&", s + OpenShiftTokenUtil.ACCESS_TOKEN_STRING.length());

        String token = frag.substring(s + OpenShiftTokenUtil.ACCESS_TOKEN_STRING.length(), e);

        return token;
    }

    private static String authorizationHeader(final String username, final String password) {
        final String auth = username + ":" + password;
        final byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        final String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }
}
