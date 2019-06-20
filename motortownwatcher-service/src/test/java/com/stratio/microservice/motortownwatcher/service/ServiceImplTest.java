package com.stratio.microservice.motortownwatcher.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.assertEquals;


import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ServiceImplTest {


    @Mock
    private ServiceImpl service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testService() {
        int i = service.getConfig().size();
        assertEquals(i,0);

    }


}
