package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import com.stratio.microservice.motortownwatcher.repository.CsvrowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
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
    @Value("${spartawfpath}")
    private String spartawfpath;
    @Value("${spartawfname}")
    private String spartawfname;
    @Value("${spartawfversion}")
    private int spartawfversion;
    @Value("${spartaretries}")
    private int spartaretries;
    @Value("${motortownsync}")
    private String motortownsync;
    @Value("${ecommerce}")
    private String ECOMMERCE;


    private static final SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private CsvfileRepository csvrepo;

    @Autowired
    private CsvrowRepository csvrowrepo;

    @Scheduled(fixedRateString = "${schedulerRate}")
    public void ingestFromSftp() {


        log.info("MOTORTOWN WATCHER v0.9: Scheduled job start at: " + dateFormat.format(new Date()));

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
                log.info(ECOMMERCE + ":" + file.filename +  "don't exist in DB so will be added. ");

                /*
                log.info(ECOMMERCE + ": unzipping " + file.filename +  " on folder " + sftpoutfolder);
                List<CsvRow> rows= new ArrayList<>();
                rows=reader.unzipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder + file.filename,sftpoutfolder);
                found = true;

                log.info(ECOMMERCE +" POSTGRES:  start writing to PG this number of entities" + rows.size());
                csvrowrepo.deleteAllInBatch();
                csvrowrepo.flush();
                csvrowrepo.save(rows);
                log.info(ECOMMERCE + " POSTGRES:  " + rows.size() +  " csv rows written in PG table. ");

                int currentTry=1;
                String result = "";
                while (currentTry <= spartaretries && !result.equalsIgnoreCase("Finished")) {

                    log.info(ECOMMERCE + " SPARTA: running " + spartawfname  + " v" + spartawfversion + " execution number " + currentTry);
                    result=runWorkflow(spartawfpath,spartawfname,spartawfversion);
                    log.info(ECOMMERCE + " SPARTA: " + spartawfname + " v" + spartawfversion + " execution number " + currentTry +  " finished with state " + result);
                    currentTry++;
                }

                log.info(ECOMMERCE + " SPARTA: finished with state " + result);

                log.info(ECOMMERCE + " SYNC: calling motortown microservice at " + motortownsync);
                log.info(ECOMMERCE + " SYNC: motortownync " + StratioHttpClient.httpGET(motortownsync));

                */




                String body = String.format("{\"sftpFile\":\"%s\"}", sftpinfolder + file.filename);
                log.info(ECOMMERCE + " SYNC: calling motortown microservice at " + motortownsync + " with body " + body);
                //System.out.println("BODY: " + sftpinfolder + file.filename);
                log.info(ECOMMERCE + " SYNC: response: " + StratioHttpClient.httpPOST(motortownsync,body));

                file.status="Processed";
                csvrepo.save(file);



                break;

            }
        }

        if (!found) log.info(ECOMMERCE + ": no new files were detected on SFTP. ");


        log.info(ECOMMERCE + ": scheduled job end at: " + dateFormat.format(new Date()));

    }


    private String getSpartaVersion() {

        String sTicket=StratioHttpClient.getDCOSTicket();

        String resul=StratioHttpClient.callSpartaAPI(sTicket);
        System.out.println(resul);

        return "";
    }

    private String runWorkflow(String wf_path, String wf_name,int wf_version) {


            String sTicket=StratioHttpClient.getDCOSTicket();

            log.info(ECOMMERCE+ ": DCOS ticket: " + sTicket);

            String resul=StratioHttpClient.runSpartaWF(sTicket,wf_path,wf_name,wf_version);

            return resul;


    }

}
