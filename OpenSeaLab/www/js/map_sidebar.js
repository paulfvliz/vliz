var map = L.map('map', {zoomControl:true}).setView([50.3791104480105, -2.19580078125], 5);

L.tileLayer.provider('Esri.OceanBasemap').addTo(map);

//create a new dictionary for feature colors
let dictionary = new Map();

var URLpart0 ="http://127.0.0.1:8080/seabed?action=getGeoJSON&minLat=";
var URLpart1="&maxLat=";
var URLpart2="&minLong=";
var URLpart3="&maxLong=";


var firstCoor; // saves the coordinate of the first ctrl+click
var polygon; // The rectangle that is drawn
var URLcoordinates; // the coordinates of the rectangle




// load data from the coordinates
function getDataFromCoords(){
	loadDataFrom(URLcoordinates);
}

function drawRectangleFromInput(){
	var minLat = document.getElementById('minLat').value;
	var minLng = document.getElementById('minLong').value;
	var maxLat = document.getElementById('maxLat').value;
	var maxLng = document.getElementById('maxLong').value;

	firstCoor = L.latLng(minLat, minLng);
	var lastCoor = L.latLng(maxLat, maxLng);
	if(polygon != null){
		map.removeLayer(polygon);
 	}
	polygon = L.polygon([
				    firstCoor,
				    [firstCoor.lat, lastCoor.lng],
				    lastCoor,
				    [lastCoor.lat, firstCoor.lng]
				]);
	polygon.addTo(map);
}


function randomHex() {
	var hexNumbers = [0,1,2,3,4,5,6,7,8,9,'A','B','C','D','E','F']
	// picking a random item of the array
	return hexNumbers[Math.floor(Math.random() * hexNumbers.length)];
}


// Genarates a Random Hex color
function hexGenerator() {
    hexValue = ['#'];
    for (var i = 0; i < 6; i += 1) {
        hexValue.push(randomHex());
    }
    return hexValue.join('');
}


function getStyle(feature){
   var clr;
	if(dictionary.has(feature.properties.WEB_CLASS)){
		clr = dictionary.get(feature.properties.WEB_CLASS);
	} else {
		clr = hexGenerator();
		dictionary.set(feature.properties.WEB_CLASS,clr);
	}
	return {color : clr};
}

function prepFeature(feature, layer){
	var seaarea = 0;
	if(feature.geometry.type == "MultiPolygon"){
		var i;
		for(i = 0; i < feature.geometry.coordinates.length; i++) {
			 seaArea = geodesicArea(layer.getLatLngs()[i]);
		} 
	}else{
		seaArea = geodesicArea(layer.getLatLngs()); 		
	}
	var list = "<dd>" + feature.properties.Allcomb + "</dd>"
			+ "<dt>Area : </dt>"
			+ seaArea ;
	layer.bindPopup( list );  
}

function addSeabedLayer(json){
    L.geoJson(json,
	   { style: getStyle
      , onEachFeature : prepFeature
		}).addTo(map); 
}


function loadDataFrom(url){
	$.getJSON(url, addSeabedLayer);
}



function geodesicArea(latLngs) {
			var pointsCount = latLngs.length,
				area = 0.0,
				d2r = Math.PI / 180,
				p1, p2;
			if (pointsCount > 2) {
				for (var i = 0; i < pointsCount; i++) {
					p1 = latLngs[i];
					p2 = latLngs[(i + 1) % pointsCount];
					area += ((p2.lng - p1.lng) * d2r) *
						(2 + Math.sin(p1.lat * d2r) + Math.sin(p2.lat * d2r));
				}
				area = area * 6378137.0 * 6378137.0 / 2.0;
			}

			return Math.abs(area);
}



map.on({click : function(e){ console.log("click"); map.dragging.enable();}});


map.on({mouseup : 
	function(e){
		console.log("mouseup");
		map.dragging.enable();
		if(!e.originalEvent.ctrlKey){
			return;
		}


		if(polygon != null){
			map.removeLayer(polygon);
		}
		var lastCoor = e.latlng;
		polygon = L.polygon([
			    firstCoor,
			    [firstCoor.lat, lastCoor.lng],
			    lastCoor,
			    [lastCoor.lat, firstCoor.lng]
			])
		polygon.addTo(map);

		if(firstCoor.lat <= lastCoor.lat ){
			document.getElementById("minLat").value = String(firstCoor.lat);
			document.getElementById("maxLat").value = String(lastCoor.lat);
		}else{
			document.getElementById("minLat").value = String(lastCoor.lat);
			document.getElementById("maxLat").value = String(firstCoor.lat);
		}
		if(firstCoor.lng <= lastCoor.lng ){
			document.getElementById("minLong").value = String(firstCoor.lng);
			document.getElementById("maxLong").value = String(lastCoor.lng);
		}else{
			document.getElementById("minLong").value = String(lastCoor.lng);
			document.getElementById("maxLong").value = String(firstCoor.lng);
		}

		URLcoordinates = URLpart0.concat(firstCoor.lat,URLpart1.concat(lastCoor.lat,URLpart2.concat(firstCoor.lng,URLpart3)))+lastCoor.lng;						
	}
});


map.on({mousedown : 
	function(e){
		console.log("Mousedown");
		if(!e.originalEvent.ctrlKey){
			return;
		}
		// map.dragging.disable();
		firstCoor = e.latlng;
		console.log(firstCoor);
		console.log("Got first coor")
	}
});

