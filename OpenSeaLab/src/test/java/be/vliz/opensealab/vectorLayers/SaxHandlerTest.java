package vectorLayers;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class SaxHandlerTest {

	@SuppressWarnings("static-method")
	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		try (InputStream file = SaxHandlerTest.class.getResourceAsStream("test.xml")) {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			SAXHandler userhandler = new SAXHandler();
			saxParser.parse(file,userhandler);
		}
	}
}
