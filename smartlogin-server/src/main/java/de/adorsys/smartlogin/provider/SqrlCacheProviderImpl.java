package de.adorsys.smartlogin.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 19.12.16.
 */
@ApplicationScoped
public class SqrlCacheProviderImpl implements SqrlCacheProvider {

    public enum SqrlState {
        NONE, // unset
        INITIALIZED, // web client requested uri with nut
        PREPARED, // necessary preparation ready
        SUCCEEDED, // finish - yeah
        LOGIN_SUCCEEDED, // finish - yeah
        CREATE_SUCCEEDED, // finish - yeah
        FAILED // finish - oh no
    }

    HashMap<String, Integer> nutStateMap = new HashMap<>();
    HashMap<String, Map<String, String>> nutResponseMap = new HashMap<>();
    HashMap<String, Map<String, String>> nutPreparationMap = new HashMap<>();

    @Override
    public void create(String nut, int state) {
        nutStateMap.put(nut, state);
    }

    @Override
    public void updateState(String nut, int sqrlState) {
        nutStateMap.remove(nut);
        nutStateMap.put(nut, sqrlState);
    }

    @Override
    public void updateResponseData(String nut, Map<String, String> data) {
        nutResponseMap.remove(nut);
        nutResponseMap.put(nut, data);
    }

    @Override
    public void updatePreparationData(String nut, Map<String, String> data) {
        nutPreparationMap.remove(nut);
        nutPreparationMap.put(nut, data);
    }

    @Override
    public boolean checkNutExists(String nut) {
        return nutStateMap.get(nut) != null;
    }

    @Override
    public int findState(String nut) {
        return nutStateMap.get(nut);
    }

    @Override
    public Map<String, String> findResponseData(String nut) {
        return nutResponseMap.get(nut);
    }

    @Override
    public Map<String, String> findPreparationData(String nut) {
        return nutPreparationMap.get(nut);
    }

    @Override
    public void drop(String nut) {
        nutStateMap.remove(nut);
        nutResponseMap.remove(nut);
        nutPreparationMap.remove(nut);
    }
}
