package itemLoot;

import itemHandler.Item;
import itemHandler.XMLParser;

import java.util.ArrayList;


public class ItemController {

	private ArrayList<Item> itemList = null;
	
	public ArrayList<Item> getItemList(){
		return itemList;
	}
	
	public void createItemListFromContents(String content){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\r";
		String value = "";
		boolean newItem = false;
		xml += "<items>\n\r";
		String[] lines = content.split("\\r?\\n");
		for(String line : lines){
			if(line.equals("--")){
				if(newItem){
					xml += "\t</item>\n\r";
				}
				xml += "\t<item>\n\r";
				newItem = true;
			}
			else if(line.contains("name=")){
				value = line.replace("name=", "");
				xml += "\t\t<name>" + value + "</name>\n\r";
			}
			else if(line.contains("cost=")){
				value = line.replace("cost=", "");
				xml += "\t\t<cost>" + value + "</cost>\n\r";
			}
			else if(line.contains("duration=")){
				value = line.replace("duration=", "");
				if(value.equals("ONCE")){
					value = "0";
				}
				else if(value.equals("PASSIVE")){
					value = "-1";
				}
				xml += "\t\t<duration>" + value + "</duration>\n\r";
			}
			else if(line.contains("type=")){
				value = line.replace("type=", "");
				xml += "\t\t<type>" + value + "</type>\n\r";
			}
			else if(line.contains("attAffected=")){
				value = line.replace("attAffected=", "");
				xml += "\t\t<attAffected>" + value + "</attAffected>\n\r";
			}
			else if(line.contains("supply=")){
				value = line.replace("supply=", "");
				xml += "\t\t<supply>" + value + "</supply>\n\r";
			}
			else if(line.contains("effectMagnitude=")){
				value = line.replace("effectMagnitude=", "");
				xml += "\t\t<effectMagnitude>" + value + "</effectMagnitude>\n\r";
			}
		}
		if(newItem){
			xml += "\t</item>\n\r";
		}
		xml += "</items>";
		XMLParser xmlP = new XMLParser(xml);
		itemList = xmlP.getItemsList();
	}	
}
