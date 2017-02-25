package de.adorsys.smartlogin.db;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import de.adorsys.smartlogin.spi.SqrlAccountProvider;

/**
 * 
 * @author fpo
 *
 */
@Stateless
public class SqrlAccountRepository implements SqrlAccountProvider {

	@PersistenceContext
    private EntityManager em;

    public void createSqrlAccount(String idpAccountId) {
    	SqrlAccount sqrlAccount = new SqrlAccount();
    	sqrlAccount.setIdpAccountId(idpAccountId);
        em.persist(sqrlAccount);
    }

    public boolean accountExistsBySqrlAccountId(String accountId) {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		Root<SqrlAccount> root = cq.from(SqrlAccount.class);
		Predicate predicate = qb.equal(root.get(SqrlAccount_.accountId), accountId);
		cq = cq.select(qb.count(root)).where(predicate);
		return em.createQuery(cq).getSingleResult()>0;
    }

    @Override
    public boolean accountExistsByIdpAccountId(String id) {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		Root<SqrlAccount> root = cq.from(SqrlAccount.class);
		Predicate predicate = qb.equal(root.get(SqrlAccount_.idpAccountId), id);
		cq = cq.select(qb.count(root)).where(predicate);
		return em.createQuery(cq).getSingleResult()>0;
    }

    public String checkIdentity(byte[] identityKey) {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = qb.createQuery(String.class);
		Root<SqrlAccount> root = cq.from(SqrlAccount.class);
		Predicate predicate = qb.equal(root.get(SqrlAccount_.sqrlIdentityKey), identityKey);
		cq = cq.select(root.get(SqrlAccount_.accountId)).where(predicate);
		try {
			return em.createQuery(cq).getSingleResult();
		} catch(NoResultException e){
			return null;
		}
    }

    public boolean sqrlIdentityExists(String accountId) {
    	SqrlAccount p = em.find(SqrlAccount.class, accountId);
        return (p != null && p.getSqrlIdentityKey() != null);
    }

    public void deleteSqrlIdentityIfExists(String accountId) {
    	SqrlAccount p = em.find(SqrlAccount.class, accountId);
        this.doUpdateSqrlKeys(p, null, null, null);
    }
    
    public byte[] fetchServerUnlockKey(byte[] identityKey) {
    	SqrlAccount result = findByField(SqrlAccount_.sqrlIdentityKey,identityKey);
    	if (result == null || result.getSqrlServerUnlockKey()==null) return null;
    	return result.getSqrlServerUnlockKey();
    }

    public byte[] fetchVerifyUnlockKey(byte[] identityKey) {
    	SqrlAccount result = findByField(SqrlAccount_.sqrlIdentityKey, identityKey);
    	if (result == null || result.getSqrlVerifyUnlockKey()==null) return null;
    	return result.getSqrlVerifyUnlockKey();
    }
    
    private <T> SqrlAccount findByField(SingularAttribute<SqrlAccount, T> attr, T field){
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<SqrlAccount> cq = qb.createQuery(SqrlAccount.class);
		Root<SqrlAccount> root = cq.from(SqrlAccount.class);
		Predicate predicate = qb.equal(root.get(attr), field);
		cq = cq.select(root).where(predicate);
		try {
			return em.createQuery(cq).getSingleResult();
		} catch(NoResultException n){
			return null;
		}
    }

    /** sqrl key manipulation **/

    public boolean insertSqrlKeys(String userLogin, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        SqrlAccount result = findByField(SqrlAccount_.idpAccountId,userLogin);
    	em.find(SqrlAccount.class, userLogin);
        return doUpdateSqrlKeys(result, identityKey, serverUnlockKey, verifyUnlockKey);
    }

    public boolean updateSqrlKeys(byte[] previousIdentityKey, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        SqrlAccount result = findByField(SqrlAccount_.sqrlIdentityKey, previousIdentityKey);
        if (result == null) return false;
        return doUpdateSqrlKeys(result, identityKey, serverUnlockKey, verifyUnlockKey);
    }

    public boolean updateSqrlServerAndVerifyUnlockKey(byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
        SqrlAccount result = findByField(SqrlAccount_.sqrlIdentityKey, identityKey);
        if (result == null) return false;
        return doUpdateSqrlKeys(result, identityKey, serverUnlockKey, verifyUnlockKey);
    }

    /**
     * Update sqrl in db.
     *
     * @param target          the target
     * @param identityKey     the identity key
     * @param serverUnlockKey the server unlock key
     * @param verifyUnlockKey the verify unlock key
     * @return true, if successful
     */
    private boolean doUpdateSqrlKeys(SqrlAccount target, byte[] identityKey, byte[] serverUnlockKey, byte[] verifyUnlockKey) {
    	target.setSqrlIdentityKey(identityKey);
    	target.setSqrlServerUnlockKey(serverUnlockKey);
    	target.setSqrlVerifyUnlockKey(verifyUnlockKey);
    	em.merge(target);
        return true;
    }
    
    @Override
    public String getIdpAccountId(String accountId) {
        SqrlAccount account = em.find(SqrlAccount.class, accountId);
        if (account == null)return null;
        return account.getIdpAccountId();
    }
}
