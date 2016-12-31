package de.adorsys.smartlogin.idp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class IdpUtils {

	private static final String IDP_CONFIG_DIR = "IDP_CONFIG_DIR";

    public static String readFile(String fileName, Map<String, String> replace) throws IOException{
    	String idpConfigDirName = getIdpConfigDir();
    	File idpCOnfigDir = new File(idpConfigDirName);
    	File file = new File(idpCOnfigDir, fileName);
    	// Return No Content. Not Found might mean that endpoint does not exist.
    	if(!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
    	String fileToString;
		try {
			fileToString = FileUtils.readFileToString(file, "UTF-8");
			if(StringUtils.isBlank(fileToString)) return null;
			
			if(replace!=null && !replace.isEmpty()){
				List<Entry<String,String>> entryList =new ArrayList<>(replace.entrySet());
				String[] keys = new String[replace.size()];
				String[] values = new String[replace.size()];
				for (int i = 0; i < entryList.size(); i++) {
					Entry<String, String> entry = entryList.get(i);
					keys[i] = entry.getKey();
					values[i] = entry.getValue();
				}
				fileToString = StringUtils.replaceEach(fileToString, keys, values);
			}
			
			return fileToString;
		} catch (IOException e) {
			throw e;
		}
    }

    
    private static String getIdpConfigDir(){
    	String idpCOnfigDir = System.getenv(IDP_CONFIG_DIR);
    	if(idpCOnfigDir==null || idpCOnfigDir.trim().length()==0) throw new IllegalStateException("Environmen property " + IDP_CONFIG_DIR + " not set. This should default to: /opt/jboss/idp-post-deploy/generated. Look at docker-compose.yml for more detail");
    	return idpCOnfigDir;
    }

    
}
