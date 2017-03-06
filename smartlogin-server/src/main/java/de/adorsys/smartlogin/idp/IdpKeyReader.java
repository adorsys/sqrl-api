package de.adorsys.smartlogin.idp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

public class IdpKeyReader {

    private final static Logger LOG = LoggerFactory.getLogger(IdpKeyReader.class);

	public static String readIdpCertificate(String keyId, String realmName) throws IOException{
		String realmKeyContent = realmKey(realmName);
    	JsonObject object = Json.createReader(new StringReader(realmKeyContent)).readObject();
    	JsonValue jsonValue = object.get("keys");
    	if(jsonValue!=null){
    		JsonArray jsonArray = (JsonArray)jsonValue;
    		JsonObject jsonObject = (JsonObject) jsonArray.get(0);
    		JsonString keyIdStr = jsonObject.getJsonString("kid");
    		if(keyIdStr!=null && StringUtils.equals(keyId, keyIdStr.getString())){
    			JsonString jsonString = jsonObject.getJsonString("certificate");
    			if(jsonString==null) return null;
    			return jsonString.getString();
    		}
    	}

		return null;
	}
	
    public static String realmKey(String realmName) throws IOException{
    	String realmKeyFileName = "realm-"+realmName+"-keys.json";
    	try {
    		return IdpUtils.readFile(realmKeyFileName, null);
    	} catch (FileNotFoundException f){
    		throw f;
    	} catch(IOException e){
			LOG.error("Error reading file " + realmKeyFileName, e);
			throw e;
    	}
    }
	
}
