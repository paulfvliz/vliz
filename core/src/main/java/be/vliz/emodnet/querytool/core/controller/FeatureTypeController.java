package be.vliz.emodnet.querytool.core.controller;

import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import be.vliz.emodnet.querytool.core.dao.LayerProvider;
import be.vliz.emodnet.querytool.core.annotations.Compress;
import be.vliz.emodnet.querytool.core.dao.FeatureTypeDao;
import be.vliz.emodnet.querytool.core.dao.LayerDao;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.Layer;
import be.vliz.emodnet.querytool.core.model.Statistics;

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
		try {
      Statistics statistics = layerProvider.retrieveStats(
        new Rectangle(latmin, lonmin, latmax, lonmax),
        featureType,
        divider == null ? featureType.getLayer().getDefaultDividor() : divider,
        geomType);

      return statistics;
    } catch (NoSuchElementException e ) {
		  throw new WebApplicationException(e.getMessage(),
        Response.status(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()).build());
    }
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
						   @QueryParam("geomType") @DefaultValue("polygon") String geomType,
               @QueryParam("filter") String filter)
  {

		FeatureType featureType = getFeature(layerName, featureName);
		if (filter != null && !filter.isEmpty()) {
      featureType.setFilter(filter);
    }

		FeatureCollection fc = layerProvider.retrieve(
				new Rectangle(latmin, lonmin, latmax, lonmax),
				featureType,
				divider == null ? featureType.getLayer().getDefaultDividor() : divider,
				isCacheOnly,
				geomType);

		return fc.toGeoJSON();
	}
}
