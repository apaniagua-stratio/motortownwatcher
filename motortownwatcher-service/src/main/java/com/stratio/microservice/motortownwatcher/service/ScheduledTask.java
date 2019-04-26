package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import com.stratio.microservice.motortownwatcher.repository.CsvrowRepository;
import com.stratio.microservice.motortownwatcher.repository.CsvrowRepository_old;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

                //csvrowrepo.deleteAll();
                csvrowrepo.flush();
                csvrowrepo.save(rows);

                log.info("AURGI:  " + rows.size() +  " csv rows written in PG table. ");

            }
        }

        if (!found) log.info("AURGI no new files were detected on SFTP. ");


    }
}
