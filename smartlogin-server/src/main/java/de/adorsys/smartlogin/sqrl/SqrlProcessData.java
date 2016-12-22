package de.adorsys.smartlogin.sqrl;

import java.io.Serializable;

import javax.ejb.Stateful;

/**
 * Holder for SQRL process data.
 * 
 * @author mko
 */
@Stateful
public class SqrlProcessData implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private String nut;
    private SqrlState state;
    private SqrlResponse response;
    private SqrlAuthenticationPreparationData sqrlAuthenticationPreparationData;
    
    /**
     * Ctor sets state to @see SqrlState.INITIALIZED.
     */
    public SqrlProcessData(){
        this.state = SqrlState.INITIALIZED;
    }
    
    public String getNut() {
        return nut;
    }
    
    public void setNut(String nut) {
        this.nut = nut;
    }
    
    public SqrlState getState() {
        return state;
    }
    
    public void setState(SqrlState state) {
        this.state = state;
    }
    
    public SqrlResponse getResponse() {
        return response;
    }
    
    public void setResponse(SqrlResponse response) {
        this.response = response;
    }

    public boolean hasNut() {
        return nut != null;
    }

    public void setPrepareData(SqrlAuthenticationPreparationData data) {
        this.sqrlAuthenticationPreparationData = data;
    }

    public SqrlAuthenticationPreparationData getPreparedData() {
        return this.sqrlAuthenticationPreparationData;
    }

}
