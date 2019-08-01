package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "products_ok", schema = "motortown")

public class Product {

    @Id
    public String EAN;
    public String precio;
    public String originalfile;


    protected Product() {}

    public Product(String EAN, String Precio) {

        this.EAN = EAN;
        this.precio = Precio;
    }

    @Override
    public String toString() {
        return String.format(
                "Product[name=%s, description='%s']",EAN, precio);
    }
}
