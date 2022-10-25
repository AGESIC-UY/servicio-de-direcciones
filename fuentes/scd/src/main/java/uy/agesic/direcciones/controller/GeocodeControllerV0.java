package uy.agesic.direcciones.controller;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.text.similarity.FuzzyScore;
import org.hibernate.mapping.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import uy.agesic.direcciones.data.CalleNormalizada;
import uy.agesic.direcciones.data.Direccion;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.Inmueble;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.NombreNormalizadoDepartamento;
import uy.agesic.direcciones.data.NombreNormalizadoLocalidad;
import uy.agesic.direcciones.data.Numero;
import uy.agesic.direcciones.data.SugerenciaCalleV0;
import uy.agesic.direcciones.data.SugerenciaResultV0;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.repository.GeocodeRepository;
import uy.agesic.direcciones.utils.GeocodingUtils;

@RestController
@RequestMapping("api/v0/geocode")
@CrossOrigin
@Api(value = "Geocode", description = "Servicio compatible con el que había. Se recomienda usar el v1", tags = {"Geocode"})
public class GeocodeControllerV0 implements IGeocodeV0 {
	
	private FuzzyScore fuzzyScore = new FuzzyScore(Locale.forLanguageTag("es-ES"));
	
    @Autowired
    private GeocodeRepository geocodeRepository;
    private Logger logger = LoggerFactory.getLogger(GeocodeControllerV0.class);

	
    
//    @GetMapping(value = "/SugerenciaCalleCompleta")
//    @ApiOperation(value="Buscar candidatos (usar en autocompletado)", tags = {"Geocode"})
//    public ResponseEntity<List<SugerenciaCalleV0>> getSugerenciaCalleCompleta(@RequestParam(value = "entrada", required = true) String query,
//    		@RequestParam(value = "todos", required = false, defaultValue = "true") boolean todos,
//    		@RequestParam(value="limit", required = false, defaultValue = "10") int limit) {
//    	if (limit > 200)
//		{
//    		throw new ResponseStatusException(
//    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
//		}
//    	
//
//    	long t = System.currentTimeMillis();
//    	List<GeocoderResult> res = geocodeRepository.findCandidates(query.trim(), limit);
//    	
//    	List<SugerenciaCalleV0> resV0 = convertToSugerenciaV0(res);
//    	
//    	t = System.currentTimeMillis() - t;
//    	logger.info("Time " + t + " msecs");    	
//        return new ResponseEntity<List<SugerenciaCalleV0>>(resV0, HttpStatus.OK);
//    }
    
    private List<SugerenciaCalleV0> convertToSugerenciaV0(List<GeocoderResult> res) {
    	ArrayList<SugerenciaCalleV0> resV0 = new ArrayList<SugerenciaCalleV0>();
    	for (GeocoderResult r : res) {
    		SugerenciaCalleV0 rV0 = new SugerenciaCalleV0();
    		String direc;
        	if (r.getPortalNumber() > 0)
        		direc = r.getNomVia() + " " + r.getPortalNumber();
        	else
        		direc = r.getNomVia();

    		rV0.setCalle(direc);
    		rV0.setDepartamento(r.getDepartamento());
    		rV0.setLocalidad(r.getLocalidad());
    		
    		resV0.add(rV0);
    	}
		return resV0;
    	
    }

    private List<GeocoderResultV0> convertV0(List<GeocoderResult> res, EntradaNormalizada en) {
    	ArrayList<GeocoderResultV0> resV0 = new ArrayList<GeocoderResultV0>();
    	for (GeocoderResult r : res) {
    		GeocoderResultV0 rV0 = convertBusquedaDireccionV0(r, en);
    		resV0.add(rV0);
    	}
		return resV0;
	}

	private GeocoderResultV0 convertBusquedaDireccionV0(GeocoderResult r, EntradaNormalizada en) {
		GeocoderResultV0 rV0 = new GeocoderResultV0();
		Direccion d = new Direccion();
		if (r.getIdCalle() > 0) {
			CalleNormalizada cN = new CalleNormalizada();
			cN.setIdCalle(r.getIdCalle());
			cN.setNombreNormalizado(r.getNomVia());
			d.setCalle(cN);
			if (r.getPortalNumber() > 0)
			{
				Numero num = new Numero();
				num.setNro_puerta(r.getPortalNumber());
				d.setNumero(num);
			}		
			if (r.getType() == TipoDirec.POI) {
				Inmueble inmueble = new Inmueble();
				inmueble.setNombre(r.getInmueble());
				inmueble.setIdPuntoNotable(Integer.parseInt(r.getId()));
				d.setInmueble(inmueble);
			}
		}
				
		NombreNormalizadoDepartamento dN = new NombreNormalizadoDepartamento();
		dN.setNombreNormalizado(r.getDepartamento());
		dN.setIdDepartamento(r.getIdDepartamento());
		d.setDepartamento(dN);
		
		
		NombreNormalizadoLocalidad lN = new NombreNormalizadoLocalidad();
		lN.setNombreNormalizado(r.getLocalidad());
		lN.setIdLocalidad(r.getIdLocalidad());
		d.setLocalidad(lN);
	
		
		rV0.setDireccion(d);
		rV0.setIdPunto(Integer.parseInt(r.getId()));
		
		// String.format("%04d", r.getPostalCode()
		try {
			rV0.setCodigoPostal(Integer.parseInt(r.getPostalCode()));
		}
		catch (NumberFormatException e) {
			System.err.println(" PostalCode error: " + r.getPostalCode());
		}
		
		rV0.setPuntoX(r.getLng());
		rV0.setPuntoY(r.getLat());
		rV0.setSrid(4326);
		switch (r.getType())
		{
			case CALLEyPORTAL:
				rV0.setIdTipoClasificacion(1);
				if (r.getState() == 2) {
					rV0.setError("PUNTO NO ENCONTRADO.\nAPROXIMADO POR CALLE: \n");
				}
				break;
			case CALLE:
				rV0.setIdTipoClasificacion(27);
				if (en.getTipoDirec() == TipoDirec.CALLEyPORTAL) {
					rV0.setError("PUNTO NO ENCONTRADO.\nAPROXIMADO POR CALLE A: \n" + d.getCalle().getNombreNormalizado());
				}
				break;
			case POI:
				rV0.setIdTipoClasificacion(22);
				break;
			case ESQUINA:
				rV0.setIdTipoClasificacion(10);
				break;
			case MANZANAySOLAR:
				rV0.setIdTipoClasificacion(2);
				break;
			case RUTAyKM:
				rV0.setIdTipoClasificacion(14);
        		if (r.getLat() == 0.0)
        			rV0.setError("RUTA SIN CONTINUIDAD - IMPOSIBLE POSICIONAR");

				break;
			case LOCALIDAD:
				rV0.setIdTipoClasificacion(3); // Sin correspondencia en AGESIC
				if (en.getTipoDirec() != TipoDirec.LOCALIDAD) {
					rV0.setError("PUNTO NO ENCONTRADO.\nAPROXIMADO POR LOCALIDAD A: \n" + r.getLocalidad());
				}				
				break;				
		}
		
		// PUNTO NO ENCONTRADO.\nAPROXIMADO POR CALLE A: \nCONVENCION??
		
		return rV0;
	}
	
	private SugerenciaResultV0 convertToSugerenciaV0(GeocoderResult r) {
		SugerenciaResultV0 rV0 = new SugerenciaResultV0();
		rV0.setCalle(r.getNomVia());		
		rV0.setLocalidad(r.getLocalidad());
		rV0.setDepartamento(r.getDepartamento());
		rV0.setIdCalle(r.getIdCalle());
		rV0.setIdLocalidad(r.getIdLocalidad());
		
		return rV0;
	}

		
	@Override
	@GetMapping(value = "/BusquedaDireccion")
    @ApiOperation(value="El servicio de búsqueda de direcciones devuelve el conjunto de direcciones que\r\n" + 
    		"cumplen los criterios de búsqueda. Estas direcciones están compuestas por el\r\n" + 
    		"nombre de la calle, el número de puerta, manzana, solar, localidad, departamento,\r\n" + 
    		"código postal, ubicación geográfica (punto en SRID 4326), entre otros.",
    		notes = "Los parámetros que recibe son:\r\n" + 
    				"•departamento (opcional): Nombre del departamento\r\n" + 
    				"•localidad (opcional): Nombre de la localidad\r\n" + 
    				"•direccion: Dirección completa. Los formatos en los que se pueden buscar direcciones son (los campos entre “[]” son opcionales):\r\n" + 
    				"•calle [número] [, localidad] [, departamento]\r\n" + 
    				"•calle [número] esquina calle2 [, localidad] [, departamento] (“esquina” también se puede escribir como “esq.” o “esq”)\r\n" + 
    				"•[calle] manzana X solar Y [, localidad] [, departamento] (manzana también se puede escribir como man. o m. y solar como sol. o s.)\r\n" + 
    				"•nombre de inmueble [, localidad] [, departamento]" + 
    				"\r\n En la salida, el campo error indica lo siguiente:\r\n" + 
    				"•Vacío: El punto es válido, considerando que la búsqueda es tolerante a fallos tipográficos.\r\n" + 
    				"•PUNTO NO ENCONTRADO. APROXIMADO POR CALLE: No se ha encontrado un portal, pero sí la calle.\r\n" + 
    				"•RUTA SIN CONTINUIDAD - IMPOSIBLE POSICIONAR: No se ha encontrado la dirección por ruta y km\r\n" +
    				"•PUNTO NO ENCONTRADO.APROXIMADO POR LOCALIDAD: No se ha encontrado una calle similar, pero sí la localidad"
    				,
    		tags = {"Geocode"})
    public ResponseEntity<List<GeocoderResultV0>> getBusquedaDireccion(
    		@RequestParam(value="calle", required = true) String nomvia,
    		@RequestParam(value="localidad", required = false) String localidad,
    		@RequestParam(value="departamento", required = false) String departamento
    		) {

    	if (!geocodeRepository.isMethodAllowed("v0", "BusquedaDireccion"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();
    	
    	List<GeocoderResult> res = new ArrayList<GeocoderResult>();
    	List<GeocoderResult> resCand = null;
    	
    	String qAux = nomvia;
    	if (null != localidad)
    		qAux = qAux + "," + localidad;
    	if (null != departamento)
    		qAux = qAux + "," + departamento;
    	
    	// FJP: EN OPENSHIFT SE RECIBE ESTO COMO ENCODED URL!!! En cambio, en Spring Boot se recibe bien. Pruebo a hacer el url decode aquí.
    	String queryDecoded = qAux;
    	try {
    	    queryDecoded = java.net.URLDecoder.decode(qAux, StandardCharsets.UTF_8.name());
    	} catch (UnsupportedEncodingException e) {
    	    // not going to happen - value came from JDK's own StandardCharsets
    	}
    	logger.debug("V0 => getBusquedaDireccion: Viene query= " + qAux + " y la transformo a " + queryDecoded);


		EntradaNormalizada en = GeocodingUtils.parseAddress(queryDecoded);
		resCand = geocodeRepository.findCandidates(queryDecoded, 20, true);
		// Quitar localidades si hay inmueble, portal o calle.
		boolean bUseLocalidad = true;
	    for (GeocoderResult rAux : resCand) {
	    	switch (rAux.getType()) 
	    	{
		    	case POI:
		    	case ESQUINA:
				case CALLEyPORTAL:
					bUseLocalidad = false;
					break;
				default:					
	    	}
	    }
		
		for (GeocoderResult r : resCand) {
			switch (r.getType())
			{
			case CALLE:
			case CALLEyPORTAL:
			{
				
				if (en.getPortal() >= 0) 
				{
					List<GeocoderResult> resCallePortal = geocodeRepository.findCalleYportal2(r.getIdCalle() + "", r.getPortalNumber(), r.getLetra());
					res.addAll(resCallePortal);
				}
				else {
					List<GeocoderResult> resCalle = geocodeRepository.findCalleByIdCalle(r.getIdCalle() + "");	
					res.addAll(resCalle);
				}
			}
			break;
			case LOCALIDAD:
			{
				if (bUseLocalidad)
				{
					List<GeocoderResult> resLocalidad = geocodeRepository.findLocalidad(r.getLocalidad(), r.getDepartamento());	
					res.addAll(resLocalidad);
				}
			}
			break;
			case MANZANAySOLAR:
			case RUTAyKM:
			case ESQUINA:
			case POI:
				res.add(r);
				break;
			default: continue;
			}
			
		}
		
//	    for (GeocoderResult r : res) {
//	    	double score = fuzzyScore.fuzzyScore(r.getAddress(), en.getEntrada()) / 10.0;
//	    	if (r.getType() == TipoDirec.POI)
//	    	{
//	    		score = score + 5;
//	    	}		    	
//	    	if (r.getType() == TipoDirec.ESQUINA)
//	    	{
//	    		score = score + 10;
//	    	}
//	    	if (r.getType() == TipoDirec.CALLEyPORTAL)
//	    	{
//	    		score = score + 15;
//	    	}
//
//	    	r.setRanking(score);
//	    }
//	    res.sort(null);
		
//		for (GeocoderResult r : res) {
//			double score = r.getRanking();
//			if (r.getState() == 1) {
//				score = score + 0.0000001;
//			}
//
//			r.setRanking(score);
//		}
//		res.sort(null);


	    // Devuelve solo el primero si es un número de portal (coicidencia exacta, se supone, aunque yo creo que se deberían enviar el resto de resulados).
//		if (res.size() > 0) {
//			if (res.get(0).getType() == TipoDirec.CALLEyPORTAL) {
//				res = res.subList(0, 1);
//			}
//		}
		List<GeocoderResultV0> resV0 = convertV0(res, en);
		
    	t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<GeocoderResultV0>>(resV0, HttpStatus.OK);
    }
	
	@PostMapping(value = "/BusquedaDireccion", produces = "application/json")
    @ApiOperation(value="Petición POST: El servicio de búsqueda de direcciones devuelve el conjunto de direcciones que\r\n" + 
    		"cumplen los criterios de búsqueda. Estas direcciones están compuestas por el\r\n" + 
    		"nombre de la calle, el número de puerta, manzana, solar, localidad, departamento,\r\n" + 
    		"código postal, ubicación geográfica (punto en SRID 4326), entre otros.",
    		notes = "Los parámetros que recibe son:\r\n" + 
    				"•departamento (opcional): Nombre del departamento\r\n" + 
    				"•localidad (opcional): Nombre de la localidad\r\n" + 
    				"•direccion: Dirección completa. Los formatos en los que se pueden buscar direcciones son (los campos entre “[]” son opcionales):\r\n" + 
    				"•calle [número] [, localidad] [, departamento]\r\n" + 
    				"•calle [número] esquina calle2 [, localidad] [, departamento] (“esquina” también se puede escribir como “esq.” o “esq”)\r\n" + 
    				"•[calle] manzana X solar Y [, localidad] [, departamento] (manzana también se puede escribir como man. o m. y solar como sol. o s.)\r\n" + 
    				"•nombre de inmueble [, localidad] [, departamento]",
    		tags = {"Geocode"})
	
    public ResponseEntity<List<GeocoderResultV0>> postBusquedaDireccion(
    		@RequestParam(value="calle", required = true) String nomvia,
    		@RequestParam(value="localidad", required = false) String localidad,
    		@RequestParam(value="departamento", required = false) String departamento
    		) {
		return getBusquedaDireccion(nomvia, localidad, departamento);
	}
	
	@Override
	@GetMapping(value = "/SugerenciaCalleCompleta")
    @ApiOperation(value="El servicio de sugerencia de calles devuelve el conjunto de calles que cumple con\n" + 
    		"el criterio de búsqueda (parámetro entrada), con el formato “calle, localidad,\n" + 
    		"departamento” y ordenadas según la cantidad de direcciones asociadas a la calle\n" + 
    		"de forma descendente. Este servicio es utilizado usualmente para el \n" + 
    		"autocompletado de campos de dirección.\n" +
	 "entrada : texto libre que representa una dirección, puede ser parte de una calle " + 
	 "incluso con palabras incompletas (ej: “Buenos Air”) y puede contener el número de puerta. ", tags = {"Geocode"})
    public ResponseEntity<List<SugerenciaResultV0>> getSugerenciaCalleCompleta(
    		@RequestParam(value="entrada", required = true) String entrada,
    		@RequestParam(value="todos", required = false) boolean todos
    		) {
    	if (!geocodeRepository.isMethodAllowed("v0", "SugerenciaCalleCompleta"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();
    	
    	EntradaNormalizada entradaNorm = GeocodingUtils.parseAddress(entrada);
    	int portal = entradaNorm.getPortal();
    	List<GeocoderResult> res = null;
    	String nomvia, localidad, departamento;
    	nomvia = entradaNorm.getNomVia();
    	localidad = entradaNorm.getLocalidad();
    	departamento = entradaNorm.getDepartamento();
    	String qAux = nomvia;
    	if (null != localidad)
    		qAux = qAux + ", " + localidad;
    	if (null != departamento)
    		qAux = qAux + ", " + departamento;

		if (portal == -1)
			// res = geocodeRepository.findOldCalle(nomvia, localidad, departamento);
			res = geocodeRepository.findCandidates(qAux, 20, false);
		else
			res = geocodeRepository.findCalleYportal(nomvia, portal, localidad, departamento); // TODO: Fuzzy
		
		List<SugerenciaResultV0> resV0 = new ArrayList<SugerenciaResultV0>(); 
		for (GeocoderResult r : res) {
			if (r.getType() != TipoDirec.LOCALIDAD)
			{
				resV0.add(convertToSugerenciaV0(r));
			}
		}
							
		
    	t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<SugerenciaResultV0>>(resV0, HttpStatus.OK);
    }

	@PostMapping(value = "/SugerenciaCalleCompleta")
    @ApiOperation(value="Petición POST: El servicio de sugerencia de calles devuelve el conjunto de calles que cumple con\n" + 
    		"el criterio de búsqueda (parámetro entrada), con el formato “calle, localidad,\n" + 
    		"departamento” y ordenadas según la cantidad de direcciones asociadas a la calle\n" + 
    		"de forma descendente. Este servicio es utilizado usualmente para el \n" + 
    		"autocompletado de campos de dirección.\n" +
	 "entrada : texto libre que representa una dirección, puede ser parte de una calle " + 
	 "incluso con palabras incompletas (ej: “Buenos Air”) y puede contener el número de puerta. ", tags = {"Geocode"})	
    public ResponseEntity<List<SugerenciaResultV0>> postSugerenciaCalleCompleta(
    		@RequestParam(value="entrada", required = true) String entrada,
    		@RequestParam(value="todos", required = false) boolean todos
    		) {
		return getSugerenciaCalleCompleta(entrada, todos);
	}
	
	@Override
	@GetMapping(value = "/localidades")
    @ApiOperation(value="El servicio de búsqueda de localidades devuelve el conjunto de localidades de un \n" + 
    		"departamento. Cada localidad esta compuesta por el identificador, nombre, código \n" + 
    		"postal y alias.",
	  tags = {"Geocode, Localidad"})
    public ResponseEntity<List<LocalidadResultV0>> getLocalidades(
    		@RequestParam(value="departamento", required = true) String departamento,
    		@RequestParam(value="alias", required = false) boolean alias
    		) {
    	if (!geocodeRepository.isMethodAllowed("v0", "localidades"))
    	{
    		throw new ResponseStatusException(
 		           HttpStatus.NOT_ACCEPTABLE, "Método no autorizado en la variable de entorno OPENSHIFT_CALLEJEROUY_APIS_ALLOWED.");	    		
    	}

    	long t = System.currentTimeMillis();
    	
    	List<LocalidadResultV0> res;
    	
		res = geocodeRepository.findLocalidad(departamento, alias);	
		
		
    	t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs. Registros: " + res.size());    	
        return new ResponseEntity<List<LocalidadResultV0>>(res, HttpStatus.OK);
    }
	@PostMapping(value = "/localidades")
    @ApiOperation(value="Petición POST: El servicio de búsqueda de localidades devuelve el conjunto de localidades de un \n" + 
    		"departamento. Cada localidad esta compuesta por el identificador, nombre, código \n" + 
    		"postal y alias.",
	  tags = {"Geocode, Localidad"})
	
    public ResponseEntity<List<LocalidadResultV0>> postLocalidades(
    		@RequestParam(value="departamento", required = true) String departamento,
    		@RequestParam(value="alias", required = false) boolean alias
    		) {
		return getLocalidades(departamento, alias);
	}
    
    @Override
	@GetMapping("/reverse")
    @ApiOperation(value="Geocodificación Inversa. Latitud y longitud en EPSG:4326")
    public List<GeocoderResult> getReverseGeocoding(
    		@RequestParam(value = "latitud", required = true) double lat,
    		@RequestParam(value = "longitud", required = true) double lng,
    		@RequestParam(value = "limit", defaultValue = "10") int limit
    		)
    {
    	if (!geocodeRepository.isMethodAllowed("v0", "reverse"))
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
    	t = System.currentTimeMillis() - t;
    	logger.info("Time " + t + " msecs");    	    	
        return res;
    }

	@PostMapping("/reverse")
	@ApiOperation(value="Petición POST: Geocodificación Inversa. Latitud y longitud en EPSG:4326")	
    public List<GeocoderResult> postReverseGeocoding(
    		@RequestParam(value = "latitud", required = true) double lat,
    		@RequestParam(value = "longitud", required = true) double lng,
    		@RequestParam(value = "limit", defaultValue = "10") int limit
    		)
    {
		return getReverseGeocoding(lat, lng, limit);
    }	
//    @GetMapping("/all")
//    public List<GeocoderResult> getGeocoding(@RequestParam(value = "q", required = true)
//    		String query, @PageableDefault(sort = {"localidad", "nombre"},
//    		page = 0, size = 10) Pageable pageable) {
//    	if ((pageable.getPageSize() > 200) || (pageable.getOffset() > 100))
//		{
//    		throw new ResponseStatusException(
//    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
//		}
//        return geocodeRepository.findAll(query, pageable.getSort());
//    }
//
//    @GetMapping(value = "/paged")
//    public Page<GeocoderResult> getPagedGeocoding(@RequestParam(value = "q", required = true) String query,
//    		@PageableDefault(sort = {"localidad", "nombre"}) Pageable pageable) {
//    	if ((pageable.getPageSize() > 200) || (pageable.getOffset() > 100))
//		{
//    		throw new ResponseStatusException(
//    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
//		}
//
//    	long t = System.currentTimeMillis();
//    	Page<GeocoderResult> res = geocodeRepository.findPage(query, pageable);
//    	t = System.currentTimeMillis() - t;
//    	logger.info("Time " + t + " msecs");    	
//        return res;
//    }
//
//    @GetMapping(value = "/sliced")
//    public Slice<GeocoderResult> getSlicedGeocoding(@RequestParam(value = "q", required = true) String query,
//    		@PageableDefault(sort = {"localidad", "nombre"}) Pageable pageable) {
//    	if ((pageable.getPageSize() > 200) || (pageable.getOffset() > 100))
//		{
//    		throw new ResponseStatusException(
//    		           HttpStatus.NOT_ACCEPTABLE, "Demasiados registros solicitados.");	
//		}
//
//        return geocodeRepository.findSlice(query, pageable);
//    }

	

}
