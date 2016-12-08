package de.adorsys.smartlogin.service;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

/**
 * Created by alexg on 07.12.16.
 */
@Singleton
@ApplicationScoped
public class SQRLCache {

    public boolean checkNutExists(String nut) {
        throw new RuntimeException("not implemented");
    }

    public void create(String nut, int ordinal) {
        throw new RuntimeException("not implemented");
    }

    public void updateState(String nut, int ordinal) {
        throw new RuntimeException("not implemented");
    }

    public void updatePreparationData(String nut, Map<String, String> data) {
        throw new RuntimeException("not implemented");
    }

    public void updateResponseData(String nut, Map<String, String> stringStringMap) {
        throw new RuntimeException("not implemented");
    }

    public int findState(String nut) {
        throw new RuntimeException("not implemented");
    }

    public Map<String,String> findPreparationData(String nut) {
        throw new RuntimeException("not implemented");
    }

    public Map<String,String> findResponseData(String nut) {
        throw new RuntimeException("not implemented");
    }

    public void drop(String nut) {
        throw new RuntimeException("not implemented");
    }
}
