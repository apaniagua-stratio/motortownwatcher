package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.entity.Csvproduct;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CsvproductRepository extends CrudRepository<Csvproduct, String> {



}
