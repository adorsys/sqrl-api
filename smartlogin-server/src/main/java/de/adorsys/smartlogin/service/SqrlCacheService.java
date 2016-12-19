package de.adorsys.smartlogin.service;

import de.adorsys.smartlogin.provider.SqrlCacheProvider;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Serves a bridge within the server to cache data from the clients (Web, SQRL)
 * and provide it to each other.
 * 
 * @author mko
 */

@Singleton
@ApplicationScoped
public class SqrlCacheService {

	@Inject
	private SqrlCacheProvider sqrlCache;

	public SqrlCacheService() {
	}

	/**
	 * Check if process data for nut exists.
	 *
	 * @param nut
	 *            the nut
	 * @return true, if successful
	 */
	public boolean existsProcessDataFor(String nut) {
		if (nut == null) {
			return false;
		}
		return sqrlCache.checkNutExists(nut);
	}

	/**
	 * Cache or update process data for nut.
	 *
	 * @param nut
	 *            the nut
	 * @param processData
	 */
	public void cache(String nut, SqrlProcessData processData) {
	    if(nut == null || processData == null){
	        throw new SqrlAuthException(Response.serverError().entity("Inavlid null arg 'nut' or 'processData'").build() );
	    }
	    
		if (!this.existsProcessDataFor(nut)) {
			sqrlCache.create(nut,
					processData.getState().ordinal());
		} else {
			sqrlCache.updateState(nut,
					processData.getState().ordinal());
		}

		if (processData.getPreparedData() != null && processData.getPreparedData().getData() != null) {
			sqrlCache.updatePreparationData(nut,
					processData.getPreparedData().getData());
		}
		if (processData.getResponse() != null) {
			sqrlCache.updateResponseData(nut,
					processData.getResponse().asMap());
		}
		return;

	}

	/**
	 * Fetch process data for nut.
	 *
	 * @param nut
	 *            the nut
	 * @return the processData
	 */
	public SqrlProcessData fetch(String nut) {
		if (existsProcessDataFor(nut)) {

			int state = sqrlCache.findState(nut);
			Map<String, String> prep = sqrlCache
					.findPreparationData(nut);
			Map<String, String> resp = sqrlCache
					.findResponseData(nut);

			SqrlProcessData d = new SqrlProcessData();

			d.setNut(nut);
			d.setState(SqrlState.values()[state]);
			d.setPrepareData(new SqrlAuthenticationPreparationData(prep));
			if (resp != null) {
				d.setResponse(new SqrlResponse(resp
						.get(SqrlResponse.Fields.ACCESS_TOKEN_ID), Long
						.valueOf(resp
								.get(SqrlResponse.Fields.EXPIRATION_DURATION))));
			}
			return d;
		}
		return null;
	}

	/**
	 * Drop the process data according to nut.
	 *
	 * @param nut
	 *            the nut
	 */
	public void drop(String nut) {
		sqrlCache.drop(nut);
	}

}
