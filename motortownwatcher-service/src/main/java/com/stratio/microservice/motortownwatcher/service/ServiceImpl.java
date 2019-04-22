package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import com.stratio.microservice.motortownwatcher.service.model.ServiceInput;
import com.stratio.microservice.motortownwatcher.service.model.ServiceOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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
  public ServiceOutput doSomething(ServiceInput input) {

    return null;

  }

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

  @Override
  public String startSpartaBatch() {



    RestTemplate restTemplate = new RestTemplate();
    String fooResourceUrl
            = "http://localhost:9090/appInfo";

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

    ResponseEntity<String> respEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.GET, entity, String.class);

    String resp = respEntity.getBody();


    return resp;

  }

}
