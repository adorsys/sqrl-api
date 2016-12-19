package de.adorsys.smartlogin.provider;

import java.util.Map;

/**
 * The Interface SqrlCacheProvider.
 */
public interface SqrlCacheProvider {

	/**
	 * Creates it.
	 *
	 * @param nut the nut
	 */
	void create(String nut, int state);
	
	/**
	 * Update state.
	 *
	 * @param nut the nut
	 * @param sqrlState the sqrl state
	 */
	void updateState(String nut, int sqrlState);
	
	/**
	 * Update response data.
	 *
	 * @param nut the nut
	 * @param data the data
	 */
	void updateResponseData(String nut, Map<String, String> data);
	
	/**
	 * Update preparation data.
	 *
	 * @param nut the nut
	 * @param data the data
	 */
	void updatePreparationData(String nut, Map<String, String> data);
	
	/**
	 * Check nut exists.
	 *
	 * @param nut the nut
	 * @return true, if successful
	 */
	boolean checkNutExists(String nut);
	
	/**
	 * Find state.
	 *
	 * @param nut the nut
	 * @return the int
	 */
	int findState(String nut);
	
	/**
	 * Find response data.
	 *
	 * @param nut the nut
	 * @return the map
	 */
	Map<String,String> findResponseData(String nut);
	
	/**
	 * Find preparation data.
	 *
	 * @param nut the nut
	 * @return the map
	 */
	Map<String, String> findPreparationData(String nut);
	
	/**
	 * Drop.
	 *
	 * @param nut the nut
	 */
	void drop(String nut);
}
