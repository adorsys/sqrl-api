package de.adorsys.smartlogin.envutils;

public class EnvProperties {

    public static String getEnvProp(String propName, boolean optional){
    	String propValue = System.getenv(propName);
    	
    	if(propValue==null || propValue.trim().length()==0) {
    		if(optional) return null;
    		throw new IllegalStateException("Environmen property " + propName + " not set. Look at docker-compose.yml and volumes/sqrl-server/sqrl-server.properties for more detail");
    	}
    	return propValue;
    }

    public static String getEnvProp(String propName, String defaultValue){
    	String propValue = System.getenv(propName);
    	
    	if(propValue==null || propValue.trim().length()==0) {
    		return defaultValue;
    	}
    	return propValue;
    }
}
