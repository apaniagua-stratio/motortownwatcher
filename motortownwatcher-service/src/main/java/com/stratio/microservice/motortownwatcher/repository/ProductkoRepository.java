package com.stratio.microservice.motortownwatcher.repository;

import com.stratio.microservice.motortownwatcher.entity.Product;
import com.stratio.microservice.motortownwatcher.entity.Productko;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductkoRepository extends CrudRepository<Productko, String> {

    List<Productko> findAll();

    long count();

    @Query(value="Select CodInterno,Categoria,GrupoProducto,Estado,Atributo05,Atributo06, Atributo07, Atributo08, Atributo09, Atributo10, Atributo11, Atributo12, Atributo13, Atributo14, Atributo15 from Productko where processts = (select max(processts) from Productko) order by Categoria asc")
    List<Productko> findLastProductsko();

}
