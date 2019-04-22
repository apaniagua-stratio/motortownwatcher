package com.stratio.microservice.motortownwatcher.service.mapper;

import com.stratio.microservice.motortownwatcher.generated.rest.model.MicroserviceRequest;
import com.stratio.microservice.motortownwatcher.service.model.ServiceInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceRequestMapper {

  ServiceInput mapInput(MicroserviceRequest request);
}
