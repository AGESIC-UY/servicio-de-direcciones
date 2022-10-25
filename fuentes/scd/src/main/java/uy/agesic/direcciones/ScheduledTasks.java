package uy.agesic.direcciones;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.repository.GeocodeRepository;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	@Autowired
	private GeocodeRepository repository;

	// a las 2:30 a.m todos los días
	@Scheduled(cron = "0 30 02 * * *")
	public void refreshMaterializedViews() {
		log.info("FROM REFRESH MATERIALIZED VIEWS: The time is now {}", dateFormat.format(new Date()));
		
		repository.refreshMaterializedViews();
	}
}

