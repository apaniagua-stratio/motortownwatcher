package com.stratio.microservice.motortownwatcher.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class StratioHttpClient {

    final static String sRequestAuth = "https://cc.anjana.local/sso/login?service=https%3A%2F%2Fcc.anjana.local%2Fsso%2Foauth2.0%2FcallbackAuthorize";
    final static String FINISHED_STATE = "Finished";
    final static String FAILED_STATE = "Failed";

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

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

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

    public static String getSpartaInfo(String sTicket) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders = loginresponse.getHeaders("Set-Cookie");
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

    public static String callSpartaAPI(String sTicket) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders = loginresponse.getHeaders("Set-Cookie");
            String sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
            sUser = StringUtils.substringAfter(sUser, "=");

            builder = new URIBuilder();

            builder.setScheme("https").setHost("sparta.anjana.local").setPath("/sparta-server/groups/findByName//home/test");
            //.addParameter("name", "/home/test");

            uri = builder.build();
            httpget = new HttpGet("https://sparta.anjana.local/sparta-server/groups/findByName/%2Fhome%2Ftest");

            //httpget.setHeader("User", sUser);
            httpget.setHeader("Cookie", "user=" + sUser);
            httpget.setHeader("Accept", "application/json");
            httpget.setHeader("Content-type", "application/json");

            HttpResponse apiresponse = loginClient.execute(httpget);

            return EntityUtils.toString(apiresponse.getEntity());

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return "";


    }

    public static String getDCOSTicket() {

        final String spartaUser = "fjurado";
        final String spartaPassEncrypted = "%3C%28%7Dx7wE%28U%27DZ%3Etv%3D";

        String sCookie = "";
        String sLT = "";
        String sExecution;

        String sTicket = "";
        String sUser = "";

        try {

            HttpClient client = StratioHttpClient.getHttpClient();
            HttpGet request = new HttpGet(sRequestAuth);
            HttpResponse response = null;
            response = client.execute(request);

            //---------------------------- get jsession from header
            Header[] jider = response.getHeaders("Set-Cookie");
            sCookie = StringUtils.substringBefore(jider[0].getValue(), ";");

            //-----------------------------get lt and execution from content
            InputStream inputStream = response.getEntity().getContent();

            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String htmlString = writer.toString();

            sLT = StringUtils.substringBetween(htmlString, "input type=\"hidden\" name=\"lt\" value=\"", "\" />");
            sExecution = StringUtils.substringBetween(htmlString, "<input type=\"hidden\" name=\"execution\" value=\"", "\" />");

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


            Header[] postjiders = postresponse.getHeaders("Location");
            //get jsession from header

            sTicket = StringUtils.substringAfter(postjiders[0].getValue(), "ticket=");
            return sTicket;

        } catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String runSpartaWF(String sTicket, String path, String name, int version) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders = loginresponse.getHeaders("Set-Cookie");
            String sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
            sUser = StringUtils.substringAfter(sUser, "=");

            // get wrokfow id
            HttpPost post = new HttpPost("https://sparta.anjana.local/sparta-server/workflows/find");

            post.setHeader("Cookie", "user=" + sUser);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            String body = String.format("{\"name\":\"%s\",\"version\":%s,\"group\":\"%s\"}", name, version, path);


            //post.setEntity(new UrlEncodedFormEntity(urlParameters));
            post.setEntity(new StringEntity(body));

            HttpResponse apiresponse = loginClient.execute(post);

            String apiResult = EntityUtils.toString(apiresponse.getEntity());

            String sId = StringUtils.substringAfter(apiResult, "\"id\":\"");
            sId = StringUtils.substringBefore(sId, "\",\"");

            // run workflow id

            HttpPost runpost = new HttpPost(String.format("https://sparta.anjana.local/sparta-server/workflows/run/%s", sId));

            runpost.setHeader("Cookie", "user=" + sUser);
            runpost.setHeader("Accept", "application/json");
            runpost.setHeader("Content-type", "application/json");

            HttpResponse runresponse = loginClient.execute(runpost);

            String runResult = EntityUtils.toString(runresponse.getEntity());
            String sExecutionId = StringUtils.substringAfter(runResult, "\"");
            sExecutionId = StringUtils.substringBefore(sExecutionId, "\"");

            //TODO: wait until completion, retry 3 times
            HttpGet execpost = new HttpGet(String.format("https://sparta.anjana.local/sparta-server/workflowExecutions/%s", sExecutionId));

            execpost.setHeader("Cookie", "user=" + sUser);
            execpost.setHeader("Accept", "application/json");
            execpost.setHeader("Content-type", "application/json");

            HttpResponse execresponse = loginClient.execute(execpost);
            String execResult = EntityUtils.toString(execresponse.getEntity());

            String lastState = parseWfState(execResult);

            //wait for completion
            while (!lastState.equalsIgnoreCase(FINISHED_STATE) && !lastState.equalsIgnoreCase(FAILED_STATE)) {
                Thread.sleep(10000);

                //HttpGet execpost2 = new HttpGet(String.format("https://sparta.anjana.local/sparta-server/workflowExecutions/%s",sExecutionId));
                //execpost2.setHeader("Cookie", "user=" + sUser );
                //execpost2.setHeader("Accept", "application/json");
                //execpost2.setHeader("Content-type", "application/json");

                HttpResponse execresponse2 = loginClient.execute(execpost);
                execResult = EntityUtils.toString(execresponse2.getEntity());
                lastState = parseWfState(execResult);

            }

            return lastState;


        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";

    }

    private static String parseWfState(String execResult) {

        //{"id":"9ac7e2a3-fd9c-4f3b-9d8c-cbec1acb5d12","statuses":[{"state":"Finished"

        String state = StringUtils.substringAfter(execResult, "\"state\":\"");
        state = StringUtils.substringBefore(state, "\"");

        return state;
    }

    public static String getSpartaWFId(String sTicket, String path, String name, String version) {

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                    .setParameter("code", sTicket);


            URI uri = builder.build();

            HttpGet httpget = new HttpGet(uri);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse loginresponse = loginClient.execute(httpget);


            Header[] loginjiders = loginresponse.getHeaders("Set-Cookie");
            String sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
            sUser = StringUtils.substringAfter(sUser, "=");


            // get workflow id
            HttpPost post = new HttpPost("https://sparta.anjana.local/sparta-server/workflows/find");

            post.setHeader("Cookie", "user=" + sUser);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            //List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            //urlParameters.add(new BasicNameValuePair("name", name));
            //urlParameters.add(new BasicNameValuePair("version", version));
            //urlParameters.add(new BasicNameValuePair("group", path));

            String body = String.format("{\"name\":\"%s\",\"version\":\"%s\",\"group\":\"%s\"}", name, version, path);


            //post.setEntity(new UrlEncodedFormEntity(urlParameters));
            post.setEntity(new StringEntity(body));

            HttpResponse apiresponse = loginClient.execute(post);

            String apiResult = EntityUtils.toString(apiresponse.getEntity());

            String sId = StringUtils.substringAfter(apiResult, "\"id\":\"");
            sId = StringUtils.substringBefore(sId, "\",\"");

            return sId;


        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return "";

    }


    public static String httpGET(String URL) {

        try {

            HttpGet httpget = new HttpGet(URL);
            HttpClient loginClient = StratioHttpClient.getHttpClient();
            HttpResponse response = loginClient.execute(httpget);
            return EntityUtils.toString(response.getEntity());
        }
        catch(IOException  e)
        {
            e.printStackTrace();
        }

        return"";

}


}
