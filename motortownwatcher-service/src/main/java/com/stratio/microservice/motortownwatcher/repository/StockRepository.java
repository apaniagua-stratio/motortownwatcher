package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.Product;
import com.stratio.microservice.motortownwatcher.entity.Stock;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StockRepository extends CrudRepository<Stock, String> {

    List<Stock> findByIdnavision(String idnavision);
    

}
