package com.anujaneja.controllers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class HomeController {

    private static final Logger LOG = Logger.getLogger(HomeController.class.getName());

    private static final String CA_KEYSTORE_TYPE = KeyStore.getDefaultType(); //"JKS";
    private static final String CA_KEYSTORE_PATH = "/Users/admin/codebase/POC/ssl-client/src/main/java/keystore.jks";
    private static final String CA_KEYSTORE_PASS = "changeit";

    private static final String CLIENT_KEYSTORE_TYPE = "PKCS12";
    private static final String CLIENT_KEYSTORE_PATH = "/Users/admin/codebase/POC/ssl-client/src/main/java/client.p12";
    private static final String CLIENT_KEYSTORE_PASS = "changeit";

    @RequestMapping(path="/home/*",method= RequestMethod.GET)
    public String viewHome(Model model) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, CertificateException {
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(
                createSslCustomContext(),
                new String[]{"TLSv1","TLSv1.2"}, // Allow TLSv1 protocol only
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(csf).build()) {
//            HttpPost req = new HttpPost("https://anuj.unicommerce.com:8443/home");
//            req.setConfig(configureRequest());
//            HttpEntity ent = new InputStreamEntity(new FileInputStream("/Users/admin/codebase/POC/ssl-client/src/main/java/bytes.bin"));
//            req.setEntity(ent);
//            try (CloseableHttpResponse response = httpclient.execute(req)) {
//                HttpEntity entity = response.getEntity();
//                LOG.log(Level.INFO, "*** Reponse status: {0}", response.getStatusLine());
//                EntityUtils.consume(entity);
//                LOG.log(Level.INFO, "*** Response entity: {0}", entity.toString());
//            }


            HttpGet req = new HttpGet("https://anuj.unicommerce.com:8443/home");
//            req.setConfig(configureRequest());
            try (CloseableHttpResponse response = httpclient.execute(req)) {
                HttpEntity entity = response.getEntity();
                String result  = EntityUtils.toString(entity);
                System.out.println(result);
            }


        }

        return "HELLO";
    }

    public static RequestConfig configureRequest() {
        HttpHost proxy = new HttpHost("anuj.unicommerce.com", 8443, "https");
        RequestConfig config = RequestConfig.custom()
                .setProxy(proxy)
                .build();
        return config;
    }



    public static SSLContext createSslCustomContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
        // Trusted CA keystore
        KeyStore tks = KeyStore.getInstance(CA_KEYSTORE_TYPE);
        tks.load(new FileInputStream(CA_KEYSTORE_PATH), CA_KEYSTORE_PASS.toCharArray());

        // Client keystore
        KeyStore cks = KeyStore.getInstance(CLIENT_KEYSTORE_TYPE);
        cks.load(new FileInputStream(CLIENT_KEYSTORE_PATH), CLIENT_KEYSTORE_PASS.toCharArray());

        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(tks, new TrustSelfSignedStrategy()) // use it to customize
                .loadKeyMaterial(cks, CLIENT_KEYSTORE_PASS.toCharArray()) // load client certificate
                .build();
        return sslcontext;
    }


}
