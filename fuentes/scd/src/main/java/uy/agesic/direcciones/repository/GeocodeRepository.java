
package uy.agesic.direcciones.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
//import org.apache.commons.text.similarity.FuzzyScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import uy.agesic.direcciones.data.AliasLocalidad;
import uy.agesic.direcciones.data.CapaPoligonalResult;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.Sinonimo;
import uy.agesic.direcciones.data.TipoVialidad;
import uy.agesic.direcciones.data.TramoResult;
import uy.agesic.direcciones.mapper.GeocodeRowMapperCalle;
import uy.agesic.direcciones.mapper.GeocodeRowMapperCapaPoligonal;
import uy.agesic.direcciones.mapper.GeocodeRowMapperEsquina;
import uy.agesic.direcciones.mapper.GeocodeRowMapperExactCalle;
import uy.agesic.direcciones.mapper.GeocodeRowMapperGeojson;
import uy.agesic.direcciones.mapper.GeocodeRowMapperLocalidad;
import uy.agesic.direcciones.mapper.GeocodeRowMapperLocalidadV0;
import uy.agesic.direcciones.mapper.GeocodeRowMapperManzanaYsolar;
import uy.agesic.direcciones.mapper.GeocodeRowMapperPoi;
import uy.agesic.direcciones.mapper.GeocodeRowMapperPortalPK;
import uy.agesic.direcciones.mapper.GeocodeRowMapperRuta;
import uy.agesic.direcciones.mapper.GeocodeRowMapperRutaYkm;
import uy.agesic.direcciones.mapper.GeocodeRowMapperTramo;
import uy.agesic.direcciones.mapper.SinonimosRowMapper;
import uy.agesic.direcciones.mapper.TipoVialidadRowMapper;
import uy.agesic.direcciones.utils.GeocodingUtils;

@Repository
@Slf4j
public class GeocodeRepository extends JdbcDaoSupport {
	
	public static <T> Predicate<T> distinctByIdCalle_Loc_Dep(Function<? super T, Object> ...keyExtractors) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();

	    return t -> {
	      final List<?> keys = Arrays.stream(keyExtractors)
	                  .map(ke -> ke.apply(t))
	                  .collect(Collectors.toList());

	      String joined = keys.stream()
	                         .map(Object::toString)
	                         .collect(Collectors.joining("_")); 
	      return seen.add(joined);
	    };
	}

	@Autowired
	public void setPrivateDatasource(DataSource datasource) {
		super.setDataSource(datasource);
	}

	@Autowired
	private GeocodeRowMapperPortalPK geocodeRowMapperPortalPK;
	@Autowired
	private GeocodeRowMapperCalle geocodeRowMapperCalle;
	@Autowired
	private GeocodeRowMapperExactCalle geocodeRowMapperExactCalle;
	@Autowired
	private GeocodeRowMapperLocalidad geocodeRowMapperLocalidad;
	@Autowired
	private GeocodeRowMapperRuta geocodeRowMapperRuta;
	@Autowired
	private GeocodeRowMapperRutaYkm geocodeRowMapperRutaYkm;
	@Autowired
	private GeocodeRowMapperPoi geocodeRowMapperPoi;
	@Autowired
	private GeocodeRowMapperManzanaYsolar geocodeRowMapperManzanaYsolar;
	@Autowired
	private GeocodeRowMapperLocalidadV0 geocodeRowLocalidadMapper;
	@Autowired
	private GeocodeRowMapperEsquina geocodeRowMapperEsquina;
	@Autowired
	private GeocodeRowMapperGeojson geocodeRowMapperGeojson;
	@Autowired
	private GeocodeRowMapperCapaPoligonal geocodeRowMapperCapaPoligonal;
	@Autowired
	private GeocodeRowMapperTramo geocodeRowMapperTramo;

	@Autowired
	private SinonimosRowMapper sinonimoRowMapper;
	@Autowired
	private TipoVialidadRowMapper tipoVialidadRowMapper;

	private HashMap<String, List<String>> sinonimos;
	private HashSet<String> tipo_vialidad;
	private HashSet<String> configMethodsAllowed = new HashSet<String>();

//    @Autowired
//    private ReverseGeocodeRowMapper reverseGeocodeRowMapper;

	@Override
	protected void initTemplateConfig() {

		// Un sinónimo funciona en los dos sentidos, así que tenemos que añadir
		// también al hash el reverso. El problema es que por ejemplo, GENERAL tiene
		// varios sinónimos (GRAL y GL). Esto nos obliga a usar un Map de String ->
		// Array
		reloadSinonimos();
	}

	public void reloadSinonimos() {
		logger.debug("Inicializando sinónimos...");
		List<Sinonimo> aux = getJdbcTemplate().query(SINONIMOS_SQL, new Object[] {}, sinonimoRowMapper);
		sinonimos = new HashMap<>();
		for (Sinonimo s : aux) {
			if (sinonimos.containsKey(s.getPalabra())) {
				List<String> auxA = sinonimos.get(s.getPalabra());
				auxA.add(s.getSinonimo());
			} else {
				ArrayList<String> auxA = new ArrayList<String>();
				auxA.add(s.getSinonimo());
				sinonimos.put(s.getPalabra(), auxA);
			}
			if (sinonimos.containsKey(s.getSinonimo())) {
				List<String> auxA = sinonimos.get(s.getSinonimo());
				auxA.add(s.getPalabra());
			} else {
				ArrayList<String> auxA = new ArrayList<String>();
				auxA.add(s.getPalabra());
				sinonimos.put(s.getSinonimo(), auxA);
			}

		}
		
//		getJdbcTemplate().execute("ANALYZE budu.mv_direcciones2");

		logger.debug("Inicializando tipo vialidad...");
		List<TipoVialidad> aux2 = getJdbcTemplate().query(TIPO_VIALIDAD_SQL, new Object[] {}, tipoVialidadRowMapper);
		tipo_vialidad = new HashSet<String>();
		for (TipoVialidad s : aux2) {
			if (!tipo_vialidad.contains(s.getNombre())) {
				tipo_vialidad.add(s.getNombre());
			}
		}

		String apisAllowed = System.getenv("OPENSHIFT_CALLEJEROUY_APIS_ALLOWED");
		if (apisAllowed != null) {
			String auxApis[] = apisAllowed.split(",");
			this.configMethodsAllowed.clear();
			this.configMethodsAllowed.addAll(Arrays.asList(auxApis));
		}
	}

	private static final String TIPO_VIALIDAD_SQL = "SELECT id, nombre " + "FROM budu.tipo_vialidad;";

	private static final String SINONIMOS_SQL = "SELECT s1.id, s1.palabra," + "s1.idsinonimo, s2.palabra as sinonimo\n"
			+ "FROM budu.sinonimo as s1 inner join budu.sinonimo as s2\n" + " on s1.idsinonimo = s2.id;";

	private static final String GEOCODE_SQL = "SELECT \n" + "  id,\n" + "  idcalle,\n" + "  nombre,\n" + " numero, letra,\n"
			+ "  localidad,\n" + "  cp,\n" + "  depa,\n" + "  ppadron,\n" + "  solar,\n" + "  manzana,\n" + "  km,\n"
			+ "  '' as nombre_inmueble,\n" + "  longitud,\n" + "  latitud\n" + "FROM budu.mv_puntos_direccion\n"
			+ "WHERE fulltext @@ to_tsquery('spanish', unaccent(?))";

	private static final String GEOCODE_SQL_LOCALIDAD_DEPRECATED = "SELECT \n"
			+ " DISTINCT l.apis_id as id,l.nombre as localidad, l.cod_postal, a.nombre as alias, depto as departamento, l.nombre, a.nombre as buscarAlias \n"
			+ " FROM budu.localidades_no_oficiales l \n"
			+ " LEFT JOIN budu.alias_localidad_geo a on a.localidad_id = l.apis_id  \n"
			+ " LEFT JOIN budu.tipo_alias ta on a.tipo_alias_id = ta.id \n" + " WHERE ( a.nombre % unaccent(?)) \n"
			+ " OR \n" + " ( l.nombre % unaccent(?)) ";

	private static final String GEOCODE_SQL_LOCALIDAD = "select distinct "
			+ "l.apis_id as id, l.nombre as localidad, l.cod_postal as cp, a.nombre as alias, \n"
			+ " d.nombre as departamento, l.nombre, a.nombre as buscarAlias, \n"
			+ " i.idloc as idlocalidad, i.iddepto as iddepartamento, \n"
			+ " ST_X(ST_Transform(ST_PointOnSurface(l.multipolygon), 4326)) as longitud, \n"
			+ " ST_Y(ST_Transform(ST_PointOnSurface(l.multipolygon), 4326)) as latitud, \n"
			+ " similarity(l.nombre, ? ) as ranking \n"
			+ " from budu.localidades_no_oficiales l left join budu.intersec_localidad_departamento i \n"
			+ " 	on l.apis_id = i.idloc \n"
			+ " LEFT JOIN budu.alias_localidad_geo a on a.localidad_id = l.apis_id  \n"
			+ "    left join budu.departamento d on i.iddepto = d.id \n" + " where ( a.nombre % unaccent(?) \n"
			+ " OR  l.nombre % unaccent(?)) \n";

	private static final String GEOCODE_SQL_EXACT_ADDRESS = "SELECT \n" + "  id,\n" + "  idcalle, \n" + "  nombre,\n"
			+ " numero, letra, \n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n" + "  idLocalidad, \n"
			+ "  idDepartamento, \n" + "  km,\n" +
            " '' as nombre_inmueble, \n" +
			"  longitud,\n" + "  latitud, \n" + "  0 as ranking \n" + "FROM budu.mv_puntos_direccion\n"
			+ "WHERE depa like ?\n" + "AND localidad like ?\n" + "AND nombre like ?\n" + "ORDER BY numero";
	private static final String GEOCODE_SQL_EXACT_ADDRESS_SIN_LOCALIDAD = "SELECT \n" + "  id,\n" + "  nombre,\n"
			+ " numero, letra,\n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n" + "  idLocalidad, \n"
			+ "  idDepartamento, \n" + "  km,\n" +
			" '' as nombre_inmueble, \n" +        
			"  longitud,\n" + "  latitud\n" + "FROM budu.mv_puntos_direccion\n" + "WHERE depa like ?\n"
			+ "AND nombre like ?\n" + "ORDER BY numero LIMIT 30";

	private static final String GEOCODE_SQL_EXACT_CALLE_WITH_NOMVIA_LOCALIDAD = "SELECT \n"
			+ "c.idcalle, c.nombre, c.localidad, c.departamento, l.cod_postal as cp, l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ " ST_X(ST_Transform(ST_PointOnSurface(ST_LineMerge(c.geom)), 4326)) as longitud, \n"
			+ " ST_Y(ST_Transform(ST_PointOnSurface(ST_LineMerge(c.geom)), 4326)) as latitud \n"
			+ " from budu.calle_con_geom c \n" + "     join budu.calle_localidad cl on (c.idcalle = cl.idcalle) \n"
			+ "     join budu.localidades_no_oficiales l on (l.apis_id = cl.idlocalidad) \n"
			+ "     join budu.calle_departamento cd on (c.idcalle = cd.idcalle) \n"
			+ "     join budu.departamento d on (cd.iddepartamento = d.id) \n" 
			+ "WHERE departamento like ?\n" + "AND localidad like ?\n" + "AND c.nombre like ?\n";

	
	private static final String GEOCODE_SQL_EXACT_ADDRESS2 = "SELECT \n" + "  id,\n" + "  idcalle,\n" + "  nombre,\n"
			+ " numero, letra, \n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n" + "  idLocalidad, \n"
			+ "  idDepartamento, \n" + "  km,\n" + "  longitud,\n" + "  latitud, \n" + " '' as nombre_inmueble, \n"
			+ "  0 as ranking \n" + "FROM budu.mv_puntos_direccion\n" + "WHERE idcalle = ?\n" + "ORDER BY numero";

	//
	private static final String GEOCODE_SQL_EXACT_CALLE_WITH_IDCALLE = "SELECT DISTINCT\n"
			+ "c.idcalle, c.nombre, c.localidad, c.departamento, l.cod_postal as cp, l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ " ST_X(ST_Transform(ST_PointOnSurface(ST_Union(c.geom)), 4326)) as longitud, \n"
			+ " ST_Y(ST_Transform(ST_PointOnSurface(ST_Union(c.geom)), 4326)) as latitud \n"
			+ " from budu.calle_con_geom c \n" + "     join budu.calle_localidad cl on (c.idcalle = cl.idcalle) \n"
			+ "     join budu.localidades_no_oficiales l on (l.apis_id = cl.idlocalidad) \n"
			+ "     join budu.calle_departamento cd on (c.idcalle = cd.idcalle) \n"
			+ "     join budu.departamento d on (cd.iddepartamento = d.id) \n"
			+ " group by c.idcalle, c.nombre, c.localidad, c.departamento, l.cod_postal, l.apis_id, d.id \n" 
			+ " having c.idcalle = ?; ";

	private static final String GEOCODE_SQL_EXACT_ADDRESS2_WITH_PORTAL = "SELECT \n" + "  id,\n" + "  idcalle,\n"
			+ "  nombre,\n" + " numero, letra,\n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n"
			+ "  idLocalidad, \n" + "  idDepartamento, \n" + "  km,\n" + " '' as nombre_inmueble, \n" + "  longitud,\n"
			+ "  latitud, \n" + "  0 as ranking \n" + "FROM budu.mv_puntos_direccion p \n" + "WHERE idcalle = ?\n";

	private static final String GEOCODE_SQL_EXACT_LOCALIDAD = "select distinct "
			+ " l.apis_id as id, l.nombre as localidad, l.cod_postal as cp, a.nombre as alias, \n"
			+ " d.nombre as departamento, l.nombre, a.nombre as buscarAlias, \n"
			+ " i.idloc as idlocalidad, i.iddepto as iddepartamento, \n"
			+ " ST_X(ST_Transform(ST_PointOnSurface(l.multipolygon), 4326)) as longitud, \n"
			+ " ST_Y(ST_Transform(ST_PointOnSurface(l.multipolygon), 4326)) as latitud \n"
			+ " from budu.localidades_no_oficiales l left join budu.intersec_localidad_departamento i \n"
			+ " 	on l.apis_id = i.idloc \n"
			+ " LEFT JOIN budu.alias_localidad_geo a on a.localidad_id = l.apis_id  \n"
			+ "    left join budu.departamento d on i.iddepto = d.id \n" + " where ( d.nombre like ? \n"
			+ " and  l.nombre like ?)";

//    		"SELECT \n" +
//    		"  id,\n" +
//    		"  idcalle, \n" +
//            "  nombre,\n" +
//            " numero, letra\n" +
//            "  localidad,\n" +
//            "  cp,\n" +
//            "  depa as departamento,\n" +
//    		"  idLocalidad, \n" +
//            "  idDepartamento, \n" +            
//            "  km,\n" +
////            "  nombre_inmueble,\n" +            
//            "  latitud,\n" +
//            "  longitud, \n" +
//            "  0 as ranking \n" +
//            "FROM budu.mv_puntos_direccion\n" +
//            "WHERE depa like ?\n" +
//            "AND localidad like ?\n" +
//            "LIMIT 3"
//            ;

	private static final String GEOCODE_SQL_EXACT_WITH_PORTAL_NUMBER = "SELECT \n" + "  id,\n" + "  idcalle, \n"
			+ "  nombre,\n" + " numero, letra, \n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n"
			+ "  idLocalidad, \n" + "  idDepartamento, \n" + "  km,\n" + " '' as nombre_inmueble, \n" + "  longitud,\n"
			+ "  latitud, \n" + "  0 as ranking \n" + "FROM budu.mv_puntos_direccion\n" + "WHERE depa like ?\n"
			+ "AND localidad like ?\n" + "AND nombre like ?\n" + "AND numero = ?\n";
	private static final String GEOCODE_SQL_EXACT_WITH_PORTAL_NUMBER_SIN_LOCALIDAD = "SELECT \n" + "  id,\n"
			+ "  nombre,\n" + " numero, letra, \n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n"
			+ "  idLocalidad, \n" + "  idDepartamento, \n" + "  km,\n" + " '' as nombre_inmueble, \n" + "  longitud,\n"
			+ "  latitud \n" + "FROM budu.mv_puntos_direccion\n" + "WHERE depa like ?\n" + "AND nombre like ?\n"
			+ "AND numero = ?\n";

	private static final String GEOCODE_SQL_SOLAR = "SELECT DISTINCT \n"
			+ "solar_id, p.idcalle as idcalle, '' as nombre, p.padron as padron, s.manzana as manzana, s.solar as solar, p.cp, st_transform(p.punto, 4326) as p4326, d.nombre as departamento, p.id, l.nombre as localidad, \n"
			+ " l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ "st_y(st_transform(p.punto, 4326)) AS latitud, st_x(st_transform(p.punto, 4326)) AS longitud "
			+ " from budu.solarp p join budu.solar s on p.solar_id = s.apis_id join budu.manzana m on s.manzana_id = m.gid \n"
			+ "  left join budu.localidades_no_oficiales l on ST_Contains(l.multipolygon, p.punto) \n"
			+ "  left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ " where s.solar = ? \n" + // String
			" and m.manzana = ? \n" + // String
			" and d.nombre % ? \n " + " and l.nombre % ?  LIMIT ?";

	private static final String GEOCODE_SQL_PADRON = "SELECT DISTINCT \n"
			+ "solar_id, p.idcalle as idcalle, '' as nombre, p.padron as padron, s.manzana as manzana, s.solar as solar, p.cp, st_transform(p.punto, 4326) as p4326, d.nombre as departamento, p.id, l.nombre as localidad, \n"
			+ " l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ "st_y(st_transform(p.punto, 4326)) AS latitud, st_x(st_transform(p.punto, 4326)) AS longitud, \n" 
			+ "similarity (l.nombre, ? ) as ranking \n"
			+ " from budu.solarp p join budu.solar s on p.solar_id = s.apis_id join budu.manzana m on s.manzana_id = m.gid \n"
			+ "  left join budu.localidades_no_oficiales l on ST_Contains(l.multipolygon, p.punto) \n"
			+ "  left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ " where p.padron = ? \n" + " and d.nombre % ? \n " + " and l.nombre % ? order by ranking desc ";

	// nombre like 'RUTA 5%'
	private static final String GEOCODE_SQL_RUTA = "SELECT DISTINCT id, nombre, ruta_id \n" + "	FROM budu.calle \n"
			+ "	WHERE \n" + "		nombre like ? \n" + "		and ruta_id is not null  LIMIT ? ";

	// Parámetros: idcalle, km, idcalle, km, km, km, idruta
	// DEVUELVE fraccion, idcalle, nombreruta, p4326, localidad, departamento
	private static final String GEOCODE_SQL_RUTA_KM = "with \n" + "	cota_inf as\r\n"
			+ "       (select * from budu.portal_pk where idcalle = ? and km <= ? order by km desc limit 1),\n"
			+ "    cota_sup as\r\n"
			+ "       (select * from budu.portal_pk where idcalle= ? and km > ? order by km asc limit 1),\n"
			+ "	sol as \n" + "	(select  st_linemerge(tr.geom) as tramo, \n"
			+ "         (case when ST_Line_Locate_Point(st_linemerge(tr.geom), ci.punto) < ST_Line_Locate_Point(st_linemerge(tr.geom), cs.punto) then \n"
			+ "           	ST_Line_Locate_Point(st_linemerge(tr.geom), ci.punto) +(( ? - ci.km ) / (cs.km - ci.km)) * (ST_Line_Locate_Point(st_linemerge(tr.geom), cs.punto) - ST_Line_Locate_Point(st_linemerge(tr.geom), ci.punto)) \n"
			+ "         else \n"
			+ "            ST_Line_Locate_Point(st_linemerge(tr.geom), cs.punto) + (1- (( ? - ci.km ) / (cs.km - ci.km))) * (ST_Line_Locate_Point(st_linemerge(tr.geom), ci.punto) - ST_Line_Locate_Point(st_linemerge(tr.geom), cs.punto)) \n"
			+ "         end ) as fraccion, \n" + "	 	 tr.id_ruta, tr.nombre as nombreRuta, tr.id as id \n"
			+ "             from cota_inf ci, cota_sup cs, budu.tramo_ruta tr where tr.id_ruta = ? and st_geometrytype(st_linemerge(tr.geom)) = 'ST_LineString'), \n"
			+ "                mojon_interpolado as (select ST_Line_Interpolate_Point(tramo, fraccion) as geom, fraccion, id_ruta, id, nombreRuta from sol) \n"
			+ "                 select distinct fraccion, m.id as idcalle, \n"
			+ "				  id_ruta, nombreRuta, st_x(st_transform(m.geom, 4326)) as longitud,"
			+ "	st_y(st_transform(m.geom, 4326)) as latitud, d.id as idDepartamento, d.nombre as departamento, ll.apis_id as idLocalidad, ll.nombre as localidad \n"
			+ "                 from mojon_interpolado m \n"
			+ "                 left join budu.localidades_no_oficiales ll on st_intersects(ll.multipolygon, m.geom) \n"
			+ "                 left join budu.alias_localidad_geo al on al.localidad_id = ll.apis_id \n"
			+ "                 left join budu.departamento d on st_intersects(m.geom, d.geom)";

	// TOD: USAR LA TABLA solarp
	private static final String GEOCODE_SQL_EXACT_SOLAR = "SELECT \n"
			+ "solar_id, p.idcalle as idcalle, '' as nombre, p.padron as padron, s.manzana as manzana, s.solar as solar, p.cp, st_transform(p.punto, 4326) as p4326, d.nombre as departamento, p.id, l.nombre as localidad, \n"
			+ " l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ "st_y(st_transform(p.punto, 4326)) AS latitud, st_x(st_transform(p.punto, 4326)) AS longitud "
			+ "FROM budu.solarp p \n"
			+ " INNER JOIN budu.solar s on p.solar_id = s.apis_id join budu.manzana m on s.manzana_id = m.gid \n"
			+ "  left join budu.localidades_no_oficiales l on ST_Contains(l.multipolygon, p.punto) \n"
			+ "  left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ "WHERE d.nombre like ?\n" + "AND l.nombre like ?\n" + "AND m.manzana = ?\n" + "AND s.solar = ?\n";

	private static final String GEOCODE_SQL_INMUEBLE = "SELECT distinct p.id, p.idcalle, p.cp, c.nombre, p.numero, p.cp, p.letra, p.km, p.nombre_inmueble,"
			+ " l.nombre as localidad, l.depto as departamento, l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ "	st_y(st_transform(p.punto, 4326)) AS latitud, st_x(st_transform(p.punto, 4326)) AS longitud \n"
			+ "   FROM budu.punto_notable p \n" + "   	inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ "       left join budu.localidades_no_oficiales l on ST_Contains(multipolygon, p.punto) \n"
			+ "          left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ "	WHERE p.nombre_inmueble % ?\n";

	private static final String GEOCODE_SQL_EXACT_INMUEBLE = "SELECT distinct p.id, p.idcalle, p.cp, p.nombre_inmueble, c.nombre, p.numero, p.cp, p.letra, p.km,"
			+ " l.nombre as localidad, l.depto as departamento, l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ "	st_y(st_transform(p.punto, 4326)) AS latitud, st_x(st_transform(p.punto, 4326)) AS longitud "
			+ "   FROM budu.punto_notable p \n" + "   	inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ "       left join budu.localidades_no_oficiales l on ST_Contains(multipolygon, p.punto) \n"
			+ "          left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ "	WHERE departamento like ? and localidad like ? and p.nombre_inmueble like ?\n";

	private static final String GEOCODE_BASE_SQL = "SELECT DISTINCT \n" + "  c.idcalle,\n" + "  c.nombre,\n"
			+ "  c.idLocalidad, \n" + "  c.localidad,\n" + "  c.departamento,\n" + "  c.idDepartamento, \n"
			+ "  '' as nombre_inmueble, \n" +
//            "  c.cp, \n" +             
			" [SIMILARITY_RANKING] \n" +
            // "  ts_rank(c.fulltext, plainto_tsquery('spanish', '[NOMBRE]')) as ranking \n" +
			"FROM budu.mv_direcciones2 c \n" +
			 "  left join budu.alias_departamento a on c.iddepartamento = a.departamento_id \n" +
			 "  left join budu.alias_localidad_geo l on c.idlocalidad = l.localidad_id \n" +

			// "WHERE fulltext @@ plainto_tsquery('spanish', unaccent(?))\n" +
			" WHERE [WHERE_CLAUSE] \n" + "ORDER BY ranking DESC LIMIT 100 \n";

	private static final String GEOCODE_LOCALIDAD_POR_DEPARTAMENTO_SQL = "SELECT DISTINCT \n"
			+ "l.apis_id as id, l.nombre, l.depto, l.cod_postal as codigoPostal \n"
			+ " from budu.localidades_no_oficiales l left join budu.intersec_localidad_departamento i \n"
			+ " on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ " where trim(upper(l.depto))=trim(upper(?)) \n" + " ORDER BY nombre;";

	private static final String GEOCODE_SQL_DIRECCIONES_EN_POLIGONO = "SELECT \n"
			+ "NOMBRE_ID, nombre, ST_AsGeoJSON(ST_Transform(NOMBRE_GEOM, 4326), 9, 8) as geojson  \n"
			+ " from budu.NOMBRE_CAPA \n" + " WHERE NOMBRE_ID = ?;";

	private static final String GEOCODE_SQL_PORTALES_DENTRO_POLIGONO = "SELECT \n" + "  id,\n" + "  idcalle, \n"
			+ "  nombre,\n" + " numero, letra, \n" + "  localidad,\n" + "  cp,\n" + "  depa as departamento,\n"
			+ "  idLocalidad, \n" + "  idDepartamento, \n" + "  km,\n" + " '' as nombre_inmueble, \n" + "  longitud,\n"
			+ "  latitud, \n" + "  0 as ranking \n" + "FROM budu.mv_puntos_direccion\n"
			+ "WHERE ST_Within(p4326, ST_SetSRID(ST_GeomfromGeoJson(?), 4326)) ";

	private static final String GEOCODE_SQL_SOLARES_DENTRO_POLIGONO = "SELECT DISTINCT \n"
			+ "p.id, p.idcalle, p.cp, p.nombre_inmueble, c.nombre, c.localidad, c.departamento \n"
			+ " FROM budu.solarp p \n" + " inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ " WHERE ST_Within(ST_Transform(punto, 4326), ST_SetSRID(ST_GeomfromGeoJson(?), 4326)) ";

	private static final String GEOCODE_SQL_POIS_DENTRO_POLIGONO = "SELECT DISTINCT \n"
			+ "p.id, p.idcalle, p.cp, p.nombre_inmueble, c.nombre, c.localidad, c.departamento, p.cp as codPostal,\n"
			+ " st_x(st_transform(punto, 4326)) as longitud, st_y(st_transform(punto, 4326)) as latitud \n"  
			+ "FROM budu.punto_notable p \n" + "inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ " WHERE ST_Within(ST_Transform(punto, 4326), ST_SetSRID(ST_GeomfromGeoJson(?), 4326)) ";

	private static final String GEOCODE_SQL_PUNTO_NOTABLE = "SELECT distinct p.id, p.idcalle, p.cp, c.nombre, p.numero, p.letra, p.km, p.nombre_inmueble,\n"
			+ " l.nombre as localidad, l.depto as departamento, l.apis_id as idLocalidad, d.id as idDepartamento, \n"
			+ " st_x(st_transform(punto, 4326)) as longitud, st_y(st_transform(punto, 4326)) as latitud"
			+ "   FROM budu.punto_notable p \n" + "   	inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ "       left join budu.localidades_no_oficiales l on ST_Contains(multipolygon, p.punto) \n"
			+ "          left join budu.intersec_localidad_departamento i \n"
			+ "    	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n"
			+ " WHERE p.nombre_inmueble % ? and c.departamento % ? \n" + " LIMIT ? ";

	private static final String FUZZY_GEOCODE_SQL = "SELECT distinct \n"+
			"  idcalle, nombre, idLocalidad, localidad, departamento," +
			"  idDepartamento, '' as nombre_inmueble, \n" +
			"  ts_rank(fulltext, plainto_tsquery(?)) as ranking \n" +
			" FROM budu.mv_direcciones2 \n" +
			" WHERE plainto_tsquery('spanish', unaccent(?)) @@ fulltext \n" +
			" ORDER BY ranking DESC \n" + "LIMIT ?";

	// TODO: HACER OTRO PARA punto_notable Y solarp
	private static final String GEOCODE_CLOSEST_POINTS_SQL = "SELECT id, idcalle, latitud, longitud,"
			+ " p4326, nombre, numero,letra, idlocalidad, localidad, iddepartamento, depa as departamento, "
			+ " km, cp, fulltext, " + " '' as nombre_inmueble, \n" + " 0 as ranking \n"
			+ "FROM budu.mv_puntos_direccion\n";

	private static final String GEOCODE_CLOSEST_POIS_SQL = "SELECT DISTINCT * FROM (SELECT p.id, p.idcalle, p.cp, c.nombre, p.numero, p.letra, p.km, p.nombre_inmueble, \n"
			+ "l.nombre as localidad, l.depto as departamento, l.apis_id as idLocalidad, d.id as idDepartamento, ST_X(ST_TRANSFORM(p.punto, 4326)) as longitud, ST_Y(ST_TRANSFORM(p.punto, 4326)) as latitud \n"
			+ "   FROM budu.punto_notable p \n" + "	inner join budu.mv_direcciones2 c on c.idcalle = p.idcalle \n"
			+ "      left join budu.localidades_no_oficiales l on ST_Contains(multipolygon, p.punto) \n"
			+ "          left join budu.intersec_localidad_departamento i \n"
			+ "   	on l.apis_id = i.idloc left join budu.departamento d on i.iddepto = d.id \n";

	// TODO: USAR tramos, O LA VISTA CON LA CALLE Y GEOMETRIA
	private static final String GEOCODE_CORNERS_OF_STREET_SQL = ""
			+ "SELECT DISTINCT c.idcalle, c.nombre as nombre, n.nombre as nombreEsq,"
			+ "n.idcalle as idcalleEsq, c.localidad, c.departamento, \n" + "	CASE \n"
			+ "   WHEN not ST_CoveredBy(c.geom, n.geom) \n" + "   THEN \n" + "      ST_Intersection(c.geom,n.geom) \n"
			+ "     END AS geomIntersec,  \n" + "   st_x(p4326) as longitud, st_y(p4326) as latitud, "
			+ "   0 as ranking \n" + " FROM budu.calle_con_geom AS c \n" + "   INNER JOIN budu.calle_con_geom AS n \n"
			+ "    ON ST_Intersects(c.geom, n.geom) \n"
			+ "    LEFT JOIN budu.mv_direcciones2 as mv on n.idcalle = mv.idcalle \n"
			+ "   , LATERAL (select st_transform(ST_Intersection(c.geom,n.geom), 4326) as p4326 ) l \n"
			+ " WHERE c.idcalle = ? and n.idcalle != ? " + " and ST_GeometryType(p4326) like 'ST_Point' LIMIT 100 \n";

	private static final String GEOCODE_CORNERS_OF_STREET_FILTERED_SQL = ""
			+ "SELECT DISTINCT c.idcalle, c.nombre as nombre, n.nombre as nombreEsq,"
			+ "n.idcalle as idcalleEsq, c.localidad, c.departamento, \n" + "	CASE \n"
			+ "   WHEN not ST_CoveredBy(c.geom, n.geom) \n" + "   THEN \n" + "      ST_Intersection(c.geom,n.geom) \n"
			+ "     END AS geomIntersec,  \n" + "   st_x(p4326) as longitud, st_y(p4326) as latitud, "
			+ "   0 as ranking \n" + " FROM budu.calle_con_geom AS c \n" + "   INNER JOIN budu.calle_con_geom AS n \n"
			+ "    ON ST_Intersects(c.geom, n.geom) \n"
			+ "    LEFT JOIN budu.mv_direcciones2 as mv on n.idcalle = mv.idcalle \n"
			+ "   , LATERAL (select st_transform(ST_Intersection(c.geom,n.geom), 4326) as p4326 ) l \n"
			+ " WHERE c.idcalle = ? AND (n.nombre ilike ? or n.nombre % ?) and ST_GeometryType(p4326) like 'ST_Point'  LIMIT 100 \n";

	private static final String GEOCODE_CORNERS_OF_STREET_EXACT_SQL = ""
			+ "SELECT DISTINCT c.idcalle, c.nombre as nombre, n.nombre as nombreEsq,"
			+ "n.idcalle as idcalleEsq, c.localidad, c.departamento, \n" + "	CASE \n"
			+ "   WHEN not ST_CoveredBy(c.geom, n.geom) \n" + "   THEN \n" + "      ST_Intersection(c.geom,n.geom) \n"
			+ "     END AS geomIntersec,  \n" + "   st_x(p4326) as longitud, st_y(p4326) as latitud, "
			+ "   0 as ranking \n" + " FROM budu.calle_con_geom AS c \n" + "   INNER JOIN budu.calle_con_geom AS n \n"
			+ "    ON ST_Intersects(c.geom, n.geom) \n"
			+ "    LEFT JOIN budu.mv_direcciones2 as mv on n.idcalle = mv.idcalle \n"
			+ "   , LATERAL (select st_transform(ST_Intersection(c.geom,n.geom), 4326) as p4326 ) l \n"
			+ " WHERE c.idcalle = ? AND n.idcalle = ?  and ST_GeometryType(p4326) like 'ST_Point' LIMIT 10 \n";

	private static final String GEOCODE_SQL_TRAMOS = "SELECT \n"
			+ "gid, idcalle, ST_AsGeoJSON(ST_Transform(geom, 4326), 9, 8) as geojson, fuente_id, tipo_vialidad_id  \n"
			+ " from budu.tramo \n" + " WHERE idcalle = ?;";

	
	private static final String SQL_CAPAS_POLIGONALES = "SELECT * FROM budu.capas_poligonales;";

	private static final String COUNT_SQL = "SELECT COUNT(*) AS count FROM (%s) counter";

//	private FuzzyScore fuzzyScore = new FuzzyScore(Locale.forLanguageTag("es-ES"));

	// -----GEOCODING-----
	public List<GeocoderResult> findAll(String query, Sort sort) {
		String sql = GEOCODE_SQL + sortString(sort);
//        String s = query.replace(" ", " & ");
//        s = s + ":*";

		return getJdbcTemplate().query(sql, new Object[] { query }, geocodeRowMapperPortalPK);
	}

	// version toma preferencia. Si encontramos version, está habilitado toda la
	// versión.
	// methodName es el nombre publico de la api, tal y como lo describe Swagger.
	public boolean isMethodAllowed(String version, String methodName) {
		if (configMethodsAllowed.isEmpty())
			return true;
		if (configMethodsAllowed.contains(version))
			return true;
		if (configMethodsAllowed.contains(methodName))
			return true;
		return false;
	}

	public Slice<GeocoderResult> findSlice(String query, Pageable pageable) {
		String sql = GEOCODE_SQL + pageableString(pageable);
		List<GeocoderResult> result = getJdbcTemplate().query(sql, new Object[] { query }, geocodeRowMapperPortalPK);
		boolean last = result.size() < pageable.getPageSize();
		return new SliceImpl<GeocoderResult>(result, pageable, !last);
	}

	public Page<GeocoderResult> findPage(String query, Pageable pageable) {
		String countSql = String.format(COUNT_SQL, GEOCODE_SQL);
//        String s = query.replace(" ", " & ");
//        s = s + ":*";

		Long total = getJdbcTemplate().queryForObject(countSql, new Object[] { query }, Long.class);
		List<GeocoderResult> content = findSlice(query, pageable).getContent();
		return new PageImpl<GeocoderResult>(content, pageable, total);
	}

	private String sustituirPrimero(String origen, String desde, String hasta) {
		String porigen[] = origen.split(" ");
		String palabras[] = desde.split(" ");
		for (int i = 0; (i + palabras.length - 1) < porigen.length; i++) {
			boolean igual;
			if (porigen[i].equals(palabras[0])) {
				igual = true;
				for (int j = 1; j < palabras.length; j++) {
					if (!porigen[i + j].equals(palabras[j])) {
						igual = false;
					}
				}
				if (igual) {
					String salida = "";
					for (int k = 0; k < i; k++) {
						salida += porigen[k] + " ";
					}
					salida += hasta + " ";
					for (int k = (i + palabras.length); k < porigen.length; k++) {
						salida += porigen[k] + " ";
					}
					return salida.trim();
				}
			}
		}
		return origen;
	}

	public String procesaSinonimosFirstWord(String s) {
		String sAux = s.toUpperCase().replaceAll(" +", " ");
		String[] tokens = sAux.split(" ");

		int pos = 0;

		for (String t : tokens) {
			if (t.trim() == "")
				continue;
			if (sinonimos.containsKey(t)) {
				List<String> aux = sinonimos.get(t);
				if (pos == 0) {
					for (String st : aux) {
						sAux = sAux.replace(t, st);
						break;
					}
				}
			}
		}
		String[] tokensCorregidos = sAux.split(" ");
		if (tokensCorregidos.length > 0) {
			if (tipo_vialidad.contains(tokensCorregidos[0])) {
				sAux = sAux.replace(tokensCorregidos[0], "");
			}
		}

		return sAux;
	}

	public List<GeocoderResult> findCandidates(String query, int limit, boolean v1mode) {
		Sort sort = Sort.by("departamento, localidad, nombre");
//		String auxS = procesaSinonimosFirstWord(query);

		EntradaNormalizada en = GeocodingUtils.parseAddress(query.toUpperCase());
		switch (en.getTipoDirec()) {
		case CALLE:
		case CALLEyPORTAL: {
			// Ponemos 20 para asegurarnos de que encontramos los suficientes y luego reordenamos
			int aux = Math.max(limit, 30);
			List<GeocoderResult> res = findCandidatesCalle(en, aux); 
			if (en.getLocalidad() == null)
				en.setLocalidad(en.getNomVia());

			if (v1mode) {
				List<GeocoderResult> merged = findCandidatesLocalidad(en, 5);
				res.addAll(merged);
			}
			List<GeocoderResult> mergedPois = findCandidatesPoi(en, 5);
			res.addAll(mergedPois);

//			for (GeocoderResult r : res) {
//				double score = r.getRanking(); 
//				if (r.getType() == TipoDirec.LOCALIDAD) {
//					// score = fuzzyScore.fuzzyScore(r.getAddress(), en.getEntrada()) / 10.0;
//					score = FuzzySearch.weightedRatio(r.getAddress(), en.getEntrada()) / 10.0;
//					score = score + 20;
//				}
//				if (r.getType() == TipoDirec.POI) {
//					// score = fuzzyScore.fuzzyScore(r.getAddress(), en.getEntrada()) / 10.0;
//					score = FuzzySearch.weightedRatio(r.getAddress(), en.getEntrada()) / 10.0;
//					score = score + 15;
//				}
//				if (r.getType() == TipoDirec.ESQUINA) {
//					score = score + 10;
//				}
//				if (r.getType() == TipoDirec.CALLEyPORTAL) {
//					score = score + 5;
//				}
//
//				r.setRanking(score);
//			}
//			res.sort(null);
			return res;
		}
		case MANZANAySOLAR:
			return findManzanaYsolar(en, limit);
		case RUTAyKM:
			return findRuta(en.getRuta(), en.getKm(), limit);
		case ESQUINA:
			String auxLocDep = "," + en.getDepartamento();
			if (en.getLocalidad() != null)
				auxLocDep = "," + en.getLocalidad() + auxLocDep;  
			return findCandidatesEsq(en.getNomVia() + auxLocDep, en.getEsquinaCon() + auxLocDep, limit);
		default:
			return null;
		}
	}

	public List<GeocoderResult> findRuta(String ruta, double km, int limit) {
		String sql = GEOCODE_SQL_RUTA;

		// nombre like 'RUTA 5 %'
		List<GeocoderResult> result = getJdbcTemplate().query(sql, new Object[] { "RUTA " + ruta + " %", limit },
				geocodeRowMapperRuta);

		// TODO: DISTINGUIR ENTRE CANDIDATES Y getAddress
		List<GeocoderResult> result2 = new ArrayList<GeocoderResult>();
		int i = 0;
		int idAux = 0;
		for (GeocoderResult r : result) {
			int idCalle = r.getIdCalle();
			int idRuta = Integer.parseInt(r.getId());
			if (i == 0) {
				idAux = idCalle;
			} else {
				if (idCalle != idAux)
					break;
			}
			List<GeocoderResult> aux = findRutaYkm(idCalle, idRuta, km);
			if (aux.size() > 0)
				result2.addAll(aux);
			else {
				r.setKm(km);
				result2.add(r);
			}
			i++;
		}
		return result2;
	}

	public List<GeocoderResult> findRutaYkm(int idCalle, int idRuta, double km) {
		String sql = GEOCODE_SQL_RUTA_KM; // solo devuelve un registro o ninguno

		// Parámetros: idcalle, km, idcalle, km, km, km, id_ruta
		// DEVUELVE fraccion, idcalle, nombreruta, p4326, localidad, departamento
		List<GeocoderResult> result = getJdbcTemplate().query(sql,
				new Object[] { idCalle, km, idCalle, km, km, km, idRuta }, geocodeRowMapperRutaYkm);
		for (GeocoderResult r : result) {
			r.setKm(km);
			r.setIdCalle(idCalle); // EL id que viene de la consulta es en realidad de tramo_ruta, por eso lo
									// cambio de nuevo aquí.
		}
		return result;
	}

	private List<GeocoderResult> findManzanaYsolar(EntradaNormalizada en, int limit) {
		String sqlManzanaYsolar = GEOCODE_SQL_SOLAR;

		List<GeocoderResult> result = getJdbcTemplate().query(sqlManzanaYsolar,
				new Object[] { en.getSolar(), en.getManzana(), en.getDepartamento(), en.getLocalidad(), limit },
				geocodeRowMapperManzanaYsolar);
		return result;
	}

	public List<GeocoderResult> findCandidatesCalle(EntradaNormalizada en, int limit) {
		List<GeocoderResult> result = new ArrayList<GeocoderResult>();

		String s = en.getNomVia().toUpperCase().replaceAll(" +", " ").trim();

		String[] tokens = s.split(" ");
		List<String> buscar = new ArrayList<>();
		buscar.add(s);

		int pos = 0;
		List<String> sinon0 = null; // solo hasta 2 variantes en posiciones 1 y 2
		String t0 = null;
		for (String t : tokens) {
			if (t.trim() == "")
				continue;
			if (sinonimos.containsKey(t)) {
				List<String> aux = sinonimos.get(t);
				if (pos == 0) {
					sinon0 = aux;
					t0 = t;
					for (String st : aux) {
						String sBuscar = s.replace(t, st);
						buscar.add(sBuscar);
					}
				} else if (pos == 1) {
					for (String s0 : sinon0) {
						String sBuscar = s.replace(t0, s0);
						for (String st : aux) {
							sBuscar = sBuscar.replace(t, st);
							buscar.add(sBuscar);
						}
					}

				} else {
					for (String st : aux) {
						String sBuscar = s.replace(t, st);
						buscar.add(sBuscar);
					}
				}
				pos++;
			}
		}

		if (tokens.length >= 3) // Prioridad para las 3 primeras palabras, luego 2 y luego 1
		{
			String key = tokens[0] + " " + tokens[1] + " " + tokens[2];
			if (sinonimos.containsKey(key)) {
				List<String> aux = sinonimos.get(key);
				for (String st : aux) {
					String sBuscar = s.replace(key, st);
					buscar.add(sBuscar);
				}
			}
		}
		if (tokens.length >= 2) {
			String key = tokens[0] + " " + tokens[1];
			if (sinonimos.containsKey(key)) {
				List<String> aux = sinonimos.get(key);
				for (String st : aux) {
					String sBuscar = s.replace(key, st);
					buscar.add(sBuscar);
				}
			}
		}
		buscar = buscar.stream().distinct().collect(Collectors.toList());
		String auxLocOrDep = "";
		if (en.getLocalidad() != null) {
			auxLocOrDep = en.getLocalidad();
		}
		else
			if (en.getDepartamento() != null)
				auxLocOrDep = en.getDepartamento();
		// similarity(concat(c.nombre, ' ', c.localidad), '[NOMBREyLOCALIDAD]') as ranking
		int auxL = en.getNomVia().length() + 7; // Para que la parte posterior no pese tanto en similarity
												// (PASAJE OLIMAR ASENT NUESTROS HIJOS)
		String rankearPor = "similarity(concat(left(c.nombre, " + auxL + "),' ', c.localidad), '" + en.getNomVia() + ',' + auxLocOrDep + "')";		
		String rankearPorAlias = "similarity(concat(left(c.aliasbuscar, " + auxL + "),' ', c.localidad), '" + en.getNomVia() + ',' + auxLocOrDep + "')";	
		rankearPor = "greatest(" + rankearPor + ", " + rankearPorAlias + ")";			
//		if (buscar.size() > 1)
//		{
//			int auxL2 = buscar.get(1).length() + 7;
//			String auxRankearPor = "similarity(concat(left(c.nombre, " + auxL2 +") , ' ', c.localidad), '" + buscar.get(1) + ',' + auxLocOrDep + "')"; 
//			rankearPor = "greatest(" + rankearPor + ", " + auxRankearPor + ")";			
//		}
		rankearPor += " as ranking";
		String sqlTotal = GEOCODE_BASE_SQL.replace("[SIMILARITY_RANKING]", rankearPor);		
		String whereClause = "( immutable_unaccent(c.aliasbuscar) ilike unaccent('%" + en.getNomVia() + "%') \n";
		whereClause = whereClause + " OR immutable_unaccent(c.nombre) % unaccent('" + en.getNomVia() + "') \n";

		for (String bus : buscar) {
//	        String sBus = bus.replace(" ", "&");	
//	        sBus = sBus + ":*";
			String sBus = bus;
			logger.info("buscando candidatos: " + sBus);
			whereClause = whereClause + " OR immutable_unaccent(c.aliasbuscar) % unaccent('" + sBus + "') \n";
		}
		whereClause += ") ";
		if ((en.getLocalidad() == null) && (en.getDepartamento() == null)) {
		} else {
			if (en.getLocalidad() == null) {
				whereClause = whereClause + " AND (c.localidad % unaccent('" + en.getDepartamento() + "')";
				whereClause = whereClause + " OR c.departamento % unaccent('" + en.getDepartamento() + "')";
				whereClause = whereClause + " or l.nombre % unaccent('" + en.getDepartamento() + " ') OR a.nombre % unaccent('" + en.getDepartamento() + "'))";
			} else {
				whereClause = whereClause + " AND (c.localidad % unaccent('" + en.getLocalidad() + "') or l.nombre % unaccent('" + en.getLocalidad() + "')";
				whereClause = whereClause + " AND c.departamento % unaccent('" + en.getDepartamento() + "') OR a.nombre % unaccent('" + en.getDepartamento() + "'))";
			}
		}

		sqlTotal = sqlTotal.replace("[WHERE_CLAUSE]", whereClause);
		List<GeocoderResult> result2 = getJdbcTemplate().query(sqlTotal, new Object[] {}, geocodeRowMapperCalle);
		result.addAll(result2);

		List<GeocoderResult> deduped = removeDuplicates(result);
		logger.info(sqlTotal + ": \n Num resultados:" + result.size() + " -> " + deduped.size());
//        Collections.sort(deduped);
		if (en.getPortal() != -1) {
			for (GeocoderResult r : deduped) {
				r.setLetra(en.getLetra()); // FIJAR SIEMPRE LETRA ANTES QUE PORTAL
				r.setPortalNumber(en.getPortal());
				
				r.setType(TipoDirec.CALLEyPORTAL);
			}
		}
		deduped = deduped.stream().limit(limit).collect(Collectors.toList());
		rankearPor = cleanDirec(rankearPor);
		for (GeocoderResult r : deduped) {
			int nomviaLimit = Math.min(r.getNomVia().length(), auxL+1);
			String dir = r.getNomVia().substring(0, nomviaLimit) + "," + r.getLocalidad();
			dir = cleanDirec(dir);			
			double score = r.getRanking() * 10.0;
			
//			System.out.println("Score:" + score + " dir=" + dir + " rankearPor:" + rankearPor);

			String[] words = StringUtils.splitByWholeSeparator(r.getNomVia(), " ");
			String dirSin = "";
			if (words.length > 2) {
				// El algoritmo es demasiado sensible a la primera palabra. Intentamos evitar aquí
				// que si la calle en la base de datos empieza por GENERAL por ejemplo, y el usuario no
				// ha escrito la primera palabra, probamos a quitarla y ver qué tal es el score.
				dirSin = words[1] + " ";
				for (int i=2;  i < words.length; i++) {
					dirSin += words[i] + " ";
				}
				dirSin = dirSin.trim() +"," + r.getLocalidad();
				dirSin = cleanDirec(dirSin);
			}			
			for (String b : buscar) {
				String bAux = b + "," + auxLocOrDep;
				
				double scoreSinonimo = FuzzySearch.weightedRatio(bAux, dir) / 10.0;		
//				System.out.println("Score Sinonimo:" + scoreSinonimo + " bAux=" + bAux + " dir:" + dir);
				score = Math.max(score,  scoreSinonimo);
				if (words.length > 2) {
					double scoreTruco = 0;
					bAux = cleanDirec(bAux);
					scoreTruco = FuzzySearch.weightedRatio(bAux, dirSin) / 10.0;
					
//					System.out.println("Score Truco:" + scoreTruco + " bAux=" + bAux + " dirSin:" + dirSin);
					
					score = Math.max(score, scoreTruco);
				}
			}
			double scoreLocalidad = FuzzySearch.weightedRatio(auxLocOrDep, r.getLocalidad()) / 10;
			score += scoreLocalidad;
			
//			double score = r.getRanking();
			
//	    	if (r.getType() == TipoDirec.LOCALIDAD)
//	    	{
//	    		score = score + 20;
//	    	}
			if (r.getType() == TipoDirec.ESQUINA) {
				score = score + 10;
			}
			if (r.getType() == TipoDirec.CALLEyPORTAL) {
				score = score + 5;
			}

			r.setRanking(score);
		}
		deduped.sort(null);
		return deduped;

	}

	private List<GeocoderResult> removeDuplicates(List<GeocoderResult> results) {
		return results.stream().filter(distinctByIdCalle_Loc_Dep(GeocoderResult::getIdCalle, GeocoderResult::getIdLocalidad, GeocoderResult::getIdDepartamento))
				.collect(Collectors.toList());
	}


	private String cleanDirec(String dir) {
		String aux = dir.replace("AV ", "");
		aux = aux.replace("AVENIDA ", "");
		aux = aux.replace("BV ", "");
		aux = aux.replace("BOULEVARD ", "");
		aux = aux.replace("BULEVARD ", "");
		return aux;
	}

	public List<GeocoderResult> findCandidatesPoi(EntradaNormalizada e, int limit) {
		String sql = GEOCODE_SQL_INMUEBLE;
		List<GeocoderResult> result = new ArrayList<GeocoderResult>();

		logger.info("buscando candidatos POI: " + e.getEntrada());
		if (e.getDepartamento() != null)
			sql += " and d.nombre % unaccent('" + e.getDepartamento() + "')";

		sql += " LIMIT ?";

		// result = getJdbcTemplate().query(sql, new Object[] { e.getNomVia(), e.getNomVia(), limit }, geocodeRowMapperPoi);
		result = getJdbcTemplate().query(sql, new Object[] { e.getNomVia(), limit }, geocodeRowMapperPoi);

		List<GeocoderResult> deduped = result.stream().distinct().collect(Collectors.toList());

		// logger.info(e.getEntrada() + ":" + result.size() + " -> " + deduped.size());
//        Collections.sort(deduped);
		for (GeocoderResult r : deduped) {
			String auxLocOrDep = "";
			if (r.getLocalidad() != null) {
				auxLocOrDep = r.getLocalidad();
			}
			else
				if (r.getDepartamento() != null)
					auxLocOrDep = r.getDepartamento();

//			int auxL = r.getNomVia().length() + 7; // Para que la parte posterior no pese tanto en similarity
//			String rankearPor = "similarity(concat(left(c.nombre, " + auxL + "),' ', c.localidad), '" + r.getNomVia() + ',' + auxLocOrDep + "')";		
//			rankearPor += " as ranking";

//			int nomviaLimit = Math.min(r.getNomVia().length(), auxL + 1);
//			String dir = r.getNomVia().substring(0, nomviaLimit) + "," + r.getLocalidad();
			String rankearPor = e.getNomVia() + "," + e.getLocalidad();
			String dir = r.getNomVia() + "," + r.getLocalidad();
			double score = FuzzySearch.weightedRatio(rankearPor,dir) / 10.0;

			double scoreTruco = 0;

			String[] words = StringUtils.splitByWholeSeparator(r.getNomVia(), " ");
			if (words.length > 2) {
				// El algoritmo es demasiado sensible a la primera palabra. Intentamos evitar aquí
				// que si la calle en la base de datos empieza por GENERAL por ejemplo, y el usuario no
				// ha escrito la primera palabra, probamos a quitarla y ver qué tal es el score.
				String dirSin = words[1] + " ";
				for (int i=2;  i < words.length; i++) {
					dirSin += words[i] + " ";
				}
				dirSin = dirSin.trim() +"," + r.getLocalidad();
				scoreTruco = FuzzySearch.weightedRatio(rankearPor, dirSin) / 10.0;
				score = Math.max(score, scoreTruco);
			}
			double scoreLocalidad = FuzzySearch.weightedRatio(auxLocOrDep, r.getLocalidad()) / 10;
			score += scoreLocalidad + 5;
			
			r.setRanking(score);
		}
		setAproximado(e.getPortal(), e.getLetra(), deduped);

		
		return deduped.stream().limit(limit).collect(Collectors.toList());
	}

	public List<GeocoderResult> findCandidatesLocalidad(EntradaNormalizada e, int limit) {
		Sort sort = Sort.by("departamento, localidad");

		// String sql = GEOCODE_DISTINCT_SQL + " LIMIT " + limit;
		String sql = GEOCODE_SQL_LOCALIDAD;
		List<GeocoderResult> result = new ArrayList<GeocoderResult>();

		logger.info("buscando candidatos: " + e.getEntrada());
		if (e.getDepartamento() != null)
			sql += " and d.nombre % unaccent('" + e.getDepartamento() + "')";
		sql += " ORDER BY ranking DESC LIMIT ?";

		result = getJdbcTemplate().query(sql, new Object[] { e.getLocalidad(), e.getLocalidad(), e.getLocalidad(), limit },
				geocodeRowMapperLocalidad);

		List<GeocoderResult> deduped = result.stream().distinct().collect(Collectors.toList());

		for (GeocoderResult r : deduped) {
			double score = (FuzzySearch.weightedRatio(r.getAddress(), e.getEntrada()) / 10.0) + 10.0; // Para que aparezcan
																									// antes las
																									// localidades
			r.setRanking(score);
		}
		deduped.sort(null);

		logger.info(e.getEntrada() + ":" + result.size() + " -> " + deduped.size());
//        Collections.sort(deduped);
		return deduped.stream().limit(limit).collect(Collectors.toList());
	}

	private String sortString(Sort sort) {

		StringBuilder p = new StringBuilder();

		p.append(" ORDER BY ");

		boolean first = true;
		for (Sort.Order order : sort) {
			if (!first) {
				p.append(',');
			}
			p.append(' ').append(order.getProperty()).append(' ').append(order.getDirection());
			first = false;
		}

		return p.toString();
	}

	private String pageableString(Pageable pageable) {
		StringBuilder p = new StringBuilder();

		if (pageable.getSort() != null) {
			p.append(sortString(pageable.getSort()));
		}

		p.append(" LIMIT ").append(pageable.getPageSize()).append(" OFFSET ").append(pageable.getOffset());
		return p.toString();
	}

	public List<GeocoderResult> findCalle(String nomvia, String localidad, String departamento) {

		String localidadU = localidad.toUpperCase();
		String departamentoU = departamento.toUpperCase();

		if (nomvia.trim() == "") {
			return findLocalidad(localidadU, departamentoU);
		}
		String nomviaU = nomvia.toUpperCase();
		List<GeocoderResult> result = null;

		result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_CALLE_WITH_NOMVIA_LOCALIDAD,
				new Object[] { departamentoU, localidadU, nomviaU }, geocodeRowMapperExactCalle);
		if (result.size() > 0)
			return result;
		
		// No hemos encontrado la geometría de calle, así que buscamos los números de
		// portal, y nos quedamos con el intermedio.

		if (localidad != "") {
			result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_ADDRESS,
					new Object[] { departamentoU, localidadU, nomviaU }, geocodeRowMapperPortalPK);
		} else {
			result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_ADDRESS_SIN_LOCALIDAD,
					new Object[] { departamentoU, nomviaU }, geocodeRowMapperPortalPK);
		}
		
		int i = 0;
		int half = result.size() / 2;
		for (GeocoderResult r : result) {
			if (i >= half) {
				List<GeocoderResult> result2 = new ArrayList<GeocoderResult>();
				r.setType(TipoDirec.CALLE);
				r.setStateMsg("GEOMETRIA DE CALLE NO ENCONTRADA");
				result2.add(r);
				return result2;
			}
			i++;
		}
		return result;
		
		
	}

//	public List<GeocoderResult> findOldCalle(String nomvia, String localidad, String departamento) {
//		
//		String localidadU = localidad.toUpperCase();
//		String departamentoU = departamento.toUpperCase();
//		
//		if (nomvia.trim() == "")
//		{
//			return findLocalidad(localidadU, departamentoU);
//		}
//		String nomviaU = nomvia.toUpperCase();
//		if (localidad != "")
//		{
//	        List<GeocoderResult> result = getJdbcTemplate().query(
//	        		V0_SUGERENCIACALLECOMPLETA_SQL, 
//	        		new Object[]{nomviaU, nomviaU, localidadU, departamentoU},
//	        		geocodeRowMapperCalle);
//	        Collections.sort(result);
//	        return result;
//		}
//		else
//		{
//	        List<GeocoderResult> result = getJdbcTemplate().query(
//	        		OLD_GEOCODE_CALLE_SQL_SIN_LOCALIDAD, 
//	        		new Object[]{nomviaU, nomviaU, departamentoU},
//	        		geocodeRowMapperCalle);
//	        Collections.sort(result);
//	        return result;			
//		}
//	}

	public List<GeocoderResult> fuzzyGeocode(String q, int limit) {

		String qU = q.toUpperCase();
		List<GeocoderResult> result = getJdbcTemplate().query(FUZZY_GEOCODE_SQL, new Object[] { qU, qU, limit },
				geocodeRowMapperCalle);
		Collections.sort(result);
		return result;
	}

	public List<GeocoderResult> findCalleByIdCalle(String idcalle) {
		try {
			List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_CALLE_WITH_IDCALLE,
					new Object[] { Integer.parseInt(idcalle) }, geocodeRowMapperExactCalle);
			if (result.size() > 0) {
				return result;
			}
		} catch (Exception ex) {

		}
		// No hemos encontrado la geometría de calle, así que buscamos los números de
		// portal, y nos quedamos con el intermedio.
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_ADDRESS2,
				new Object[] { Integer.parseInt(idcalle) }, geocodeRowMapperPortalPK);
		int i = 0;
		int half = result.size() / 2;
		for (GeocoderResult r : result) {
			if (i >= half) {
				List<GeocoderResult> result2 = new ArrayList<GeocoderResult>();
				r.setType(TipoDirec.CALLE);
				r.setStateMsg("GEOMETRIA DE CALLE NO ENCONTRADA");
				result2.add(r);
				return result2;
			}
			i++;
		}
		return result;

	}

	public List<GeocoderResult> findCalleYportal2(String idcalle, int portal, String letra) {
		String portalWhere = " and (numero=? or ? IN (select numero from budu.alias_numero_puerta ap where ap.idpunto=p.id))";
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_ADDRESS2_WITH_PORTAL + portalWhere,
				new Object[] { Integer.parseInt(idcalle), portal, portal }, geocodeRowMapperPortalPK);
		setAproximado(portal, letra, result); // TODO: INDICAR QUE EL APROXIMADO ES POR ALIAS??
		if (result.size() == 0 ) { // Ese portal no está
			// Aproximamos
			// result = findCalleByIdCalle(idcalle);
			// Cambio: Recuperamos todos los portales de esa calle y nos quedamos con el más cercano
			result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_ADDRESS2_WITH_PORTAL + " ORDER BY numero;",
					new Object[] { Integer.parseInt(idcalle) }, geocodeRowMapperPortalPK);

			GeocoderResult rMin = null;
			GeocoderResult rMax = null;
			for (GeocoderResult r : result) {
				r.setState(2);
				r.setType(TipoDirec.CALLEyPORTAL);
				r.setStateMsg("Aproximado");
				if (r.getPortalNumber() >= portal) {
					rMax = r;
					break;
				}
				rMin = r;
			}
			List<GeocoderResult> aux = new ArrayList<GeocoderResult>();
			if (rMin != null) {
				if (rMax == null) {
					aux.add(rMin);
				} else {
					if (portal - rMin.getPortalNumber() < rMax.getPortalNumber() - portal)
						aux.add(rMin);
					else
						aux.add(rMax);
				}
			} else {
				if (rMax != null)
					aux.add(rMax);
			}

			return aux;

		}
//		for (GeocoderResult r : result) {
//			double score = r.getRanking();
//			System.out.println("score " + r.getAddress() + " = " + score);
//			if (r.getState() == 1) {
////				score = score + 0.0000001;
//			}
//
//			r.setRanking(score);
//		}
		result.sort(null);

		return result;
	}

	public void setAproximado(int portal, String letra, List<GeocoderResult> result) {
		if (result.size() > 0) {
			List<GeocoderResult> exact = null;
			for (GeocoderResult r : result) {
				if (letra != null) {
					if ((r.getLetra() == null) || (!r.getLetra().equals(letra))) {
						r.setState(2);
						r.setStateMsg("Aproximado");
					}
					if ((r.getLetra() != null) && (r.getLetra().equalsIgnoreCase(letra))) {
						exact = new ArrayList<GeocoderResult>();
						exact.add(r);
					}
				}
				else // No hemos pedido letra, pero este portal viene con letra
				{
					if ((r.getLetra() != null) && (!r.getLetra().equals("")))
					{
						r.setState(2);
						r.setStateMsg("Aproximado");
					}
				}
				if (portal != r.getPortalNumber())
				{
					r.setState(2);
					r.setStateMsg("Aproximado");
				}
			}
		}
	}

	public List<GeocoderResult> findLocalidad(String localidad, String departamento) {
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_LOCALIDAD,
				new Object[] { departamento, localidad }, geocodeRowMapperLocalidad);
//		for (GeocoderResult r : result) {
//			r.setType(TipoDirec.LOCALIDAD);
//		}
//
//        Collections.sort(result);
		return result;
	}

	public List<GeocoderResult> findCalleYportal(String nomvia, int portal, String localidad, String departamento) {
		List<GeocoderResult> result = new ArrayList<GeocoderResult>();
		if (localidad != "") {
			result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_WITH_PORTAL_NUMBER,
					new Object[] { departamento, localidad, nomvia, portal }, geocodeRowMapperPortalPK);
		} else {
			result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_WITH_PORTAL_NUMBER_SIN_LOCALIDAD,
					new Object[] { departamento, nomvia, portal }, geocodeRowMapperPortalPK);
		}
		for (GeocoderResult r : result) {
			double score = r.getRanking();
			if (r.getState() == 1) {
				score = score + 0.1;
			}

			r.setRanking(score);
		}
		result.sort(null);
		return result;
	}

	public List<GeocoderResult> findSolar(String manzana, String solar, String localidad, String departamento) {
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_SOLAR,
				new Object[] { departamento, localidad, manzana, solar }, geocodeRowMapperManzanaYsolar);

		for (GeocoderResult r : result) {
			r.setType(TipoDirec.MANZANAySOLAR);
			r.setAddress("MANZANA " + r.getManzana() + " SOLAR " + r.getSolar() + ", " + r.getLocalidad() + ", "
					+ r.getDepartamento());
		}
		return result;
	}

	public List<GeocoderResult> findInmueble(String inmueble, String localidad, String departamento) {
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_SQL_EXACT_INMUEBLE,
				new Object[] { departamento, localidad, inmueble }, geocodeRowMapperPortalPK);
		Collections.sort(result);
		return result;
	}
	
	private class ComparatorDist implements Comparator<GeocoderResult> {
		private double lat;
		private double lng;

		public ComparatorDist(double lat, double lng) {
			super();
			this.lat = lat;
			this.lng = lng;
		}

		@Override
		public int compare(GeocoderResult o1, GeocoderResult o2) {
			Double dist1 = GeocodingUtils.calculateDistance(lat,  lng, o1.getLat(), o1.getLng());
			Double dist2 = GeocodingUtils.calculateDistance(lat,  lng, o2.getLat(), o2.getLng());
			return dist2.compareTo(dist1);
		}
		
	}

	public List<GeocoderResult> reverse(double lat, double lng, int limit) {
		String strOrderBy = " ORDER BY p4326 <-> 'SRID=4326;POINT(" + lng + " " + lat + ")'::geometry";
		String aux = GEOCODE_CLOSEST_POINTS_SQL + strOrderBy + " LIMIT ? ;";
		List<GeocoderResult> result = getJdbcTemplate().query(aux, new Object[] { limit }, geocodeRowMapperPortalPK);
		for (GeocoderResult r : result) {
			if (r.getType() == TipoDirec.MANZANAySOLAR) {
				r.setNomVia("MANZANA " + r.getManzana() + " SOLAR " + r.getSolar());
				r.setAddress(r.getNomVia());
			}
		}
		String strOrderBy2 = "ORDER BY st_transform(p.punto, 4326) <-> 'SRID=4326;POINT(" + lng + " " + lat
				+ ")'::geometry";
		String aux2 = GEOCODE_CLOSEST_POIS_SQL + strOrderBy2 + " LIMIT ? ) mySubquery;";
		List<GeocoderResult> result2 = getJdbcTemplate().query(aux2, new Object[] { limit }, geocodeRowMapperPortalPK);
		result.addAll(result2);
		ComparatorDist comp = new ComparatorDist(lat, lng);
		List<GeocoderResult> ordered = result.stream().sorted(comp).collect(Collectors.toList());
		return ordered;
	}

	public List<GeocoderResult> findCornersOfStreet(int idcalle) {
		// String strOrderBy = " ORDER BY p4326 <-> 'SRID=4326;POINT(" + lng + " " + lat
		// + ")'::geometry";
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_CORNERS_OF_STREET_SQL,
				new Object[] { idcalle, idcalle }, geocodeRowMapperEsquina);
//        if (result.size() > 0)
//        	return result;		
//
//        result = getJdbcTemplate().query(
//        		GEOCODE_CORNERS_OF_STREET_SQL, 
//        		new Object[]{ idcalle },
//        		geocodeRowMapperCalle);
//        Collections.sort(result);
		return result;

	}

	public List<GeocoderResult> findCornersOfStreet(int idcalle, String c2) {
		EntradaNormalizada esq = GeocodingUtils.parseAddress(c2);
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_CORNERS_OF_STREET_FILTERED_SQL,
				new Object[] { idcalle, "%" + esq.getNomVia() + "%", esq.getNomVia() }, geocodeRowMapperEsquina);
//        if (result.size() > 0)
//        	return result;		
//
//        result = getJdbcTemplate().query(
//        		GEOCODE_CORNERS_OF_STREET_FILTERED_SQL, 
//        		new Object[]{ idcalle, c2 },
//        		geocodeRowMapperCalle);
//        Collections.sort(result);
		return result;

	}

	public List<GeocoderResult> findCornersOfStreetByIdcalle(int idcalle, int idcalle2) {
		List<GeocoderResult> result = getJdbcTemplate().query(GEOCODE_CORNERS_OF_STREET_EXACT_SQL,
				new Object[] { idcalle, idcalle2 }, geocodeRowMapperEsquina);

//        if (result.size() > 0)
//        	return result;		
//
//        result = getJdbcTemplate().query(
//        		GEOCODE_CORNERS_OF_STREET_FILTERED_SQL, 
//        		new Object[]{ idcalle, c2 },
//        		geocodeRowMapperCalle);
//        Collections.sort(result);
		return result;

	}

	public List<GeocoderResult> findCandidatesEsq(String c1, String c2, int limit) {
		List<GeocoderResult> lstC1 = findCandidates(c1, limit, true);
		List<GeocoderResult> result = new ArrayList<GeocoderResult>();
		for (GeocoderResult r : lstC1) {
//	        String sBus = c2.replace(" ", "&");	
//	        sBus = sBus + ":*";
			String sBus = c2;
			List<GeocoderResult> esquinas = findCornersOfStreet(Integer.parseInt(r.getId()), sBus);
			for (GeocoderResult rEsq : esquinas) {
				GeocoderResult resEsq = crearEsquinaResult(r, rEsq);
				result.add(resEsq);
			}
		}
		List<GeocoderResult> deduped = result.stream().distinct().collect(Collectors.toList());
		logger.info("Esquinas: \n Num resultados:" + result.size() + " -> " + deduped.size());

		return deduped;
	}

	public GeocoderResult crearEsquinaResult(GeocoderResult r, GeocoderResult esq) {
		GeocoderResult rEsq = r.toBuilder().build();
//		rEsq.setAddress(r.getNomVia() + " ESQ " + esq.getAddress());
		rEsq.setAddress(esq.getAddress());
		rEsq.setIdCalleEsq(esq.getIdCalle());
		rEsq.setLat(esq.getLat());
		rEsq.setLng(esq.getLng());

		rEsq.setType(EntradaNormalizada.TipoDirec.ESQUINA);

		return rEsq;
	}

	public List<LocalidadResultV0> findLocalidad(String departamento, boolean alias) {
		List<LocalidadResultV0> result = getJdbcTemplate().query(GEOCODE_LOCALIDAD_POR_DEPARTAMENTO_SQL,
				new Object[] { departamento }, geocodeRowLocalidadMapper);

		if (alias) {
			for (LocalidadResultV0 l : result) {
				String sql = ""
						+ " select a.id, a.nombre, a.localidad_id, ta.publicable from budu.alias_localidad_geo a, budu.tipo_alias ta  where "
						+ " a.localidad_id = " + l.getId() + " and (ta.id = 1 or ta.id = 3)"
						+ " and a.tipo_alias_id= ta.id;";
				// + "ta.publicable = true ";
				List<AliasLocalidad> auxList = getJdbcTemplate().query(sql, new RowMapper<AliasLocalidad>() {
					public AliasLocalidad mapRow(ResultSet rs, int rowNum) throws SQLException {
						AliasLocalidad a = new AliasLocalidad();
						a.setId(rs.getInt("id"));
						a.setNombre(rs.getString("nombre"));
						a.setTipo_alias(rs.getBoolean("publicable"));
						return a;
					}
				});
				l.setAlias(auxList);
			}
		}

		return result;
	}

	public List<GeocoderResult> getPortalesDentroDe(String poligono, int limit) {
		String sqlPortalesDentro = GEOCODE_SQL_PORTALES_DENTRO_POLIGONO + " LIMIT ?";

		List<GeocoderResult> result = getJdbcTemplate().query(sqlPortalesDentro, new Object[] { poligono, limit },
				geocodeRowMapperPortalPK);
		return result;
	}

	public List<GeocoderResult> getSolaresDentroDe(String poligono, int limit) {
		String sqlPortalesDentro = GEOCODE_SQL_SOLARES_DENTRO_POLIGONO + " LIMIT ? ";

		List<GeocoderResult> result = getJdbcTemplate().query(sqlPortalesDentro, new Object[] { poligono, limit },
				geocodeRowMapperPortalPK);
		return result;
	}

	public List<GeocoderResult> getPoisDentroDe(String poligono, int limit) {
		String sqlPortalesDentro = GEOCODE_SQL_POIS_DENTRO_POLIGONO + " LIMIT ? ";

		List<GeocoderResult> result = getJdbcTemplate().query(sqlPortalesDentro, new Object[] { poligono, limit },
				geocodeRowMapperPortalPK);
		return result;
	}

	public String getPoligono(String capa, String campo_id, String campoGeom, int id) {
		String sql = GEOCODE_SQL_DIRECCIONES_EN_POLIGONO;
		sql = sql.replace("NOMBRE_CAPA", capa);
		sql = sql.replace("NOMBRE_GEOM", campoGeom);
		sql = sql.replace("NOMBRE_ID", campo_id);

		List<String> result = getJdbcTemplate().query(sql, new Object[] { id }, geocodeRowMapperGeojson);
		if (result.size() > 0)
			return result.get(0);

		return null;

	}

	public List<CapaPoligonalResult> getCapasPoligonales() {
		List<CapaPoligonalResult> result = getJdbcTemplate().query(SQL_CAPAS_POLIGONALES,
				geocodeRowMapperCapaPoligonal);
		return result;
	}

	public CapaPoligonalResult getCapaPoligonal(String nomCapa) {
		List<CapaPoligonalResult> result = getJdbcTemplate().query(SQL_CAPAS_POLIGONALES,
				geocodeRowMapperCapaPoligonal);
		CapaPoligonalResult r = null;
		for (CapaPoligonalResult c : result) {
			if (c.getTabla().equalsIgnoreCase(nomCapa)) {
				r = c;
				break;
			}
		}
		return r;
	}

	public List<GeocoderResult> getDirecPorManzanaSolar(String departamento, String localidad, int manzana, int solar,
			int limit) {
		String sqlManzanaYsolar = GEOCODE_SQL_SOLAR;

		List<GeocoderResult> result = getJdbcTemplate().query(sqlManzanaYsolar,
				new Object[] { solar + "", manzana + "", departamento, localidad, limit },
				geocodeRowMapperManzanaYsolar);
		return result;
	}

	public List<GeocoderResult> getPadronPorDireccion(int padron, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GeocoderResult> getDirecPorPadron(String departamento, String localidad, int padron, int limit) {
		String sqlPadron = GEOCODE_SQL_PADRON + " LIMIT ? ";

		List<GeocoderResult> result = getJdbcTemplate().query(sqlPadron,
				new Object[] { localidad, padron, departamento, localidad, limit }, geocodeRowMapperManzanaYsolar);
		return result;

	}

	public List<GeocoderResult> getPois(String departamento, String poi, int limit) {
		String sql = GEOCODE_SQL_PUNTO_NOTABLE;

		List<GeocoderResult> result = getJdbcTemplate().query(sql, new Object[] { poi, departamento, limit },
				geocodeRowMapperPoi);
		return result;
	}
	
	public List<TramoResult> getTramos( int idcalle) {
		String sql = GEOCODE_SQL_TRAMOS;

		List<TramoResult> result = getJdbcTemplate().query(sql, new Object[] { idcalle }, geocodeRowMapperTramo);
		if (result.size() > 0)
			return result;

		return null;

	}


	public void refreshMaterializedViews() {
		reloadSinonimos();
		
		String sql1 = "REFRESH MATERIALIZED VIEW budu.mv_puntos_direccion;";
		String sql2 = "REFRESH MATERIALIZED VIEW budu.mv_direcciones2;";
		String sql3 = "REFRESH MATERIALIZED VIEW budu.calle_con_geom;";
		//String sql4 = "REFRESH MATERIALIZED VIEW calle_con_geom;";
//		drop table if exists BUDU.intersec_localidad_departamento cascade;
//		create table BUDU.intersec_localidad_departamento as
//			select oldLoc.apis_id as idloc, d.id as iddepto, ST_SNAP(oldLoc.multipolygon, d.geom, 0.001) as intersec , 
//					st_area(st_intersection(d.geom, ST_SNAP(oldLoc.multipolygon, d.geom, 0.001)))/(st_area(oldLoc.multipolygon))*100 as porcentaje
//				from public.localidades_no_oficiales oldLoc left join public.departamento d 
//				on
//				st_intersects (d.geom, oldLoc.multipolygon)
//				where  
//					st_area(st_intersection(d.geom, ST_SNAP(oldLoc.multipolygon, d.geom, 0.001)))/(st_area(oldLoc.multipolygon))*100 > 2;

				
	}

	public void testSql(String sql) {
		List<String> result = getJdbcTemplate().queryForList(sql, String.class);
		for (String s : result) 
			System.out.println(s);

//		getJdbcTemplate().execute(sql);
	}

}
