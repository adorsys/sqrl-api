package de.adorsys.smartlogin.service;

import java.io.Serializable;
import java.util.Map;

/**
 * Key-Value-Container for preparation data.
 * 
 * @author mko
 */
public class SqrlAuthenticationPreparationData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Map<String, String> data;

    public SqrlAuthenticationPreparationData(){
    }

    public SqrlAuthenticationPreparationData(Map<String, String> data){
    	this();
    	this.data = data;
    }
    
    public Map<String, String> getData() {
        return this.data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
    /**
     * Find value for key.
     *
     * @param key the key
     * @return the string
     */
    public String findValue(String key){
        if(key == null){
            return null;
        }
        return this.data.get(key);
    }
}
