package uy.agesic.direcciones;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.repository.GeocodeRepository;
import uy.agesic.direcciones.utils.GeocodingUtils;

@Component
@Endpoint(id = "customhealth")
public class GeocoderCustomHealth {
   
	@Autowired
	private GeocodeRepository repository;
	private static Logger logger = LoggerFactory.getLogger(GeocoderCustomHealth.class);
 
    @ReadOperation
    public CustomHealth health() {    	
    	Map<String, Object> details = new LinkedHashMap<>();
    	try {
    		int numCapasPol = repository.getCapasPoligonales().size();
    		if (numCapasPol > 0)
    			details.put("CustomHealthStatus", "UP");
    	}
    	catch (Exception e) {
    		logger.debug(e.getMessage());
    		details.put("CustomHealthStatus", "DOWN." + e.getMessage());
    	}
        
        CustomHealth health = new CustomHealth();
        health.setHealthDetails(details);
        return health;
    }

    @ReadOperation
    public String customEndPointByName(@Selector String name) {
        return "custom-end-pointh";
    }


  
}
