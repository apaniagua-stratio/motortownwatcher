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
    @Value("${motortownsync}")
    private String motortownsync;
    @Value("${ecommerce}")
    private String ECOMMERCE;
    @Value("${sendermail}")
    private String sendermail;
    @Value("${receivermail}")
    private String receivermail;
    @Value("${subjectmail}")
    private String subjectmail;


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

    //@Scheduled(fixedRateString = "${schedulerRate}")
    @Scheduled(cron = "${schedulerCron}")
    public void ingestFromSftp() {

        log.info("MOTORTOWN WATCHER DEV: Scheduled job start at: " + dateFormat.format(new Date()));

        SftpReader reader = new SftpReader();
        List<Csvfile> listaZip=reader.listZipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder);

        List lst = csvrepo.findByfilename("test");

        if (lst.size() == 0) {
            log.info(" POSTGRES DEV: NOT FOUND");
        }
        else {
            log.info(" POSTGRES DEV: FOUND!!");
        }


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

                //envio de mail resultados
                String content= getMailContent();
                mailClient.prepareAndSend(sendermail,receivermail,subjectmail,content);

                break;

            }
        }

        if (!found) log.info(ECOMMERCE + ": no new files were detected on SFTP. ");



        log.info(ECOMMERCE + ": just testing dev version ");
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
        /*
        das d hrap might put simitsu sit der also satpad, and genpad wen arrive, point is got sit stil pendin
         */
        long productCount= prodrepo.findLastProductsOk().size();
        String originalfile=prodrepo.findLastOriginalFile();
        //stockCount= stockCount - plataformaCount - toprecambiosCount;

        String mailContent="";
        mailContent += System.lineSeparator() + "Fichero que se va a ingestar: " + originalfile + System.lineSeparator() + System.lineSeparator();
        mailContent += System.lineSeparator() + "Productos en csv: " + csvproductCount + System.lineSeparator() + System.lineSeparator();
        mailContent += "Stock centros en csv: " + csvstockCount + System.lineSeparator() + System.lineSeparator();
        mailContent += "Stock Plataforma en csv: " + csvplataformaCount + System.lineSeparator() + System.lineSeparator();
        mailContent += "Stock Toprecambios en csv: " + csvtoprecambiosCount + System.lineSeparator() + System.lineSeparator();

        mailContent += "Productos OK que se importarán: " + productCount + System.lineSeparator() + System.lineSeparator();
        //mailContent += "Stock plataforma OK que se importará:" + plataformaCount;
        //mailContent += "Stock toprecambios OK que se importará:" + toprecambiosCount;
        //mailContent += "Stock centros OK que se importará:" + stockCount;

        mailContent += "Listado de productos KO que no se importarán: " + System.lineSeparator() + System.lineSeparator();

        List<Productko> lstprod =  prodkorepo.findLastProductsko();


        for (Object pko : lstprod) {
            mailContent += "  " +  ToStringBuilder.reflectionToString(pko, ToStringStyle.SIMPLE_STYLE) + System.lineSeparator();

        }

        /*TODO productos añadidos hoy */

        return mailContent;

    }

}