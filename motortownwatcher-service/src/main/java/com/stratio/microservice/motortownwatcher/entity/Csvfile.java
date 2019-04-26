package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "aurgifiles", schema = "motortown")

public class Csvfile {

    @Id
    public String fileid;
    public String filename;
    public String filedate;



    protected Csvfile() {}

    public Csvfile(String filename, String filedate) {
        this.filename = filename;
        this.filedate = filedate;
        this.fileid = filename + "&" + filedate;
    }

    @Override
    public String toString() {
        return String.format(
                "Csvfile[name=%s, date='%s']",
                filename, filedate);
    }
}
