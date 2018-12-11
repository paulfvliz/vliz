package be.vliz.opensealab.vectorLayers.controller;

import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.main.LayerProvider;
import be.vliz.opensealab.vectorLayers.annotations.Compress;
import be.vliz.opensealab.vectorLayers.dao.FeatureTypeDao;
import be.vliz.opensealab.vectorLayers.dao.LayerDao;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import be.vliz.opensealab.vectorLayers.model.Layer;
import be.vliz.opensealab.vectorLayers.model.Statistics;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Path("/layer/{layerName}/feature/{featureName}")
public class FeatureTypeController {
	private LayerProvider layerProvider;
	private FeatureTypeDao featureTypeDao;
	private LayerDao layerDao;

	@Inject
	public FeatureTypeController(LayerProvider layerProvider, FeatureTypeDao featureTypeDao, LayerDao layerDao) {
		this.layerProvider = layerProvider;
		this.featureTypeDao = featureTypeDao;
		this.layerDao = layerDao;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public FeatureType getFeature(@PathParam("layerName") String layerName, @PathParam("featureName") String featureName) {
		Layer layer = null;
		try {
			layer = layerDao.getLayer(layerName);
		} catch (NoSuchElementException e) {
			String layerNames = layerDao.getLayers().stream()
					.map(Layer::getName)
					.collect(Collectors.joining(", \n"));
			String msg = String.format("Layer %s not found. Known layers are %s", layerName, layerNames);
			throw new WebApplicationException(
					msg,
					Response.status(Response.Status.NOT_FOUND.getStatusCode(), msg).build());
		}
		FeatureType featureType = null;
		try {
			featureType = featureTypeDao.getFeatureType(layer, featureName);
		} catch (NoSuchElementException e) {
			String featureTypeNames = featureTypeDao.getFeatureTypes(layer).stream()
					.map(FeatureType::getName)
					.collect(Collectors.joining(", \n"));
			String msg = String.format("FeatureType %s not found. Known types are %s", featureName, featureTypeNames);
			throw new WebApplicationException(
					msg,
					Response.status(Response.Status.NOT_FOUND.getStatusCode(), msg).build());
		}
		return featureType;
	}

	@Path("stats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Statistics getStats(
			@PathParam("layerName") String layerName,
			@PathParam("featureName") String featureName,
			@QueryParam("latmin") double latmin,
			@QueryParam("latmax") double latmax,
			@QueryParam("lonmin") double lonmin,
			@QueryParam("lonmax") double lonmax,
			@QueryParam("dividingProperty") String divider,
			@QueryParam("geomType") @DefaultValue("polygon") String geomType) {

		FeatureType featureType = getFeature(layerName, featureName);
		HashMap<String, Double> percentages = layerProvider.retrieveStats(
				new Rectangle(latmin, lonmin, latmax, lonmax),
				featureType,
				divider == null ? featureType.getLayer().getDefaultDividor() : divider,
				geomType).calculatePercentages();

		return new Statistics(percentages);
	}

	@Path("data")
	@Compress
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getGeoJSON(@PathParam("layerName") String layerName,
						   @PathParam("featureName") String featureName,
						   @QueryParam("latmin") double latmin,
						   @QueryParam("latmax") double latmax,
						   @QueryParam("lonmin") double lonmin,
						   @QueryParam("lonmax") double lonmax,
						   @QueryParam("dividingProperty") String divider,
						   @QueryParam("cacheOnly") @DefaultValue("false") boolean isCacheOnly,
						   @QueryParam("geomType") @DefaultValue("polygon") String geomType) {

		FeatureType featureType = getFeature(layerName, featureName);
		FeatureCollection fc = layerProvider.retrieve(
				new Rectangle(latmin, lonmin, latmax, lonmax),
				featureType,
				divider == null ? featureType.getLayer().getDefaultDividor() : divider,
				isCacheOnly,
				geomType);

		return fc.toGeoJSON();
	}
}