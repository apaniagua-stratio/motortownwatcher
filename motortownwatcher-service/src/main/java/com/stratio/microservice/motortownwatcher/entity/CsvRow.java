package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "aurgicsvrows", schema = "motortown")

public class CsvRow {

    @Id
    @GeneratedValue
    private UUID id;
    public String content;
    public String processts;
    public String entity;
    public String originalfile;


    protected CsvRow() {}

    public CsvRow(String content,String entity, String originalfile) {
        //this.rowid = rowid;
        this.content = content;
        this.processts = new Date().toString();
        this.entity= entity;
        this.originalfile=originalfile;
    }

}
