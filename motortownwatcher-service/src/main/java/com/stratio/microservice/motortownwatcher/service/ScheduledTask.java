package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.entity.Product;
import com.stratio.microservice.motortownwatcher.entity.Productko;
import com.stratio.microservice.motortownwatcher.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    @Value("${adminmail}")
    private String adminmail;


    final static String FINISHED_STATE = "Finished";
    final static String FAILED_STATE = "Failed";
    private static final SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private CsvfileRepository csvrepo;

    @Autowired
    private StockRepository stockrepo;

    @Autowired
    private ProductRepository prodrepo;

    @Autowired
    private ProductkoRepository prodkorepo;

    @Autowired
    private CsvrowRepository csvrowrepo;

    @Autowired
    private MailClient mailClient;

    @Scheduled(fixedRateString = "${schedulerRate}")
    //TMP PRUEBAS @Scheduled(cron = "${schedulerCron}")
    public void ingestFromSftp() {

        log.info("MOTORTOWN WATCHER DEV: Scheduled job start at: " + dateFormat.format(new Date()));


        SftpReader reader = new SftpReader();
        List<Csvfile> listaZip=reader.listZipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder);


        boolean found=false;
        Iterator<Csvfile> csvIterator = listaZip.iterator();

        while (csvIterator.hasNext()) {

            Csvfile file=csvIterator.next();

            //if file dont exist in repo, add it (and launch workflow)
            if (csvrepo.findByFileid(file.filename + "&" + file.filedate).size() <= 0 || csvrepo.findByFileidAndStatus(file.filename + "&" + file.filedate,FAILED_STATE).size()>0) {

                //save to files readed table
                csvrepo.save(file);
                log.info(ECOMMERCE + ":" + file.filename +  " don't exist in DB so will be added. ");

                String body = String.format("{\"sftpFile\":\"%s\"}", sftpinfolder + file.filename);
                log.info(ECOMMERCE + " SYNC: calling motortown microservice at " + motortownsync + " with body " + body);
                //System.out.println("BODY: " + sftpinfolder + file.filename);
                String response = StratioHttpClient.httpPOST(motortownsync,body);
                log.info(ECOMMERCE + " SYNC: response: " + response);

                //corregir esto lo q hay q identificar es el finisehd, por si llega un 500 tmb poner failed
                if (response.contains(FINISHED_STATE)) {
                    file.status=FINISHED_STATE;
                }
                else {
                    file.status=FAILED_STATE;
                }

                csvrepo.save(file);

                break;

            }
        }

        if (!found) log.info(ECOMMERCE + ": no new files were detected on SFTP. ");

        String content= getMailContent();
        mailClient.prepareAndSend(adminmail,content);

        System.out.println(content);

        log.info(ECOMMERCE + ": scheduled job end at: " + dateFormat.format(new Date()));

    }

    private String getMailContent()
    {
        //TODO send mail with rows ingested

        int csvproductCount=0;
        int csvplataformaCount=0;
        int csvtoprecambiosCount=0;
        int csvstockCount=0;

        List<CsvRow> rows = csvrowrepo.findAll();

        for (CsvRow r: rows) {

            if (r.entity.equalsIgnoreCase("motortown_productos_y_servicios.csv")) csvproductCount++;
            if (r.entity.equalsIgnoreCase("motortown_stock_en_plataforma.csv")) csvplataformaCount++;
            if (r.entity.equalsIgnoreCase("motortown_stock_por_centro.csv")) csvstockCount++;
            if (r.entity.equalsIgnoreCase("pricat_ctop5.csv")) csvtoprecambiosCount++;

        }

        //long stockCount=stockrepo.count();
        //int plataformaCount= stockrepo.findByIdnavision("20").size();
        //int toprecambiosCount= stockrepo.findByIdnavision("100001").size();
        long productCount= prodrepo.count();
        //stockCount= stockCount - plataformaCount - toprecambiosCount;

        String mailContent="";
        mailContent += " Productos en csv original:" + csvproductCount + System.lineSeparator();
        //mailContent += " Stock en csv original:" + csvstockCount;
        //mailContent += " Plataforma en csv original:" + csvplataformaCount;
        //mailContent += " Toprecambios en csv original:" + csvtoprecambiosCount;

        mailContent += " Productos OK que se importarán:" + productCount + System.lineSeparator();
        //mailContent += " Stock plataforma OK que se importará:" + plataformaCount;
        //mailContent += " Stock toprecambios OK que se importará:" + toprecambiosCount;
        //mailContent += " Stock centros OK que se importará:" + stockCount;

        mailContent += " Productos KO descartados: " + System.lineSeparator();

        List<Productko> lstprod =  prodkorepo.findLastProductsko();


        for (Object pko : lstprod) {
            mailContent += ToStringBuilder.reflectionToString(pko, ToStringStyle.SIMPLE_STYLE) + System.lineSeparator();

        }

        System.out.println(mailContent);


        /*TODO productos añadidos hoy */


        return mailContent;

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
