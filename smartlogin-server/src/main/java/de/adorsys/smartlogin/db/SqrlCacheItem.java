package de.adorsys.smartlogin.db;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 22.12.16.
 */
@Entity(value = "sqrlcache", noClassnameStored = true)
@Indexes({ @Index(fields = @Field(value = SqrlCacheItem.Fields.NUT), options = @IndexOptions(expireAfterSeconds = 30)) })
public class SqrlCacheItem {

    public interface Fields {
        String ID = "_id";
        String NUT = "nut";
        String STATE = "state";
        String RESPONSE_DATA = "responseData";
        String PREPARE_DATA = "prepareData";
    }

    @Id
    private ObjectId cacheId;
    private String nut;
    private int state;
    private Map<String, String> responseData;
    private Map<String, String> prepareData;

    public SqrlCacheItem() {
    }

    public SqrlCacheItem(String nut, int state) {
        this();
        this.nut = nut;
        this.state = state;
        responseData = new HashMap<>();
        prepareData = new HashMap<>();
    }

    public ObjectId getCacheId() {
        return cacheId;
    }

    public SqrlCacheItem cacheId(ObjectId cacheId) {
        this.cacheId = cacheId;
        return this;
    }

    public String getNut() {
        return nut;
    }

    public SqrlCacheItem nut(String nut) {
        this.nut = nut;
        return this;
    }

    public int getState() {
        return state;
    }

    public SqrlCacheItem state(int state) {
        this.state = state;
        return this;
    }

    public Map<String, String> getResponseData() {
        return responseData;
    }

    public SqrlCacheItem responseData(Map<String, String> responseData) {
        this.responseData = responseData;
        return this;
    }

    public Map<String, String> getPrepareData() {
        return prepareData;
    }

    public SqrlCacheItem prepareData(Map<String, String> prepareData) {
        this.prepareData = prepareData;
        return this;
    }
}
