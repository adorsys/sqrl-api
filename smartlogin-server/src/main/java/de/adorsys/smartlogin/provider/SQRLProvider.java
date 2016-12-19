package de.adorsys.smartlogin.provider;

import java.util.Map;

/**
 * Created by alexg on 07.12.16.
 */
public interface SQRLProvider {
    boolean checkNutExists(String nut);

    void create(String nut, int ordinal);

    void updateState(String nut, int ordinal);

    void updatePreparationData(String nut, Map<String, String> data);

    void updateResponseData(String nut, Map<String, String> stringStringMap);

    int findState(String nut);

    Map<String, String> findPreparationData(String nut);

    Map<String, String> findResponseData(String nut);

    void drop(String nut);
}
