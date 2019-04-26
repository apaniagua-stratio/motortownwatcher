package com.stratio.microservice.motortownwatcher.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "csvproduct", schema = "motortown")

public class Csvproduct {

    @Id
    public String CodInterno;
    public String EAN;
    public String Matricula;
    public String DescripcionAlias;
    public String Descripcion02;
    public String Bondades;
    public String Estado;
    public String Categoria;
    public String GrupoProducto;
    public String ActivoExterno;
    public String Atributo01;
    public String Atributo02;
    public String Atributo03;
    public String Atributo04;
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
    public String Atributo16;
    public String Precio;
    public String linkImgL;
    public String linkImgM;
    public String linkImgS;
    public String PerteneceGrupo01;
    public String PerteneceGrupo02;
    public String PerteneceGrupo03;
    public String PerteneceGrupo04;
    public String PerteneceGrupo05;
    public String ServiciosObligatorios;
    public String ServiciosOpcionales;
    public String DeshabilitadoVenta;
    public String ActualizarImagen;
    public String ActualizarProductos;
    public String FamiliaAntigua;



    protected Csvproduct() {}


}
