var map = L.map('map', {zoomControl:true}).setView([47.3791104480105, -2.19580078125], 4);

L.tileLayer.provider('Esri.OceanBasemap').addTo(map);

var autoShowCached = false;
var autoShowCachedMinZoom = 8;

var draw;
var rectangle;

var loadedLayer = undefined;

//create a new dictionary for feature colors
let dictionary = new Map();

// layer source http://portal.emodnet-bathymetry.eu/services/#wms
let bathymetryOWSMaps = ["mean","mean_rainbowcolour","mean_multicolour","mean_atlas_land","source_references","contours","products"];


var availableLayers = ["geology", "seabed"];

var layer = "geology"
var URLpart0 ="http://127.0.0.1:8080/"+layer+"?action=getGeoJSON&minLat=";

var URLpart0Stats ="http://127.0.0.1:8080/"+layer+"?action=getStats&minLat=";
//var seabedtype = "EUSM2016_simplified200"

// DOESN'T WORK at the moment, works when commented out and using the other method
/*var hostLocal = "127.0.0.1";
var host = "172.21.190.147:8080"
var URLpart0 ="http://"+host+"/seabed?action=getGeoJSON&minLat=";
var URLpart0Stats ="http://"+host+"/seabed?action=getStats&minLat=";*/

var URLpart1="&maxLat=";
var URLpart2="&minLng=";
var URLpart3="&maxLng=";
var URLPart4="&type=";

document.getElementById("minLat").value = "";
document.getElementById("maxLat").value = "";
document.getElementById("minLng").value = "";
document.getElementById("maxLng").value = "";



var baseMaps = [];

function BathymetryCheck(layerNum){
	document.getElementById('loadingSVG').style.zIndex = "4";
	var layerName = "Bathymetry-opt" + (layerNum).toString();
	console.log("curr "+ layerName + layerNum);
	if(document.getElementById(layerName).checked == true){
	   	console.log("checked "  + layerNum);
		baseMaps.push( L.tileLayer.wms('http://ows.emodnet-bathymetry.eu/wms', {
	   		id: layerName,
		    layers: bathymetryOWSMaps[layerNum], transparent: true,
		    format: 'image/png',
		    opacity: 0.75
			}) );
		(baseMaps[baseMaps.length -1]).addTo(map);
	}else{
		console.log("removing");
		for (var key of baseMaps) { 
		    console.log(key.wmsParams.id);
		    if(key.wmsParams.id == layerName){
		    	console.log(layerName + " Match!");
		    	map.removeLayer(key);
		    }
		}
	}
	document.getElementById('loadingSVG').style.zIndex = "0";	

}




$('.btn-expand-collapse').click(function(e) {
	$('.navbar-primary').toggleClass('collapsed');
	map.invalidateSize();
});


// Draw the rectangle on the map
map.on({

	'draw:created': function (event) {
		console.log("Drawing started");
		rectangle = event.layer;
		console.log(rectangle);
	},
	'draw:drawstop': function (event) {
		console.log(rectangle.getLatLngs());
		rectangle.addTo(map);

		var coors = rectangle.getLatLngs()[0];
		var lats = coors.map(point => point.lat);
		var lons = coors.map(point => point.lng);
		document.getElementById("minLat").value = String(Math.min.apply(null, lats));
		document.getElementById("maxLat").value = String(Math.max.apply(null, lats));
		document.getElementById("minLng").value = String(Math.min.apply(null, lons));
		document.getElementById("maxLng").value = String(Math.max.apply(null, lons));

		document.getElementById('loadingSVG').style.zIndex = "4";

		if(getActiveTab() != null){
			URLpart0 ="http://127.0.0.1:8080/"+getActiveTab()+"?action=getGeoJSON&minLat=";
			URLpart0Stats ="http://127.0.0.1:8080/"+getActiveTab()+"?action=getStats&minLat=";
			getDataFromCoords();
		}else{
			document.getElementById('loadingSVG').style.zIndex = "0";
			deleteButton();
			alert("Select a layer / Not an available layer");
		}
		

	},
	//'moved': loadForView
});


/* 
Only one tab is active at a time. This will find the active one and return the name of the portal it is associated with
*/
function getActiveTab(){
	var coll = document.getElementsByClassName("collapsible");
	for (i = 0; i < coll.length; i++) { // find the other active tab and deactivate it and close its content
      if(coll[i].classList.contains("active") && availableLayers.includes(coll[i].id) ){
        return coll[i].id; 
      }
    }
    return null;
}

/*
Allows the user to load data for the full screen from previous selection when the user moves around on the map
*/
var lastNorth;
var lastEast;
function loadForView(){
		if(!autoLoadCached){
			return;
		}
		if(map.getZoom() <= autoShowCachedMinZoom){
			return;
		}
		var bnds = map.getBounds();
		var n = Math.ceil(bnds.getNorth());
		var e = Math.ceil(bnds.getEast());
		if(lastNorth == n && lastEast == e){
			return;
		}
		lastNorth = n;
		lastEast = e;

		getDataForCoords(""+bnds.getSouth(), ""+bnds.getNorth(), ""+bnds.getWest(), ""+bnds.getEast(), "True");
}

////// Adding seabed Habitat Data to the map

/*
Each feature type for each layer is given a color based on a hash produced from it's unique description. This way the color will be the same each time, as long as the unique description remains the same for that feature
*/
function getStyle(feature){
   var clr;

	var name = "";
	if(feature.properties.AllcombD){
		name = feature.properties.AllcombD;
	}else if(feature.properties.folk_5_substrate_class){
		name = feature.properties.folk_5_substrate_class;
	}	
    console.log(name);
	if(dictionary.has(name)){
		clr = dictionary.get(name);
	} else if (name) {
		clr = "#"+ intToRGB(hashCode(name)); //hexGenerator();
		dictionary.set(name,clr);
	}
	return {color : clr, weight : 0.0, fillOpacity : .75};
}

/*
Attempt to add a popup to each polygon, the popups don't work as a bug with leaflet is interferring with the recognition of individual polygons once loaded onto the map
 */
function prepFeature(feature, layer){
	var list = feature.properties.Allcomb ;
	popupOptions = {maxWidth: 200};

	layer.bindPopup( /*list.toString(), popupOptions*/ "hey" );
}



function addSeabedLayer(json){
	




	console.log("adding seabed");

    loadedLayer = L.geoJson(json,
	   { style: getStyle
      , onEachFeature : prepFeature
		, pointToLayer: function (feature, latlng) {
        return L.circleMarker(latlng, geojsonMarkerOptions);}
		});
    document.getElementById('loadingSVG').style.zIndex = "0";
    loadedLayer.addTo(map);
}


function loadDataFrom(url){
	console.log("about to add seabed");
	console.log(url);
	clearData();
	$.getJSON(url, function(json){
		clearRect();

		addSeabedLayer(json);
	});
}

// load data from the coordinates
function getDataFromCoords(){

	var minLat = document.getElementById("minLat").value;
	var maxLat = document.getElementById("maxLat").value;
	var minLng = document.getElementById("minLng").value;
	var maxLng = document.getElementById("maxLng").value;
	getDataForCoords(minLat, maxLat, minLng, maxLng, "False");
}

function getDataForCoords(minLat, maxLat, minLng, maxLng, caching){
	if(minLat == "" || maxLat == "" || minLng == "" || maxLng == ""){
		alert("Specify an area first");
		return;
	}

    URLcoordinates = minLat +
						URLpart1 + maxLat +
						URLpart2 + minLng +
						URLpart3 + maxLng +
						"&cacheOnly=" + caching +
						"&geomType=polygon";
	loadDataFrom(URLpart0 + URLcoordinates);
	loadStatsFrom(URLpart0Stats + URLcoordinates);

}



// Get statistics from the URL

function loadStatsFrom(url){
	//var div = document.getElementById('statsOutput');
	$.getJSON(url, function(json){

		var div = document.getElementById('statsOutput');

		console.log(url);


		var statsDictionary = {};
		var statsVals = []

		JSON.parse(JSON.stringify(json), function (key, value) {

			if(isInt(value) && value != 0.0){
				if(statsDictionary[value] != undefined ){ // this percentage already exists
					statsDictionary[value].push(key);
				}else{
					statsDictionary[value] = [key];
					statsVals.push(value);
				}
			}

			/*console.log("hello " +json);
			if(isInt(value) && value != 0.0){

				var y = document.createElement("div");
				y.id = "wrapper";

				var x = document.createElement("div");
			    x.className = "seaBedColorSquare";
				x.style.backgroundColor = "#"+ intToRGB(hashCode(key));

				y.appendChild(x);

				var x1 = document.createElement("div");
			    x1.innerHTML = String( Math.round(value*100) / 100 ).substring(0,4).concat("%    "+String(key));
			    x1.className = "statsValue";

			    y.appendChild(x1);
			   	div.insertBefore(y,null);
			}*/

		});

		statsVals.sort(function(a, b){return b-a}); // sorts in descending order
		console.log(statsVals);

		for(var statVal of statsVals){
			for(var keyVal of statsDictionary[statVal]){
				console.log(statVal.toString() + "  "+ keyVal);
				var y = document.createElement("div");
				y.id = "wrapper";

				var x = document.createElement("div");
			    x.className = "seaBedColorSquare";
				x.style.backgroundColor = "#"+ intToRGB(hashCode(keyVal));

				y.appendChild(x);

				var x1 = document.createElement("div");
			    x1.innerHTML =  '<strong>' + String( Math.round(statVal*100) / 100 ).substring(0,4).concat("%  </strong> "+String(keyVal));
			    x1.className = "statsValue";
			    //x1.style.fontWeight = 'bold';



			    y.appendChild(x1);
			   	div.insertBefore(y,null);
			}
		}


	} );
  undisableBtn();
}

// Create a hash for the seabed habitat type based on its unique WEB_CLASS

function hashCode(str) {
    var hash = 0;
    for (var i = 0; i < str.length; i++) {
       hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
}

function intToRGB(i){
    var c = (i & 0x00FFFFFF)
        .toString(16)
        .toUpperCase();

    return "00000".substring(0, 6 - c.length) + c;
}



function clearData(){
	
	var divDel = document.getElementById('statsOutput');
	divDel.innerHTML = "";

	clearRect();
	
	if(loadedLayer != undefined){
		loadedLayer.clearLayers();
		map.removeLayer(loadedLayer);
		loadedLayer = undefined;
	}
  disableBtn();
}

function deleteButton(){
	document.getElementById("minLat").value = "";
	document.getElementById("maxLat").value = "";
	document.getElementById("minLng").value = "";
	document.getElementById("maxLng").value = "";
	clearData();
	clearRect();
}

function clearRect(){
	
	if(rectangle != null){
		map.removeLayer(rectangle);
		rectangle = null;
	}
}



function isInt(value) {
  return !isNaN(value) && !isNaN(parseInt(value, 10));
}

function enableDrawing(){
  //delete a rectangle annimation on the map
  rectAnnimation();
  
  //drawing rectangle
	clearRect();
	draw = new L.Draw.Rectangle(map);
	draw.enable();

}



var layer = new ol.layer.Image({
	extent: [-36, 25, 43, 85],
	source: new ol.source.ImageWMS({
		url: 'http://ows.emodnet-bathymetry.eu/wms',
		// refer to the section layer name to find the name of the layer
		params: {'LAYERS': 'mean_atlas_land'}
	})
});

/**
  * Delete a rectangle annimation on the map
  */
function rectAnnimation(){
  document.getElementById("rect-pop").style.display = "none";
}

/**
  * UnDisable download button when have statictics summary
  */
function undisableBtn() {
  document.getElementById("dwn-btn").disabled = false;
}
/**
  * Disable download button when don't have statictics summary
  */
function disableBtn() {
    document.getElementById("dwn-btn").disabled = true;
}
