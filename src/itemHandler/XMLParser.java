package itemHandler;

import itemHandler.Item.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {
	private Item item;
	private String tmpValue;
	private ArrayList<Item> itemList;

	public XMLParser(String xml) {
		itemList = new ArrayList<Item>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(new StringReader(xml)), this);
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfig error");
		} catch (SAXException e) {
			System.out.println("SAXException : xml not well formed");
		} catch (IOException e) {
			System.out.println("IO error");
		}
	}
	
	public ArrayList<Item> getItemsList(){
		return itemList;
	}

	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
		if(item != null && !itemList.contains(item)){
			itemList.add(item);
		}
		// WE CAN USE THAT IN CASE WE USE THE XML FOR ITEMS TO BUY AND SOMETHING
		// ELSE
	}

	@Override
	public void endElement(String s, String s1, String element)
			throws SAXException {
		if (element.equals("name")) {
			item = new Item(tmpValue);
		}

		else if (element.equals("cost")) {
			item.setCost(Integer.parseInt(tmpValue));
		}
		
		else if(element.equals("attAffected")){
			item.setAttributeAffected(AttributeAffected.valueOf(tmpValue));
		}

		else if (element.equals("supply")) {
			item.setSupply(Integer.parseInt(tmpValue));
		}

		else if (element.equals("effectMagnitude")) {
			item.setAmount(Float.parseFloat(tmpValue));
		}

		else if (element.equals("type")) {
			item.setType(AssessmentType.valueOf(tmpValue));
		}

		else if (element.equals("duration")) {
			item.setDuration(Integer.parseInt(tmpValue));
		}
	}

	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
		tmpValue = new String(ac, i, j);
	}
}