package uy.agesic.direcciones.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.openlocationcode.OpenLocationCode;
import com.google.openlocationcode.OpenLocationCode.CodeArea;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import springfox.documentation.spring.web.json.Json;
import uy.agesic.direcciones.data.CapaPoligonalResult;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.TramoResult;
import uy.agesic.direcciones.repository.GeocodeRepository;
import uy.agesic.direcciones.utils.GeocodingUtils;

@RestController
@RequestMapping("api/v1/geocode")
@CrossOrigin
@Api(value = "Geocode", description= "Se recomienda usar los nuevos servicios ( api/v1 ). \r\n"
		+ "En los resultados de tipo GeocoderResult se devuelve un campo de estado de la geocodificación: \r\n" +
		"GeocoderResult: El campo state y stateMsg indican si la geocodificación es válida o aproximada.\r\n" + 
		"Si stateMsg es Aproximado y el resultado puede ser un portal cercano, o el centroid de la calle.\r\n" + 
		"Si pone GEOMETRIA DE CALLE NO ENCONTRADA, en la base de datos existen portales, pero no una calle como línea.", tags = {"Geocode"})
public class GeocodeController implements IGeocodeV1 {
	
    @Autowired
    private GeocodeRepository geocodeRepository;
    private Logger logger = LoggerFactory.getLogger(GeocodeController.class);
    
    private FuzzyScore fuzzyScore = new FuzzyScore(Locale.forLanguageTag("es-ES"));

//	@CrossOrigin
	// @RequestMapping
//    public GeocoderResult getGeocodingSuggest(@RequestParam(value = "q", required = true) String query) {
//        Slice<GeocoderResult> result= geocodeRepository.findSlice(query, 
//        		PageRequest.of(0, 10, Sort.Direction.ASC, "localidad", "nombre"));
//
//        if(result!=null && result.getContent()!=null && !result.getContent().isEmpty()){
//            return result.getContent().get(0);
//        }
//        return null;
//        
////        GeocoderResult testResult = new GeocoderResult();
////        testResult.setAddress("My Street");
////        result.add(testResult);
//
//    }
	
//    @GetMapping("/calle")
//    public List<GeocoderResult> getGeocoding2(@RequestParam(value = "q", required = true)
//    		String query, @RequestParam(value="limit", required = false, defaultValue = "10") int limit) {
//    	long t = System.currentTimeMillis();   
//        List<GeocoderResult> res = geocodeRepository.findCalle(query, limit);
//    	t = System.currentTimeMillis() - t;
//    	logger.info("Time " + t + " msecs");
//    	
//    	return res;
//    }
    
    private List<GeocoderResult> getOLCGeocoderResults(EntradaNormalizada en) {
    	if (en.getTipoDirec() == EntradaNormalizada.TipoDirec.OLC) {
    		// Es un OLC, así que devolvemos el candidato único.
    		OpenLocationCode olc = new OpenLocationCode(en.getOLC());
    		if (olc.isShort()) {
    			// TODO
//    			olc.recover(referenceLatitude, referenceLongitude);
    		}
    		else {
	    		// TODO: BUSCAR EN LOS OLC GUARDADOS DE Portales y POIs
	    		// TODO: Rellenar también el departamento, y quizás la localidad.
	    		// (Si lo permite la velocidad). Si no, hacerlo cuando el usuario hace intro
	    		List<GeocoderResult> res = new ArrayList<GeocoderResult>();
	    		GeocoderResult aux = new GeocoderResult();
	    		aux.setAddress(en.getEntrada());
	    		aux.setNomVia(en.getEntrada());
	    		CodeArea codeArea = olc.decode();
	    		double lat = codeArea.getCenterLatitude();
	    		double lng = codeArea.getCenterLongitude();
	    		aux.setLat(lat);
	    		aux.setLng(lng);
	    		aux.setType(en.getTipoDirec());
	    		aux.setOLC(en.getOLC());
	    		res.add(aux);
	    		return res;
    		}
    	}
    	return null;
    }
    
    @Override
	@GetMapping(value = "/candidates")
    @ApiOperation(value="Buscar candidatos (usar en autocompletado)",  
    notes="http://{host}:{port}/api/v1/geocode/candidates?q=sucre&soloLocalidad=false&limit=100",
    tags = {"Geocode, candidatos"})
    public ResponseEntity<List<GeocoderResult>> getCandidates(@RequestParam(value = "q", required = true) String query,
    		@RequestParam(value = "soloLocalidad", required = false, defaultValue = "false") boolean onlyLocalidad,
    		@RequestParam(value="limit", required = false, defaultValue = "10") int limit) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "candidates"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	if (limit > 200)
		{
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
		}
    	
    	// FJP: EN OPENSHIFT SE RECIBE ESTO COMO ENCODED URL!!! En cambio, en Spring Boot se recibe bien. Pruebo a hacer el url decode aquí.
    	String queryDecoded = query;
    	try {
    	    queryDecoded = java.net.URLDecoder.decode(query, StandardCharsets.UTF_8.name());
    	} catch (UnsupportedEncodingException e) {
    	    // not going to happen - value came from JDK's own StandardCharsets
    	}
    	logger.debug("getCandidates: Viene query= " + query + " y la transformo a " + queryDecoded);
    	EntradaNormalizada en = GeocodingUtils.parseAddress(query.toUpperCase());
    	if (en.getTipoDirec() == EntradaNormalizada.TipoDirec.OLC) {
    		List<GeocoderResult> res = getOLCGeocoderResults(en);
    		return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    	}
    	if (onlyLocalidad)
    	{
        	long t = System.currentTimeMillis();
        	if (en.getLocalidad() == null)
        		en.setLocalidad(en.getNomVia());

        	List<GeocoderResult> res = geocodeRepository.findCandidatesLocalidad(en, limit);
        	t = System.currentTimeMillis() - t;
        	logger.info("Time Localidad " + t + " msecs");    	
            return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);    		
    	}

    	long t = System.currentTimeMillis();
    	List<GeocoderResult> res = geocodeRepository.findCandidates(query.trim().toUpperCase(), limit, true);
		for (GeocoderResult r : res) {
			double score = r.getRanking(); 
			if (r.getType() == TipoDirec.LOCALIDAD) {
				// score = fuzzyScore.fuzzyScore(r.getAddress(), en.getEntrada()) / 10.0;
				score = FuzzySearch.weightedRatio(r.getAddress(), en.getEntrada()) / 10.0;
				score = score + 20;
			}
			if (r.getType() == TipoDirec.POI) {
				// score = fuzzyScore.fuzzyScore(r.getAddress(), en.getEntrada()) / 10.0;
				score = FuzzySearch.weightedRatio(r.getAddress(), en.getEntrada()) / 10.0;
				score = score + 15;
			}
			if (r.getType() == TipoDirec.ESQUINA) {
				score = score + 10;
			}
			if (r.getType() == TipoDirec.CALLEyPORTAL) {
				score = score + 5;
			}
	
			r.setRanking(score);
		}
		res.sort(null);
		res = res.stream().limit(limit).collect(Collectors.toList());
    	
    	t = System.currentTimeMillis() - t;
    	logger.info("Time candidates " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/find")
    @ApiOperation(value="Buscar direcciones por coincidencia exacta de varios parámetros (usar después de autocompletado)",
    	notes="Valores posibles de type: calle, solar, inmueble, ruta, esquina, localidad \n" +
    			"http://{host}:{port}/api/v1/geocode/find?type=calle&idcalle=8096",
    	tags = {"Geocode, find"})
    public ResponseEntity<List<GeocoderResult>> getAddress(@RequestParam(value = "type", required = true) String type,
    		@RequestParam(value="idcalle", required = false) String idcalle,
    		@RequestParam(value="nomvia", required = false) String nomvia,
    		@RequestParam(value="localidad", required = false) String localidad,
    		@RequestParam(value="portal", required = false) String portal,
    		@RequestParam(value="departamento", required = false) String departamento,
    		@RequestParam(value="manzana", required = false) String manzana,
    		@RequestParam(value="solar", required = false) String solar,
    		@RequestParam(value="inmueble", required = false) String inmueble,
    		@RequestParam(value="ruta", required = false) String ruta,
    		@RequestParam(value="km", required = false) String km,
    		@RequestParam(value="letra", required = false) String letra,
    		@RequestParam(value="idcalleEsq", required = false) String idcalle2
    		) {

    	if (!geocodeRepository.isMethodAllowed("v1", "find"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	
    	long t = System.currentTimeMillis();
    	
    	String typeLower = type.toLowerCase();
    	
    	if (letra != null && portal == null)
    		portal = "0";

    	List<GeocoderResult> res = null;
    	switch (typeLower) {
    		case "esquina":
    			res = geocodeRepository.findCornersOfStreetByIdcalle(Integer.parseInt(idcalle), Integer.parseInt(idcalle2));
    			break;
    		case "localidad":
    			res = geocodeRepository.findLocalidad(localidad, departamento);
    			break;
    		case "calle":
    			if (idcalle != null) {
        			if (portal == null)
        				res = geocodeRepository.findCalleByIdCalle(idcalle);
        			else
        				res = geocodeRepository.findCalleYportal2(idcalle, Integer.parseInt(portal), letra);
    			}
    			else { // deprecated. Usar idcalle en lugar de nomvia, localidad y departamento (más rápido)
	    			if (portal == null)
	    				res = geocodeRepository.findCalle(nomvia, localidad, departamento);
	    			else
	    				res = geocodeRepository.findCalleYportal(nomvia, Integer.parseInt(portal), localidad, departamento);
    			}
    			break;
    		case "calleyportal":
    			if (idcalle != null) {
    				if (null == portal)
    					portal = "0";
       				res = geocodeRepository.findCalleYportal2(idcalle, Integer.parseInt(portal), letra);
       				List<GeocoderResult> exact = new ArrayList<GeocoderResult>();
       				for (GeocoderResult r : res) 
   					{
       					if (r.getState() == 1)
       						exact.add(r);
   					}
       				if (exact.size() > 0)
       					res = exact;
    			}
    			else { // deprecated. Usar idcalle en lugar de nomvia, localidad y departamento (más rápido)
	    			if (portal == null)
	    				res = geocodeRepository.findCalle(nomvia, localidad, departamento);
	    			else
	    				res = geocodeRepository.findCalleYportal(nomvia, Integer.parseInt(portal), localidad, departamento);
    			}
    			break;    			
    		case "solar":
    		case "manzanaysolar":
    			res = geocodeRepository.findSolar(manzana, solar, localidad, departamento);    			
    			break;
    		case "inmueble":
    			res = geocodeRepository.findInmueble(inmueble, localidad, departamento);    			
    			break;
    		case "ruta":
    		case "rutaykm":
    			EntradaNormalizada en = GeocodingUtils.parseAddress(nomvia + " KM " + km);
    			res = geocodeRepository.findRuta(en.getRuta(), en.getKm(), 1);
    			break;    			    			
    		case "olc":
    			EntradaNormalizada en2 = GeocodingUtils.parseAddress(nomvia);
    	    	if (en2.getTipoDirec() == EntradaNormalizada.TipoDirec.OLC) {
    	    		res = getOLCGeocoderResults(en2);
    	    	}
    	    	break;
    	}
    	t = System.currentTimeMillis() - t;
    	if (res != null)
    		logger.info("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }
    
    @Override
	@GetMapping(value = "/findEsq")
    @ApiOperation(value="Buscar esquinas de una calle. Devuelve las calles que intersectan con esa calle y los puntos de intersección.",  
    	    notes="http://{host}:{port}/api/v1/geocode/findEsq?idcalle=8096",
    	    tags = {"Geocode, Esquinas"})
    public ResponseEntity<List<GeocoderResult>> getCornersOfStreet(
    		@RequestParam(value="idcalle", required = true) int idcalle
    		) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "findEsq"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}


    	long t = System.currentTimeMillis();

    	List<GeocoderResult> res = null;
		res = geocodeRepository.findCornersOfStreet(idcalle);
    	t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    
    @Override
	@GetMapping(value = "/cruces")
    @ApiOperation(value="Dado el nombre de un departamento, el nombre de una localidad, el nombre de calle y "
    		+ "una cadena de texto (opcional), se devuelven las calles que cruzan la calle ingresada."
    		+ " Si se suministra la cadena de texto, el resultado se filtra por las calles que contengan esa cadena de texto.",    		
    	    notes="http://{host}:{port}/api/v1/geocode/cruces?departamento=Montevideo&localidad=Montevideo&calle=Sucre",
    		tags = {"Geocode, cruces, esquinas"})
    public ResponseEntity<List<GeocoderResult>> getCruces(
    		@RequestParam(value="departamento", required = true) String departamento,
    		@RequestParam(value="localidad", required = true) String localidad,
    		@RequestParam(value="calle", required = true) String calle,
    		@RequestParam(value="q", required = false) String filtro    		
    		) {

    	if (!geocodeRepository.isMethodAllowed("v1", "cruces"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();

    	List<GeocoderResult> res = null;
    	String entrada = calle + ", " + localidad + ", " + departamento;
    	EntradaNormalizada en = GeocodingUtils.parseAddress(entrada);
    	res = geocodeRepository.findCandidatesCalle(en, 3);
    	List<GeocoderResult> cruces = null;
    	List<GeocoderResult> result = new ArrayList<GeocoderResult>();
    	for (GeocoderResult r : res) {
    		cruces = geocodeRepository.findCornersOfStreet(r.getIdCalle());
        	if (filtro != null) {
        		String filtroL = filtro.toLowerCase();
        		Predicate<GeocoderResult> p = item -> item.getNomVia().toLowerCase().contains(filtroL);   
        		cruces = cruces.stream().filter(p).collect(Collectors.toList());
        	}

    		for (GeocoderResult rEsq : cruces) {
    			GeocoderResult resEsq = geocodeRepository.crearEsquinaResult(r, rEsq);			
    			result.add(resEsq);
    		}
    	}

    	
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs. Registros: " + result.size());    	
        return new ResponseEntity<List<GeocoderResult>>(result, HttpStatus.OK);
    }
    
    @Override
	@GetMapping(value = "/crucesPorIdCalle")
    @ApiOperation(value="Dado el idcalle y "
    		+ "una cadena de texto (opcional), se devuelven las calles que cruzan la calle ingresada."
    		+ " Si se suministra la cadena de texto, el resultado se filtra por las calles que contengan esa cadena de texto.",
    		notes="http://{host}:{port}/api/v1/geocode/candidates?idcalle=9372&q=javier",
    		tags = {"Geocode, cruces, esquinas"})
    public ResponseEntity<List<GeocoderResult>> getCrucesPorIdCalle(
    		@RequestParam(value="idcalle", required = true) int idcalle,
    		@RequestParam(value="q", required = false) String filtro    		
    		) {

    	if (!geocodeRepository.isMethodAllowed("v1", "crucesPorIdCalle"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();

    	List<GeocoderResult> cruces = null;
    	List<GeocoderResult> result = new ArrayList<GeocoderResult>();

    	List<GeocoderResult> res = geocodeRepository.findCalleByIdCalle(idcalle + "");

    	for (GeocoderResult r : res) {
	   		cruces = geocodeRepository.findCornersOfStreet(idcalle);    		
	    	if (filtro != null) {
	    		String filtroL = filtro.toLowerCase();
	    		Predicate<GeocoderResult> p = item -> item.getNomVia().toLowerCase().contains(filtroL);   
	    		cruces = cruces.stream().filter(p).collect(Collectors.toList());
	    	}
	
			for (GeocoderResult rEsq : cruces) {
				GeocoderResult resEsq = geocodeRepository.crearEsquinaResult(r, rEsq);			
				result.add(resEsq);
			}
    	}
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs. Registros: " + result.size());    	
        return new ResponseEntity<List<GeocoderResult>>(result, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/crucesConRadio")
    @ApiOperation(value="Dado el nombre de un departamento, el nombre de una localidad, el nombre de calle, "
    		+ "una cadena de texto (opcional),  un punto de coordenadas latitud y longitud (en EPSG 4326), un radio"
    		+ " de búsqueda en metros, se devuelven las calles que cruzan la calle ingresada."
    		+ " Si se suministra la cadena de texto, el resultado se filtra por las calles que contengan esa cadena de texto.",
    		tags = {"Geocode, cruces, esquinas, radio"})
    public ResponseEntity<List<GeocoderResult>> getCrucesConRadio(
    		@RequestParam(value="departamento", required = true) String departamento,
    		@RequestParam(value="localidad", required = true) String localidad,
    		@RequestParam(value="calle", required = true) String calle,
    		@RequestParam(value="latitud", required = true) double latitud,
    		@RequestParam(value="longitud", required = true) double longitud,
    		@RequestParam(value="radio", required = true) Double radio,
    		@RequestParam(value="q", required = false) String filtro    		
    		) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "crucesConRadio"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();

    	List<GeocoderResult> res = null;
    	String entrada = calle + ", " + localidad + ", " + departamento;
    	EntradaNormalizada en = GeocodingUtils.parseAddress(entrada);
    	res = geocodeRepository.findCandidatesCalle(en, 3);

    	List<GeocoderResult> cruces = null;
    	List<GeocoderResult> result = new ArrayList<GeocoderResult>();
    	for (GeocoderResult r : res) {
    		if (r.getType() == TipoDirec.CALLE)
    		{
    			cruces = geocodeRepository.findCornersOfStreet(r.getIdCalle());
    	    	List<GeocoderResult> cruces2 = new ArrayList<GeocoderResult>();
    	    	for (GeocoderResult r2 : cruces) {
    	    		double dist = GeocodingUtils.calculateDistance(longitud, latitud, r2.getLng(), r2.getLat());
    	    		if (dist <= radio) {
    	    			cruces2.add(r2);
    	    		}    		
    	    	}
    	    	if (filtro != null) {
    	    		String filtroL = filtro.toLowerCase();
    	    		Predicate<GeocoderResult> p = r2 -> r2.getNomVia().toLowerCase().contains(filtroL);   
    	    		cruces2 = cruces2.stream().filter(p).collect(Collectors.toList());
    	    	}
    			for (GeocoderResult rEsq : cruces2) {
    				GeocoderResult resEsq = geocodeRepository.crearEsquinaResult(r, rEsq);			
    				result.add(resEsq);
    			}
    		}
    	}

    	
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs. Registros: " + result.size());    	
        return new ResponseEntity<List<GeocoderResult>>(result, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/rutakm")
    @ApiOperation(value="Dado un ruta y km, se devuelve un punto ubicado sobre esa ruta.",
    tags = {"Geocode, ruta, km"})
    public ResponseEntity<List<GeocoderResult>> getRutaKm(
    		@RequestParam(value="ruta", required = true) String ruta,
    		@RequestParam(value="km", required = true) Double km
    		) {
    	if (!geocodeRepository.isMethodAllowed("v1", "rutakm"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();

    	List<GeocoderResult> res = null;
    	res = geocodeRepository.findRuta(ruta, km, 2);
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }




    @Override
	@GetMapping("/reverse")
    @ApiOperation(value="Geocodificación Inversa. Latitud y longitud en EPSG:4326", 
    tags="geocode, reverse, inversa")
    public List<GeocoderResult> getReverseGeocoding(
    		@RequestParam(value = "latitud", required = true) double lat,
    		@RequestParam(value = "longitud", required = true) double lng,
    		@RequestParam(value = "limit", defaultValue = "10") int limit
    		)
    {
    	if (!geocodeRepository.isMethodAllowed("v1", "reverse"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	if (limit > 200)
		{
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
		}
    	long t = System.currentTimeMillis();
    	List<GeocoderResult> res = geocodeRepository.reverse(lat, lng, limit); 
    	
    	res.sort(new Comparator<GeocoderResult>() {

			@Override
			public int compare(GeocoderResult o1, GeocoderResult o2) {
				double distEstimation2 = Math.pow((o2.getLng() - lng),2) + Math.pow((o2.getLat() - lat),2);
				double distEstimation1 = Math.pow((o1.getLng() - lng),2) + Math.pow((o1.getLat() - lat),2); 
				if (distEstimation2 > distEstimation1)
					return -1;
				if (distEstimation2 < distEstimation1)
					return 1;
				return 0;					
			}
    		
    	});
    	
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
    	// res.forEach(System.out::println);
        return res;
    }


    
    @Override
	@GetMapping(value = "/fuzzyGeocode")
    @ApiOperation(value="Geocodificar una dirección buscando de manera que no afecten los errores al escribir. Es algo más lenta.", tags = {"Geocode"})
    public ResponseEntity<List<GeocoderResult>> fuzzyGeocode(@RequestParam(value = "q", required = true) String query,
    		@RequestParam(value = "soloLocalidad", required = false, defaultValue = "false") boolean onlyLocalidad,
    		@RequestParam(value="limit", required = false, defaultValue = "10") int limit) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "fuzzyGeocode"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	if (limit > 200)
		{
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
		}
    	
    	EntradaNormalizada en = GeocodingUtils.parseAddress(query);
    	if (en.getTipoDirec() == EntradaNormalizada.TipoDirec.OLC) {
    		List<GeocoderResult> res = getOLCGeocoderResults(en);
    		return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    	}

    	if (onlyLocalidad)
    	{
        	long t = System.currentTimeMillis();
        	if (en.getLocalidad() == null)
        		en.setLocalidad(en.getNomVia());
        	
        	List<GeocoderResult> res = geocodeRepository.findCandidatesLocalidad(en, limit);
        	t = System.currentTimeMillis() - t;
        	logger.debug("Time Localidad " + t + " msecs");    	
            return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);    		
    	}

    	long t = System.currentTimeMillis();
    	List<GeocoderResult> res = geocodeRepository.fuzzyGeocode(query.trim(), limit);
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/direcUnica")
    @ApiOperation(value="Geocodificar una dirección única. Se puede usar el parámetro limit para hacer pruebas", tags = {"Geocode"})
    public ResponseEntity<List<GeocoderResult>> direcUnica(
    		@RequestParam(value = "q", required = true) String query,
    		@RequestParam(value = "limit", required = false, defaultValue = "1") int limit
    	)
    {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "direcUnica"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}
    	
    	EntradaNormalizada en = GeocodingUtils.parseAddress(query);
    	// FJP: EN OPENSHIFT SE RECIBE ESTO COMO ENCODED URL!!! En cambio, en Spring Boot se recibe bien. Pruebo a hacer el url decode aquí.
    	String queryDecoded = query;
    	try {
    	    queryDecoded = java.net.URLDecoder.decode(query, StandardCharsets.UTF_8.name());
    	} catch (UnsupportedEncodingException e) {
    	    // not going to happen - value came from JDK's own StandardCharsets
    	}
    	if (en.getTipoDirec() == EntradaNormalizada.TipoDirec.OLC) {
    		// TODO: REVISAR ESTO PARA QUE SALGAN LOS OLC GUARDADOS PRIMERO
    		List<GeocoderResult> res = getOLCGeocoderResults(en);
    		return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    	}

    	long t = System.currentTimeMillis();
    	List<GeocoderResult> resCand = geocodeRepository.findCandidates(queryDecoded.trim(), 20, false);
    	List<GeocoderResult> res = new ArrayList<GeocoderResult>();
    	for (GeocoderResult r1 : resCand) {
			switch (r1.getType())
			{
				case CALLE:
				case CALLEyPORTAL:
				{
					
					if (en.getPortal() >= 0) 
					{
						List<GeocoderResult> resCallePortal = geocodeRepository.findCalleYportal2(r1.getIdCalle() + "", r1.getPortalNumber(), r1.getLetra());
						for (GeocoderResult rAux : resCallePortal) {
							rAux.setRanking(r1.getRanking());
						}						
						res.addAll(resCallePortal);
					}
					else {
						List<GeocoderResult> resCalle = geocodeRepository.findCalleByIdCalle(r1.getIdCalle() + "");
						for (GeocoderResult rAux : resCalle) {
							rAux.setRanking(r1.getRanking());
						}
						res.addAll(resCalle);
					}
					break;
				}
				case MANZANAySOLAR:
				case RUTAyKM:
				case ESQUINA:
				case POI:
					res.add(r1);
					break;
				default: continue;
			}

		}
    	
		for (GeocoderResult r : res) {
//			double score = FuzzySearch.weightedRatio(r.getAddress().substring(0, 14), en.getEntrada()) / 10.0;
			double score = r.getRanking();
			if (r.getState() == 1) {
				score = score + 0.0000001;
			}
//			if (r.getType() == TipoDirec.POI) {
//				if (en.getTipoDirec() != TipoDirec.CALLEyPORTAL) {
//					score = score + 15;	
//				}
//				else if (en.getTipoDirec() != TipoDirec.MANZANAySOLAR) {
//					score = score + 15;	
//				}				
//				else if (en.getTipoDirec() != TipoDirec.ESQUINA) {
//					score = score + 15;	
//				}				
//				else if (en.getTipoDirec() != TipoDirec.RUTAyKM) {
//					score = score + 15;	
//				}				
//			}
//			else
//			{
//				if (en.getTipoDirec() == TipoDirec.CALLEyPORTAL) {
//					if (r.getType() == TipoDirec.CALLEyPORTAL) {
//						score = score + 15;
//					}
//				}
//				if (en.getTipoDirec() == TipoDirec.ESQUINA) {
//					if (r.getType() == TipoDirec.ESQUINA) {
//						score = score + 15;
//					}
//				}
//				if (en.getTipoDirec() == TipoDirec.MANZANAySOLAR) {
//					if (r.getType() == TipoDirec.MANZANAySOLAR) {
//						score = score + 15;
//					}
//				}
//				if (en.getTipoDirec() == TipoDirec.RUTAyKM) {
//					if (r.getType() == TipoDirec.RUTAyKM) {
//						score = score + 15;
//					}
//				}				
//			}

			r.setRanking(score);
		}
		res.sort(null);
		List<GeocoderResult> bestBet = res.stream().limit(limit).collect(Collectors.toList());
    	
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(bestBet, HttpStatus.OK);

    }

    
    @Override
	@GetMapping(value = "/direcEnPoligono")
    @ApiOperation(value="Recibe un polígono en EPSG 4326 y GeoJson, y devuelve"
    		+ "las direcciones que contiene ese polígono."
    		+ "El usuario puede especificar si quiere portales, solares, y/o puntos de interés."
    		+ "(Parámetro tipoDirec con valores como portal, solar, poi)",
    		notes = "Ejemplo: http://{host}:{port}/api/v1/geocode/direcEnPoligono?limit=100&poligono={\"type\":\"Polygon\",\"coordinates\":[[[-56.1720, -34.8716],[-56.1690, -34.8754],[-56.1757, -34.8773],[-56.1720, -34.8716]]]}&tipoDirec=portales",
    		tags = {"Geocode, direcciones, poligono"})
    public ResponseEntity<List<GeocoderResult>> direcEnPoligono(
    		@RequestParam(value = "poligono", required = true) String poligono,
    		@RequestParam(value = "tipoDirec", required = false, defaultValue = "portales") String tipoDirec,
    		@RequestParam(value="limit", required = false, defaultValue = "100") int limit) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "direcEnPoligono"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}
    	
    	if (limit > 200)
		{
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
		}
    	
        	

    	long t = System.currentTimeMillis();
    	
    	List<GeocoderResult> res = new ArrayList<GeocoderResult>();
    	if (tipoDirec.contains("portal")) {
    		res.addAll(geocodeRepository.getPortalesDentroDe(poligono, limit));    		
    	}
    	
    	if (tipoDirec.contains("poi")) {
    		res.addAll(geocodeRepository.getPoisDentroDe(poligono, limit));
    	}
    	
    	if (tipoDirec.contains("solar")) {
    		res.addAll(geocodeRepository.getSolaresDentroDe(poligono, limit));
    	}
    			
    	t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/poligono")
    @ApiOperation(value="Recibe un identificador de capa y un identificador de polígono "
    		+ "dentro de esa capa y devuelve "
    		+ "las direcciones que contiene ese polígono. Usar junto con el servicio de listar capas poligonales."
    		+ "El usuario puede especificar si quiere portales, solares, o puntos de interés."
    		+ "(Parámetro tipoDirec con valores como portal o solar o poi",
    		notes="http://{host}:{port}/api/v1/geocode/poligono?capa=departamento&id=1&tipoDirec=portal&limit=100", 
    		tags = {"Geocode, direcciones, poligono"})
    public ResponseEntity<List<GeocoderResult>> poligono (
    		@RequestParam(value = "capa", required = true) String capa,
    		@RequestParam(value = "id", required = true) int id,
    		@RequestParam(value = "tipoDirec", required = false, defaultValue = "portal") String tipoDirec,
    		@RequestParam(value="limit", required = false, defaultValue = "100") int limit) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "poligono"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	if (limit > 200)
		{
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
		}
    	
        	

    	long t = System.currentTimeMillis();
    	
    	List<GeocoderResult> res = new ArrayList<GeocoderResult>();
    	CapaPoligonalResult c = geocodeRepository.getCapaPoligonal(capa);
    	if (c == null) {
    		throw new ResponseStatusException(
    		           HttpStatus.NOT_FOUND, "Tabla no encontrada:" + capa, new NullPointerException());
    	}
   		String geoJson = geocodeRepository.getPoligono(c.getTabla(), c.getCampo_id(), c.getCampoGeom(), id);
   		switch (tipoDirec)
   		{
   		case "portal":
   			res.addAll(geocodeRepository.getPortalesDentroDe(geoJson, limit));
   			break;
   		case "poi":
   			res.addAll(geocodeRepository.getPoisDentroDe(geoJson, limit));
   			break;
   		case "solar":
   			res.addAll(geocodeRepository.getSolaresDentroDe(geoJson, limit));
   			break;
   		}
   		    		

   		t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }
    

    @Override
	@GetMapping(value = "/capasPoligonales")
    @ApiOperation(value="Devuelve las capas poligonales en el sistema. Usar junto con el servicio de "
    		+ "obtener direcciones dentro de un polígono \n"
    		+ "Ejemplo: http://{host}:{port}/api/v1/geocode/capasPoligonales", 
    		tags = {"Geocode, capas, poligonales"})
    public ResponseEntity<List<CapaPoligonalResult>> getCapasPoligonales () {
    	if (!geocodeRepository.isMethodAllowed("v1", "capasPoligonales"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	List<CapaPoligonalResult> res = geocodeRepository.getCapasPoligonales();
        return new ResponseEntity<List<CapaPoligonalResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/tramosCalle")
    @ApiOperation(value="Devuelve los tramos de una calle como un GeoJson. Usar junto con el servicio de "
    		+ "direcciones para obtener primero el idcalle \n"
    		+ "Ejemplo: http://{host}:{port}/api/v1/geocode/tramosCalle", 
    		tags = {"Geocode, tramos, geometria, geojson"})
    public ResponseEntity<String> getTramos (int idCalle) {
    	if (!geocodeRepository.isMethodAllowed("v1", "capasPoligonales"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	List<TramoResult> res = geocodeRepository.getTramos(idCalle);
    	JSONObject featureCollection = new JSONObject();
    	featureCollection.put("type", "FeatureCollection");
    	JSONObject properties = new JSONObject();
    	properties.put("name", "ESPG:4326");
    	JSONObject crs = new JSONObject();
    	crs.put("type", "name");
    	crs.put("properties", properties);
    	featureCollection.put("crs", crs);

    	JSONArray features = new JSONArray();
    	for (TramoResult t : res) {
        	JSONObject feature = new JSONObject();
        	feature.put("type", "Feature");
        	String geom = t.getGeom();

        	feature.put("id", t.getGid());
//        	String aux = geom.subSequence(1, geom.length()-1).toString();
        	JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        	JSONObject geomObj;
			try {
				geomObj = (JSONObject) parser.parse(geom);
	        	feature.put("geometry", geomObj);
	        	
	        	JSONObject featProps = new JSONObject();
	        	featProps.put("gid", t.getGid());
	        	featProps.put("idcalle", t.getIdcalle());
	        	featProps.put("fuente_id", t.getFuente_id());
	        	featProps.put("tipo_vialidad_id", t.getTipo_vialidad_id());
	        	feature.put("properties", featProps);
	
	        	features.add(feature);
			} catch (ParseException e) {
				e.printStackTrace();
			}	        	
    	}
    	featureCollection.put("features", features);    	
    	
        return new ResponseEntity<String>(featureCollection.toString(), HttpStatus.OK);
    }


    @Override
	@GetMapping(value = "/direcManzaSolar")
    @ApiOperation(value="Devuelve las direcciones asociadas a una manzana y solar", 
    tags = {"Geocode, manzana, solar"})
    public ResponseEntity<List<GeocoderResult>> getDirecManzaSolar (
    		@RequestParam(value = "departamento", required = true) String departamento,
    		@RequestParam(value = "localidad", required = true) String localidad,
    		@RequestParam(value = "manzana", required = true) int manzana,
    		@RequestParam(value = "solar", required = true) int solar,
    		@RequestParam(value="limit", required = false, defaultValue = "100") int limit
    		) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "direcManzaSolar"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();
    	
    	List<GeocoderResult> res = geocodeRepository.getDirecPorManzanaSolar(departamento, localidad, manzana, solar, limit); 
    	// geocodeRepository.findSolar(manzana + "", solar + "", localidad, departamento);
    	    		

   		t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/direcPadron")
    @ApiOperation(value="Devuelve las direcciones seleccionando por padron dentro de un departamento y localidad\n"
    		+ "Ejemplo: http://{server}:{port}/api/v1/geocode/direcPadron?departamento=Cerro Largo&localidad=Melo&padron=562", 
    		tags = {"Geocode, padron"})
    public ResponseEntity<List<GeocoderResult>> getDirecPadron (
    		@RequestParam(value = "departamento", required = true) String departamento,
    		@RequestParam(value = "localidad", required = true) String localidad,
    		@RequestParam(value = "padron", required = true) int padron,
    		@RequestParam(value="limit", required = false, defaultValue = "100") int limit
    		) {
    	
    	if (!geocodeRepository.isMethodAllowed("v1", "direcPadron"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();
    	
    	List<GeocoderResult> res = geocodeRepository.getDirecPorPadron(departamento, localidad, padron, limit);    		

   		t = System.currentTimeMillis() - t;
    	logger.debug("Time " + t + " msecs");    	
        return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
    }

    @Override
	@GetMapping(value = "/direcPuntoNotable")
    @ApiOperation(value="Devuelve las direcciones seleccionando por POI (Punto de Interés) dentro de un departamento\n"
    		+ "Ejemplo: http://{server}:{port}/api/v1/geocode/direcPuntoNotable?departamento=Cerro Largo&nombre=Correos", 
    		tags = {"Geocode, pois"})
    public ResponseEntity<List<GeocoderResult>> getDirecPuntoNotable (
		@RequestParam(value = "departamento", required = true) String departamento,
		@RequestParam(value = "nombre", required = true) String poi,
		@RequestParam(value="limit", required = false, defaultValue = "100") int limit
		) {
    	if (!geocodeRepository.isMethodAllowed("v1", "direcPuntoNotable"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

		long t = System.currentTimeMillis();
		
		List<GeocoderResult> res = geocodeRepository.getPois(departamento, poi, limit);    		
		
			t = System.currentTimeMillis() - t;
		logger.debug("Time " + t + " msecs");    	
		return new ResponseEntity<List<GeocoderResult>>(res, HttpStatus.OK);
	}

    
}
