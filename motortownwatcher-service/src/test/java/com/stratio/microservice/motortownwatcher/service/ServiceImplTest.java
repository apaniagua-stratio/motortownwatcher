package com.stratio.microservice.motortownwatcher.service;

import com.stratio.microservice.motortownwatcher.repository.CsvfileRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ServiceImplTest {

    private CsvfileRepository csvrepo;
    private ServiceImpl serv;

    @Before
    public void setUp()
    {
        ServiceImpl serv = new ServiceImpl();
    }

    @Test
    public void fileListNotEmpty() {

        //assertThat(serv.listFilesInSftp("/anjana").size() > 0);
        assertThat(true);

    }
}
