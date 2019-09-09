package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "products_ko")

public class Productko {

    @Id
    public String CodInterno;
    public String Categoria;
    public String GrupoProducto;
    public String Estado;
    public String Atributo05;
    public String Atributo06;
    public String Atributo07;
    public String Atributo08;
    public String Atributo09;
    public String Atributo10;
    public String Atributo11;
    public String Atributo12;
    public String Atributo13;
    public String Atributo14;
    public String Atributo15;
    public String processts;



    protected Productko() {}

    public Productko(String CodInterno) {

        this.CodInterno = CodInterno;

    }



}
