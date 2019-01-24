package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.exceptions.BizzException;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.Layer;
import com.google.common.collect.ImmutableMap;
import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;

@Service(name="remoteDao")
public class RemoteFeatureTypeDaoImpl extends AbstractFeatureTypeDao {
	private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	private CloseableHttpClient httpClient;
	private DocumentBuilderFactory documentBuilderFactory;
	private XPath xpath;

	public RemoteFeatureTypeDaoImpl() {
		this.httpClient = HttpClients.custom().setConnectionManager(cm).build();

		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setNamespaceAware(true);
		// using saxon xpath since this supports xpath2.0 which is needed for wildcard namespaces
		// -> wfs elements have same local name but different namespaces
		XPathFactory xPathFactory = XPathFactory.newInstance();
		this.xpath = xPathFactory.newXPath();

	}

	@Override
	public Collection<FeatureType> getFeatureTypes(Layer layer) throws BizzException {
    Map<String,FeatureType> types = getFeatureTypesFromCapabilities(layer);
    addFeatureProperties(layer, types);

		return types.values();
	}

	private Map<String, FeatureType> getFeatureTypesFromCapabilities(Layer layer) {
    Map<String,FeatureType> types = new HashMap<>();

    HttpGet request = new HttpGet(layer.resolveUrl(Collections.singletonMap(Layer.PARAM_REQ_TYPE, "GetCapabilities")));
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
      Document doc = documentBuilder.parse(response.getEntity().getContent());

      NodeList nodeList = (NodeList) xpath.evaluate("//*[local-name() = 'FeatureType' or local-name() = 'Layer']", doc, XPathConstants.NODESET);

      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        FeatureType ft = new FeatureType(
          layer,
          xpath.evaluate("*[local-name() = 'Name']", node),
          xpath.evaluate("*[local-name() = 'Title']", node),
          xpath.evaluate("*[local-name() = 'Abstract']", node)
        );
        types.put(ft.getName(), ft);
      }
    } catch (Exception e) {
      throw new BizzException(e);
    }
    return types;
  }

  private Map<String, FeatureType> addFeatureProperties(Layer layer, Map<String, FeatureType> types) {

    HttpGet request = new HttpGet(layer.resolveUrl(
      ImmutableMap.of(Layer.PARAM_REQ_TYPE, "DescribeFeatureType", Layer.PARAM_TYPE, "")));

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      String contentType = response.getEntity().getContentType().getValue();
      if (contentType != null && contentType.startsWith("text/xml")) {
        addFeatureProperiesFromXML(response, types);
      } else if ("application/json".equals(contentType)) {
        addFeatureProperiesFromJSON(response, types);
      }

    } catch (Exception e) {
      throw new BizzException(e);
    }
    return types;
  }

  private Map<String, FeatureType> addFeatureProperiesFromJSON(HttpResponse response, Map<String,FeatureType> types)
      throws IOException {
	  Genson genson = new GensonBuilder().withConverters(new FeatureTypePropertiesConverter(types)).create();
	  genson.deserialize(response.getEntity().getContent(), FeatureType.class);

	  return types;
  }

  private Map<String, FeatureType> addFeatureProperiesFromXML(HttpResponse response, Map<String, FeatureType> types)
    throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

    DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
    Document doc = documentBuilder.parse(response.getEntity().getContent());
    xpath.setNamespaceContext(new NamespaceResolver(doc));
    NodeList typeNodes = (NodeList) xpath.evaluate("/xsd:schema/xsd:element", doc, XPathConstants.NODESET);
    for (int i = 0; i < typeNodes.getLength(); i++) {
      Map<String, String> featureTypeProperties = new HashMap<>();
      String featureTypeName = xpath.evaluate("@name",typeNodes.item(i));
      if (!types.containsKey(featureTypeName)) {
        continue;
      }
      FeatureType featureType = types.get(featureTypeName);
      String typeRef = xpath.evaluate("@type",typeNodes.item(i)).split(":")[1];
      NodeList elements = (NodeList) xpath.evaluate("/xsd:schema/xsd:complexType[@name='" + typeRef + "']/xsd:complexContent/xsd:extension/xsd:sequence/xsd:element", doc, XPathConstants.NODESET);
      for (int j = 0; j < elements.getLength(); j++) {
        featureTypeProperties.put(
          xpath.evaluate("@name", elements.item(j)),
          xpath.evaluate("@type", elements.item(j))
        );
      }
      featureType.setProperties(featureTypeProperties);
    }

    return types;
  }

  private static class FeatureTypePropertiesConverter implements Converter<FeatureType> {
	  private Map<String, FeatureType> stubs;
    public FeatureTypePropertiesConverter(Map<String, FeatureType> stubs) {
      this.stubs = stubs;
    }

    @Override
    public void serialize(FeatureType object, ObjectWriter writer, Context ctx) throws Exception {
      throw new NotImplementedException();
    }

    @Override
    public FeatureType deserialize(ObjectReader reader, Context ctx) throws Exception {

      String prefix = null;

      String propName = null;
      String propType = null;
      reader.beginObject();
      while(reader.hasNext()) {
        reader.next();
        if ("targetPrefix".equals(reader.name())) {
          prefix = reader.valueAsString();
        } else if ("featureTypes".equals(reader.name())) {
          reader.beginArray();
          while(reader.hasNext()) {
            reader.next();
            reader.beginObject();
            Map<String, String> properties = new HashMap<>();
            FeatureType featureType = null;
            while (reader.hasNext()) {
              reader.next();
              if ("typeName".equals(reader.name())) {
                featureType = this.stubs.get(prefix + ":" + reader.valueAsString());
                if (!properties.isEmpty()) {
                  featureType.setProperties(properties);
                }
              } else if ("properties".equals(reader.name())) {
                reader.beginArray();
                while (reader.hasNext()) {
                  reader.next();
                  reader.beginObject();
                  while(reader.hasNext()) {
                    reader.next();
                    if ("name".equals(reader.name())) {
                      propName = reader.valueAsString();
                    } else if ("localType".equals(reader.name())) {
                      propType = reader.valueAsString();
                    }
                  }
                  reader.endObject();
                  properties.put(propName, propType);
                }
                reader.endArray();
                if (featureType != null) {
                  featureType.setProperties(properties);
                }
                properties.clear();
              }
            }
            reader.endObject();
          }
          reader.endArray();
        }
      }
      reader.endObject();
      return null;
    }
  }

  private static class NamespaceResolver implements NamespaceContext
  {
    //Store the source document to search the namespaces
    private Document sourceDocument;

    public NamespaceResolver(Document document) {
      sourceDocument = document;
    }

    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
      if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
        return sourceDocument.lookupNamespaceURI(null);
      } else {
        return sourceDocument.lookupNamespaceURI(prefix);
      }
    }

    public String getPrefix(String namespaceURI) {
      return sourceDocument.lookupPrefix(namespaceURI);
    }

    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes(String namespaceURI) {
      return null;
    }
  }
}
