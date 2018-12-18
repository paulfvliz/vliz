package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.exceptions.BizzException;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;
import be.vliz.emodnet.querytool.vectorLayers.model.Layer;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service(name="remoteDao")
public class RemoteFeatureTypeDaoImpl extends AbstractFeatureTypeDao {
	private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	private CloseableHttpClient httpClient;
	private DocumentBuilderFactory documentBuilderFactory;
	private XPath xpath;

	public RemoteFeatureTypeDaoImpl() {
		this.httpClient = HttpClients.custom().setConnectionManager(cm).build();

		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		// using saxon xpath since this supports xpath2.0 which is needed for wildcard namespaces
		// -> wfs elements have same local name but different namespaces
		XPathFactory xPathFactory = new XPathFactoryImpl();
		this.xpath = xPathFactory.newXPath();
	}

	@Override
	public Collection<FeatureType> getFeatureTypes(Layer layer) throws BizzException {
		List<FeatureType> types = new LinkedList<>();

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
				types.add(ft);
			}
		} catch (Exception e) {
			throw new BizzException(e);
		}

		return types;
	}
}
