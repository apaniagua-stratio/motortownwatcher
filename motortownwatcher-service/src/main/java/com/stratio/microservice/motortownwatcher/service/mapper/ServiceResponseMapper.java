package com.stratio.microservice.motortownwatcher.service.mapper;

import com.stratio.microservice.motortownwatcher.generated.rest.model.MicroserviceResponse;
import com.stratio.microservice.motortownwatcher.service.model.ServiceOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceResponseMapper {

  MicroserviceResponse mapOutput(ServiceOutput output);
}
