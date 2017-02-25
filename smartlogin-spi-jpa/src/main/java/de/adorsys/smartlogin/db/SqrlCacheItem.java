package de.adorsys.smartlogin.db;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.Table;

/**
 * 
 * @author fpo
 *
 */
@Entity
@Table(indexes={@Index(columnList="nut")})
//@Indexes({ @Index(fields = @Field(value = SqrlCacheItem.Fields.NUT), options = @IndexOptions(expireAfterSeconds = 30)) })
public class SqrlCacheItem {

    @Id
    private String cacheId;
    
    private String nut;
    private int state;
    
    @ElementCollection(fetch=FetchType.EAGER)
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="responseData", joinColumns=@JoinColumn(name="cacheId"))
    private Map<String, String> responseData = new HashMap<>();
    
    @ElementCollection(fetch=FetchType.EAGER)
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="prepareData", joinColumns=@JoinColumn(name="cacheId"))
    private Map<String, String> prepareData = new HashMap<>();

    @PrePersist
    public void postConstruct(){
    	cacheId = UUID.randomUUID().toString();
    }
    
    public SqrlCacheItem() {
    }

    public SqrlCacheItem(String nut, int state) {
        this();
        this.nut = nut;
        this.state = state;
    }

	public String getCacheId() {
		return cacheId;
	}

	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
	}

	public String getNut() {
		return nut;
	}

	public void setNut(String nut) {
		this.nut = nut;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Map<String, String> getResponseData() {
		return responseData;
	}

	public void setResponseData(Map<String, String> responseData) {
		this.responseData = responseData;
	}

	public Map<String, String> getPrepareData() {
		return prepareData;
	}

	public void setPrepareData(Map<String, String> prepareData) {
		this.prepareData = prepareData;
	}
}
