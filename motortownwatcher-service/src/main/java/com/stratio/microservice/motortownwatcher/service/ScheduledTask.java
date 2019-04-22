package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

    @Scheduled(fixedRate = 180000)
    public void reportCurrentTime() {

        System.out.println("The time is: " + dateFormat.format(new Date()));

        SftpReader reader = new SftpReader();

        List<Csvfile> listaZip=reader.listZipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder);

        Iterator<Csvfile> csvIterator = listaZip.iterator();
        while (csvIterator.hasNext()) {

            Csvfile file=csvIterator.next();

            //if file dont exist in repo, add it (and launch workflow)
            if (csvrepo.findByFileid(file.filename + "&" + file.filedate).size() <= 0) {

                csvrepo.save(file);
                log.info("FILE: " + file.filename +  "not exists will be added. ");
                reader.unzipFileFromSftp(sftpuser,sftphost,sftpkey,sftpinfolder + file.filename,sftpoutfolder);

            }

        }

    }
}
