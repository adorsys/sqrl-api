package de.adorsys.smartlogin.db;

import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.adorsys.smartlogin.spi.SqrlCacheProvider;

/**
 * 
 * @author fpo
 *
 */
@Stateless
public class SqrlCacheRepository implements SqrlCacheProvider {

    private final static Logger LOG = LoggerFactory.getLogger(SqrlCacheRepository.class);

    @PersistenceContext
    private EntityManager em;

    private <T> SqrlCacheItem findByField(SingularAttribute<SqrlCacheItem, T> attr, T field){
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<SqrlCacheItem> cq = qb.createQuery(SqrlCacheItem.class);
		Root<SqrlCacheItem> root = cq.from(SqrlCacheItem.class);
		Predicate predicate = qb.equal(root.get(attr), field);
		cq = cq.select(root).where(predicate);
		try {
			return em.createQuery(cq).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
    }
    
    @Override
    public void create(String nut, int state) {
        LOG.info("Created sqrlCache with nut:" + nut);
        SqrlCacheItem item = new SqrlCacheItem(nut, state);
        em.persist(item);
    }

    @Override
    public void updateState(String nut, int sqrlState) {
    	SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
    	if(item==null) return;
    	item.setState(sqrlState);
    	em.merge(item);
    }

    @Override
    public void updateResponseData(String nut, Map<String, String> data) {
    	SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
    	if(item==null) return;
    	item.setResponseData(data);
    	em.merge(item);
    }

    @Override
    public void updatePreparationData(String nut, Map<String, String> data) {
    	SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
    	if(item==null) return;
    	item.setPrepareData(data);
    	em.merge(item);
    }

    @Override
    public boolean checkNutExists(String nut) {
    	SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
        return item != null;
    }

    @Override
    public int findState(String nut) {
        SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
        if (item != null) {
            return item.getState();
        }
        return -1;
    }

    @Override
    public Map<String, String> findResponseData(String nut) {
        SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
        if (item != null) {
            return item.getResponseData();
        }
        return null;
    }

    @Override
    public Map<String, String> findPreparationData(String nut) {
        SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
        if (item != null) {
            return item.getPrepareData();
        }
        return null;
    }

    @Override
    public void drop(String nut) {
        LOG.info("Destroying sqrlCache with nut:" + nut);
        SqrlCacheItem item = findByField(SqrlCacheItem_.nut, nut);
        if(nut!=null)
        	em.remove(item);
    }
}
