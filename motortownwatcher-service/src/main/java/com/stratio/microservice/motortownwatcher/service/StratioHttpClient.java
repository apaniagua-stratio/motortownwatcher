package com.stratio.microservice.motortownwatcher.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class StratioHttpClient {

    final static String sRequestAuth= "https://cc.anjana.local/sso/login?service=https%3A%2F%2Fcc.anjana.local%2Fsso%2Foauth2.0%2FcallbackAuthorize";


    public static HttpClient getHttpClient() {

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {

                            return null;
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {

                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {

                        }
                    }}, new SecureRandom());

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpClient httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(socketFactory)
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.STANDARD).build()).build();

            return httpClient;

        } catch (Exception e) {
            e.printStackTrace();
            return HttpClientBuilder.create().build();
        }
    }

    public static String callSpartaAPI(String sTicket) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders= loginresponse.getHeaders("Set-Cookie");
            String sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
            sUser = StringUtils.substringAfter(sUser, "=");

            builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/appInfo")
                    .setParameter("user", sUser);
            uri = builder.build();
            httpget = new HttpGet(uri);
            HttpResponse apiresponse = loginClient.execute(httpget);

            return EntityUtils.toString(apiresponse.getEntity());

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return "";


    }

    public static String getDCOSTicket() {

        final String spartaUser="fjurado";
        final String spartaPassEncrypted="%3C%28%7Dx7wE%28U%27DZ%3Etv%3D";

        String sCookie="";
        String sLT="";
        String sExecution;

        String sTicket="";
        String sUser ="";

        try {

            HttpClient client = StratioHttpClient.getHttpClient();
            HttpGet request = new HttpGet(sRequestAuth);
            HttpResponse response = null;
            response = client.execute(request);

            //---------------------------- get jsession from header
            Header[] jider=response.getHeaders("Set-Cookie");
            sCookie = StringUtils.substringBefore(jider[0].getValue(), ";");

            //-----------------------------get lt and execution from content
            InputStream inputStream=response.getEntity().getContent();

            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String htmlString = writer.toString();

            sLT = StringUtils.substringBetween(htmlString,"input type=\"hidden\" name=\"lt\" value=\"","\" />");
            sExecution = StringUtils.substringBetween(htmlString,"<input type=\"hidden\" name=\"execution\" value=\"","\" />");

            // --------------------- POST -----------------------------

            HttpPost post = new HttpPost(sRequestAuth);


            post.addHeader("Cookie", sCookie);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("lt", sLT));
            urlParameters.add(new BasicNameValuePair("execution", sExecution));
            urlParameters.add(new BasicNameValuePair("_eventId", "submit"));
            urlParameters.add(new BasicNameValuePair("tenant", "NONE"));
            urlParameters.add(new BasicNameValuePair("username", spartaUser));
            urlParameters.add(new BasicNameValuePair("password", "<(}x7wE(U'DZ>tv="));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));


            HttpResponse postresponse = client.execute(post);


            Header[] postjiders= postresponse.getHeaders("Location");
            //get jsession from header

            sTicket = StringUtils.substringAfter(postjiders[0].getValue(), "ticket=");
            return sTicket;

        }
        catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String runSpartaWF(String sTicket, String path, String name, String version) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders= loginresponse.getHeaders("Set-Cookie");
            String sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
            sUser = StringUtils.substringAfter(sUser, "=");

            HttpPost post = new HttpPost("https://sparta.anjana.local/sparta-server/workflows/find");

            post.addHeader("User", sUser);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("name", name));
            urlParameters.add(new BasicNameValuePair("version", version));
            urlParameters.add(new BasicNameValuePair("group", path));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));


            HttpResponse apiresponse = loginClient.execute(post);

            return EntityUtils.toString(apiresponse.getEntity());

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return "";


    }

}
