package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "stock_ok")

public class Stock {

    @Id
    public String idnavision;
    public String codinterno;
    public String stock;


    protected Stock() {}

    public Stock(String CodInterno, String IdNavision, String Stock) {
        this.codinterno = CodInterno;
        this.idnavision = IdNavision;
        this.stock = Stock;

    }

    @Override
    public String toString() {
        return String.format(
                "Product[name=%s, description='%s']",codinterno, stock);
    }
}
