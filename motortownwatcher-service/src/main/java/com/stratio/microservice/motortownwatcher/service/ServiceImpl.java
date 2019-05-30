package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceImpl implements com.stratio.microservice.motortownwatcher.service.Service{

  @Value("${sftphost}")
  private String sftphost;

  @Value("${sftpuser}")
  private String sftpuser;

  @Value("${sftpkey}")
  private String sftpkey;

  @Autowired
  private CsvfileRepository csvrepo;


  @Override
  public List<String> listFilesInSftp(String remotePath) {

    SftpReader reader = new SftpReader();
    reader.listZipFileFromSftp(sftpuser,sftphost,sftpkey,"/anjana");
    return null;


  }

  @Override
  public int writeFilelistTable() {

    csvrepo.save(new Csvfile("hey","20190404"));
    return csvrepo.findByfilename("hey").size();

    //return csvrepo.findList().size();

  }


}
