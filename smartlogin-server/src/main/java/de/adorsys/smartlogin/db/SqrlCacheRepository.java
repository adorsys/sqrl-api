package de.adorsys.smartlogin.db;

import de.adorsys.smartlogin.spi.SqrlCacheProvider;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

/**
 * Created by alexg on 22.12.16.
 */
@ApplicationScoped
public class SqrlCacheRepository implements SqrlCacheProvider {

    private final static Logger LOG = LoggerFactory.getLogger(SqrlCacheRepository.class);

    @Inject
    private Datastore datastore;

    @Override
    public void create(String nut, int state) {
        LOG.info("Created sqrlCache with nut:" + nut);
        SqrlCacheItem item = new SqrlCacheItem(nut, state);
        datastore.save(item);
    }

    @Override
    public void updateState(String nut, int sqrlState) {
        UpdateOperations<SqrlCacheItem> ops = datastore.createUpdateOperations(SqrlCacheItem.class)
                .set(SqrlCacheItem.Fields.STATE, sqrlState);
        updateItem(nut, ops);
    }

    @Override
    public void updateResponseData(String nut, Map<String, String> data) {
        UpdateOperations<SqrlCacheItem> ops = datastore.createUpdateOperations(SqrlCacheItem.class)
                .set(SqrlCacheItem.Fields.RESPONSE_DATA, data);
        updateItem(nut, ops);
    }

    @Override
    public void updatePreparationData(String nut, Map<String, String> data) {
        UpdateOperations<SqrlCacheItem> ops = datastore.createUpdateOperations(SqrlCacheItem.class)
                .set(SqrlCacheItem.Fields.PREPARE_DATA, data);
        updateItem(nut, ops);
    }

    @Override
    public boolean checkNutExists(String nut) {
        SqrlCacheItem item = tryFindItemForNut(nut);
        return item != null;
    }

    @Override
    public int findState(String nut) {
        SqrlCacheItem item = tryFindItemForNut(nut);
        if (item != null) {
            return item.getState();
        }
        return -1;
    }

    @Override
    public Map<String, String> findResponseData(String nut) {
        SqrlCacheItem item = tryFindItemForNut(nut);
        if (item != null) {
            return item.getResponseData();
        }
        return null;
    }

    @Override
    public Map<String, String> findPreparationData(String nut) {
        SqrlCacheItem item = tryFindItemForNut(nut);
        if (item != null) {
            return item.getPrepareData();
        }
        return null;
    }

    private Query<SqrlCacheItem> createQueryForEqualNut(String nut) {
        Query<SqrlCacheItem> query = datastore.createQuery(SqrlCacheItem.class);
        query.criteria(SqrlCacheItem.Fields.NUT).equal(nut);
        return query;
    }

    private void updateItem(String nut, UpdateOperations<SqrlCacheItem> ops) {
        Query<SqrlCacheItem> query = createQueryForEqualNut(nut);
        datastore.update(query, ops);
    }

    private SqrlCacheItem tryFindItemForNut(String nut) {
        Query<SqrlCacheItem> query = this.createQueryForEqualNut(nut);
        return query.get();
    }

    @Override
    public void drop(String nut) {
        SqrlCacheItem item = tryFindItemForNut(nut);
        LOG.info("Destroying sqrlCache with nut:" + nut);
        datastore.delete(item);
    }
}
