package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.CsvRow;
import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CsvrowRepository_old extends JpaRepository<CsvRow, String> {



    List<CsvRow> findByContent(String content);

    //List<CsvRow> save(Iterable<? extends CsvRow> entities);
    //List<T> save(Iterable<? extends T> entities);



}
