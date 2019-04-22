package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CsvfileRepository extends CrudRepository<Csvfile, String> {

    List<Csvfile> findByfilename(String filename);

    List<Csvfile> findByFilenameAndFiledate(String filename,String filedate);

    List<Csvfile> findByFileid(String fileid);

}
