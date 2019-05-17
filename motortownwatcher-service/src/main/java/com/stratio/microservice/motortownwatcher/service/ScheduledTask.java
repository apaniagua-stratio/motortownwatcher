package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import com.stratio.microservice.motortownwatcher.repository.CsvrowRepository;
import com.stratio.microservice.motortownwatcher.repository.CsvrowRepository_old;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class ScheduledTask {

    @Value("${sftphost}")
    private String sftphost;

    @Value("${sftpuser}")
    private String sftpuser;

    @Value("${sftpkey}")
    private String sftpkey;

    @Value("${sftpinfolder}")
    private String sftpinfolder;

    @Value("${sftpoutfolder}")
    private String sftpoutfolder;

    private static final SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private CsvfileRepository csvrepo;

    @Autowired
    private CsvrowRepository csvrowrepo;

    //@Scheduled(fixedRateString = "${schedulerRate}")
    @Scheduled(fixedRateString = "${schedulerRate}")
    public void ingestFromSftp() {

        log.info("AURGI Scheduled job at: " + dateFormat.format(new Date()));

        SftpReader reader = new SftpReader();

        List<Csvfile> listaZip=reader.listZipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder);

        boolean found=false;
        Iterator<Csvfile> csvIterator = listaZip.iterator();
        while (csvIterator.hasNext()) {

            Csvfile file=csvIterator.next();

            //if file dont exist in repo, add it (and launch workflow)
            if (csvrepo.findByFileid(file.filename + "&" + file.filedate).size() <= 0) {

                //save to files readed table
                csvrepo.save(file);
                log.info("AURGI FILE: " + file.filename +  "don't exist in DB so will be added. ");

                //unzip the csvs in zip
                List<CsvRow> rows= new ArrayList<>();
                rows=reader.unzipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder + file.filename,sftpoutfolder);
                found = true;

                //save the rows of all csvs to table

                log.info("AURGI:  start writing to PG this number of entities" + rows.size());
                //csvrowrepo.saveAll(rows);

                csvrowrepo.deleteAllInBatch();
                csvrowrepo.flush();
                csvrowrepo.save(rows);

                log.info("AURGI:  " + rows.size() +  " csv rows written in PG table. ");
                break;

            }
        }

        if (!found) log.info("AURGI no new files were detected on SFTP. ");


        log.info("AURGI scheduled job end.");

    }

    private String getSpartaVersion() {

        String sTicket=StratioHttpClient.getDCOSTicket();

        String resul=StratioHttpClient.callSpartaAPI(sTicket);
        System.out.println(resul);

        return "";
    }

    private String runWorkflow(String wf_path, String wf_name,String wf_version) {

        String sTicket=StratioHttpClient.getDCOSTicket();

        String resul=StratioHttpClient.getSpartaWFId(sTicket,wf_path,wf_name,wf_version);
        System.out.println(resul);
        resul=StratioHttpClient.runSpartaWF(sTicket,resul);
        System.out.println(resul);

        return "";
    }

    private String startSpartaWF() {


            final String sRequestAuth= "https://cc.anjana.local/sso/login?service=https%3A%2F%2Fcc.anjana.local%2Fsso%2Foauth2.0%2FcallbackAuthorize";

            final String spartaUser="fjurado";
            final String spartaPassEncrypted="%3C%28%7Dx7wE%28U%27DZ%3Etv%3D";
            String sCookie="";
            String sLT="";
            String sExecution;

            String sTicket="";
            String sUser ="";


            //HttpClient client = new DefaultHttpClient();
            HttpClient client = StratioHttpClient.getHttpClient();
            HttpGet request = new HttpGet(sRequestAuth);
            HttpResponse response = null;
            try {
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


                // -------------------------LOGIN  ----------------------------

                /*

                URIBuilder builder = new URIBuilder();
                builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/login")
                            .setParameter("code", sTicket);


                URI uri = builder.build();

                HttpGet httpget = new HttpGet(uri);



                //HttpGet requestLogin = new HttpGet(sRequestLogin);

                //List<NameValuePair> urlLoginParameters = new ArrayList<NameValuePair>();
                //urlParameters.add(new BasicNameValuePair("code", sTicket));



                HttpClient loginClient = StratioHttpClient.getHttpClient();
                HttpResponse loginresponse = loginClient.execute(httpget);


                Header[] loginjiders= loginresponse.getHeaders("Set-Cookie");
                sUser = StringUtils.substringBefore(loginjiders[0].getValue(), ";");
                sUser = StringUtils.substringAfter(sUser, "=");

                 */

                String resul=StratioHttpClient.callSpartaAPI(sTicket);
                System.out.println(resul);

                /*
                builder = new URIBuilder();
                builder.setScheme("https").setHost("sparta.anjana.local/sparta-server").setPath("/appInfo")
                        .setParameter("user", sUser);

                uri = builder.build();

                httpget = new HttpGet(uri);

                HttpResponse apiresponse = loginClient.execute(httpget);
                System.out.println(EntityUtils.toString(apiresponse.getEntity()));
                */


                return "";

                //https://cc.anjana.local/sso/login?service=https%3A%2F%2Fcc.anjana.local%2Fsso%2Foauth2.0%2FcallbackAuthorize
                // -H 'Cookie: JSESSIONID=1su8iedejq0tm1sgzgy2gbk3sr'
                // --data 'lt=LT-135026-QSjr53TH3uayckGV6FpMGq7P7wxVwf-gosec-sso-ha
                // &execution=07b0e44d-1a92-4826-a658-bf144c143567_AAAAIgAAABB7QfLsttvFFJWtBGZtD9WtAAAABmFlczEyOAtke7g7w2eEHhFtfb8AfY2Gb5xAl1zhd9k3Srj6r7%2FfNRTCIxuJ9m%2FH3Yrp0PhJLGdk126ZtvfC%2Bk%2B1QJSAP3TK3ooC017uRNCJPL2wyarIe70LW%2BB1kpWXzbOaAh0lT3gjbZY9se5TSOxeTwxRx%2Fpv6r1j9FPN63VZ5j%2BHSxwXJKkuofimWfCJErU85lIBwzOVhhA1%2B9MmksV943NlDrxBq7PAPHoGcqbmenBjpBFbDvt8BDZtrmu0q5NtE0s43nLioScKLS%2F1fU19z%2BV17hbCaMvJM9gizhESK0F87QhJ%2F%2Bac0qP72v9rJ1OGlAOlrlkV8D3%2FgJdB4L2l4r87w%2BOJk%2BC882WIaHAEbXU60CvVpWnTqHsZZrOVH%2BEVKrXSxvdD8%2FSYudbDhxXlPvnavg%2FPGhkogmbVc7N9Xh5yW1dQkVSb8d1Vas3eAN2osdfw7cVNEZHwDfctuypvnJwPokptk%2BoWazyOmh8W7qyxcfxDw2FepFaECujQvLByISrmxl3V3ymZ3aErEBCVMW3oF4Px3lEsLa4oQC5wUJppGIW4m4whgx4446PpmGzFgjxhkNWVqcKwNdwJiJjGR4KXqE%2B2eJEd31YIIft3rafg4Cpuw90RGhIK5z33BVud1WQeLp0TXC2WaDj3Cp10tnqGRKdais0k5Rl4%2Bem%2F5nSHMnMJDLgPfMoaNbF1t%2F3QNe%2BcboP6n9R4lvEehvdL0o27HRe90KTkuKiN0MiFvWJpLzkB10X7bijLtbao0vRNianV3pdtOumUhsxQuHyv%2F%2Bc%2BnVyHgEgZUXM2ryiW2Ed6ZUK3Yl7L7CyujYfd3fXGddB%2BdnejSlC2X1Zi0ltLDAfJ2K0M120aoB62Zz7f78uZOvZ4%2B4UExone6uDTU0leTyKrcU0GY2feGKBZuwTnBC6W8eYeyVflYriMhK2XTIYyYzEFNSsPPR2ezTxCVX6r2O%2BByk7ARU51VqxIf3ejwaFiDKqPKo8T7d1Nj8BpMykbdv1qkV0P9Pz1OdLaJqlnEKvt75DNWeyQLhoSHjw4JIXymT33jSJqoMqltPfI%2FOl7TuwqYMSnZqQnOSryuK%2F%2FSD997SEwy%2BBedVzXRAOZ7XzsUvvpMDm9UodM4BI38a0N6b%2F3SJr71r2dauPDViNuCBltm1bxue71InAwoc7HclucvL8DroanNTlqNLFsN4%2F9LYJe%2FPW5JmZq8T6Lpe%2BRHae9nHurCBwvjrhrjRO%2Be3%2FN6jCYlQSkzY2QamUAbOp9p8BI%2BGl6AdqZ4E0vG%2BY5UlnAjjj6CPJmPL%2BDb7bOPmvdp%2B09LZuYUDY6Sduva6NlU%2BEvdrzhRmxYGE7%2FbmvrNretPQuMFIzj3r2WfPP3eBSFHsYCj65In8Zf4jtDHvIsCHN9baKRCIek5zbB5LJ3rtO4ZAnAEprpZzclPIoMFne%2Fd7vaSgo%2FjPU0y9xcy4RTC05UWYpiXOa1plyNCaDqlwXdhR2983r%2FI5PsEmIdU2%2BGEqq%2BiELaF1bekgFUgCJH99qkswWLmJk6Jy7%2BjYdD%2BhGI1DwCOv2xF0gjVsQ9C9dHz6koMaoUHHoLOJ17CAkUoAefuRnd69pQpQeM8t%2FRQKUYLB0Z%2B7%2FQwTenLfjD7WfQZlMTfaQ4xNh2FWsFqHqz%2F%2BVVdKVUrcb%2FBRb1Sx3WPbpM7%2BDx87wMx4O1tYKJdMVxZI9GGJSaDtbzehh563kPijThaE2mRxcZVSaJ447%2B%2F90uCFt4D4gvmkktGwrFAKOC6AtzEV3M2Z4goxdC%2FXy2KJ8ojUeE1yNC33Y0zR8grcaOTPGzgubpW3AHBAIlYBs7Vw%2F2TTHyc2fHJeYhxy8s1GO%2F4VSO1zRwNFNj9o69fErV13Pw4KbWKS4eaKzByku3FfL7aZViEROwmh3XbQMiKebHnutZVAXpV7wmmxsTmxttvpBmtacKJCU6PVhLq%2BiI6PXRNP6WizJIkhbUjhcCkLFWSQ042hw%2BgArc0gf1%2BSOncPYKK7qk%2FDZRDExa%2BKOJseisMOP4byS4s1sc%2FwB5AcEP1US%2Fuf8YlePVO%2BrAAstevU0x8nF%2FuZuVh%2FfwdlIxfda4v0dDxaZqWjfouMipgvFCgKGSXh3dbrco%2Bl2uMCZes9jJz%2FkNXcWh8ygP7tC5TnnE%2FFEcEVSBK5Gt7MKHXzPnPCTKQuT%2BWzLHPP9Zeg8moABkvg7AJxWgd4E4OClSuNd2mAeSYnCaTO9p6scUGaGGAf1mwstdE9G6eH0b%2F95xo89v5ftko%2B2sfkSycc2CEOb%2FFidy9qR7ZRapzgJxGQwYqrlOsf6igydfJffqOmG%2Bg0pCg5DmkPFDJU3TyC7vDW0vfLFxJt4BCsr1fVL2eeXDMwX6JrxbJ5IQftgF3go9eebyWO8Cg%2FqOBRN87aXIgdkN2IhQkHjI2dRrsXZtYVvzGYtnM9qdlRx6kgzgjOp8%2FufnLoIxtBGWwa2tWBbq%2FFl2ZwsXQFGePz0rR4gv7EhG6bnQQ1NtGFGelg6zRUjUUloFxPLu112SYfKWqicLd6vMr%2FPBKukNHOp4egItYamFKztqw%2B4NrgqsWH8ZNSpiVw%3D%3D&_eventId=submit&tenant=NONE&username=fjurado&password=???'


            } catch (IOException e) {
                e.printStackTrace();
            }

        return sCookie;
    }
}
