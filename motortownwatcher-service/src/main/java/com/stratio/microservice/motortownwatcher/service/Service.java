package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.service.model.ServiceInput;
import com.stratio.microservice.motortownwatcher.service.model.ServiceOutput;

import java.util.List;

public interface Service {

  ServiceOutput doSomething(ServiceInput input);

  List<String> listFilesInSftp(String remotepath);

  int writeFilelistTable();

  String startSpartaBatch();



}
