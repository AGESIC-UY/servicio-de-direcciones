// API PRINCIPAL. IMPORTANTE: TIENEN QUE HABILITAR CORS EN SISTEMAS UY!! (Supongo que lo hacen para que no todo el mundo pueda lanzar peticiones)
var geocoderURL = "https://direcciones.ide.uy";

// localhost
// geocoderURL = "http://localhost:8090";

// Test:
// geocoderURL = "https://callejerouy-direcciones.agesic.gub.uy";


function objToUrl(obj) {
    var str = [];
    for (var p in obj)
      if (obj.hasOwnProperty(p)) {
        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
      }
    return str.join("&");
}

function onClickReverse() {
    alert('Direcciones cercanas a la coordenada longitud = -54.93, latitud=-34.93');
    var latitud = -34.93;
    var longitud = -54.93;
    var serviceUrl = geocoderURL + "/api/v1/geocode/reverse?latitud=" + latitud + "&longitud=" + longitud + "&limit=3"; 
  
    fetch(serviceUrl)
        .then((response) => response.json())
        .then((json) => {
            console.log(json);
            showInfo(json);
    });
}

// Muestra información de lo que se encuentra con el método "find" (cuando el usuario selecciona una dirección)
function showInfo( obj ) {
    var str = JSON.stringify(obj, null, 2); // spacing level = 2
    document.getElementById('resultado').innerHTML = str;
}

// Se llama al teclear
function candidates(q, response) {
  var query = q.term;
  var serviceUrl =
    geocoderURL +
    "/api/v1/geocode/candidates?q=" +
    query +
    "&limit=10&soloLocalidad=false";
  var myHeaders = new Headers();
  myHeaders.append('Content-Type', 'application/json');
  var myInit = {
      header: myHeaders,
      method: 'GET',      
      mode: 'cors',
      cache: 'default'      
  }
  var myRequest = new Request(serviceUrl, myInit);
  fetch(myRequest )
    .then( response => response.json())
    .then( json => {
      console.log(json);
      // json contiene un array con los candidatos enontrados.
      // Los candidatos no tienen todavía información de geometría o latitud y longitud.
      // Para obtener estos datos, llamamos al método find.
      // Ejemplo de cada item:
        // address: "TACU, RIVERA, RIVERA"
        // departamento: "RIVERA"
        // geom: null
        // id: "28287"
        // idCalle: 28287
        // idCalleEsq: 0
        // idDepartamento: 20
        // idLocalidad: 106
        // inmueble: null
        // km: 0
        // lat: 0
        // letra: ""
        // lng: 0
        // localidad: "RIVERA"
        // manzana: null
        // nomVia: "TACU"
        // portalNumber: 0
        // postalCode: null
        // priority: 0
        // ranking: 9
        // solar: null
        // source: "ide_uy"
        // state: 1
        // stateMsg: ""
        // tip_via: null
        // type: "CALLE"
      var suggestions = json.map((a) => {
        let aux = {
          label: a.address,
          value: a,
        };
        return aux;
      });
      response(suggestions);
    });
}

function find(address) {
    // Ejemplo: api/v1/geocode/find?type=calle&nomvia=YAGUARON&departamento=MONTEVIDEO&localidad=MONTEVIDEO

    var typeSearch = address['type'];
    var inmueble = address['inmueble'];
    if (inmueble !== null)
        typeSearch = 'inmueble';
    
    
    
    var params = {};
    params = {
        type: typeSearch,
        id: address.id,
        idcalle: address.idCalle,
        idcalleEsq: address.idCalleEsq,
        nomvia: address.nomVia,
        source: 'ide_uy',
        localidad: address.localidad,
        departamento: address.departamento,
        inmueble: inmueble
    };
    if (0 !== address.portalNumber)
        params['portal'] = address.portalNumber;
    if ('' !== address.letra)
        params['letra'] = address.letra;
        

    if (null !== address['manzana']) {
        params['manzana'] = address['manzana'];
        params['solar'] = address['solar'];
    }
    if (0 !== address['km'])
        params['km'] = address['km'];

    var serviceUrl = geocoderURL +
      "/api/v1/geocode/find?" + objToUrl(params);

      fetch(serviceUrl)
      .then((response) => response.json())
      .then((json) => {
        console.log(json);
        var found = json;
        showInfo(found);
      });
  }
  

function onSelect(event, ui) {
    // $("#autocomplete").val(ui.item.label);
    event.target.value = ui.item.label;
    find( ui.item.value );
    return false;
}

function myRenderItem(ul, item) {
  var icon = '<i class="fa fa-map-marker" title="Calle"></i> ';
  if (item.value.type === "LOCALIDAD") icon = '<i class="fa fa-map-pin" title="Localidad"></i> ';
  if (item.value.type === "POI") icon = '<i class="fa fa-university" title="Punto de Interés"></i> ';
  if (item.value.type === "CALLEyPORTAL") icon = '<i class="fa fa-map-signs" title="Portal"></i> ';
  // TODO: Iconos para Manzana y Solar, esquina, etc.

  return (
    $("<li>")
      .attr("data-value", item.value)
      .append( icon + item.label)
      .appendTo(ul)
  );
}
