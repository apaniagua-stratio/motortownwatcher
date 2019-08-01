package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.Csvfile;
import com.stratio.microservice.motortownwatcher.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, String> {

    List<Product> findAll();

    long count();

    @Query(value="Select EAN from Product where originalfile=(Select max(originalfile) from Product)")
    List<Product> findLastProductsOk();

}
