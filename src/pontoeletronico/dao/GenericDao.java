package pontoeletronico.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;

import br.jus.tjms.comuns.exceptions.DaoException;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.log.LogMachine;

@SuppressWarnings("unchecked")
public abstract class GenericDao<T, ID extends Serializable>  {
    
    private Class<T> persistentClass;
    protected EntityManager em;

    public GenericDao() {
        try {
            setEntityManager(EntityManagerFactory.getEntityManager());
            this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "JpaGenericDaoImpl");
        }
    }
    
    public GenericDao(EntityManager newEntitymanager) {
        try {
            setEntityManager(newEntitymanager);
            this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "JpaGenericDaoImpl");
        }
    }

    public List<T> buscarTodos() throws DaoException {
        try {
            return em.createQuery("Select t from " + persistentClass.getSimpleName() + " t").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "buscarTodos");
            throw new DaoException("Não foi possível buscar!");
        }
    }

    public T buscarPorId(ID id) throws DaoException {
        try {
            return em.find(persistentClass, id);
        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "buscarPorId");
            throw new DaoException("Não foi possível buscar!");
        }
    }

    public synchronized T salvar(T entity) throws DaoException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(entity);
            if (em.getTransaction().isActive()){
                em.flush();
                em.getTransaction().commit();
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "salvar");
            throw new DaoException("Não foi possível salvar o(s) registro(s)!");
        }
    }

    public synchronized T atualizar(T entity) throws DaoException {
        try {
            if (!em.getTransaction().isActive()){
                em.getTransaction().begin();
            }
            em.merge(entity);            
            if (em.getTransaction().isActive()){
                em.flush();
                em.getTransaction().commit();
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }            
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "atualizar");
            throw new DaoException("Não foi possível salvar o(s) registro(s)!");
        }
    }
    
    public void refresh(T entity) throws DaoException {
        try {
            em.refresh(entity);
        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "refresh");
            throw new DaoException("Não foi possível atualizar o(s) registro(s)");
        }
    }

    public synchronized void remover(T entity) throws DaoException {
        try {            
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.remove(entity);
            if (em.getTransaction().isActive()){
                em.flush();
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logErro(e.getMessage(), GenericDao.class.getName(), "remover");
            throw new DaoException("Não foi possível excluir o registro!");
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    public EntityManager getEntityManager() {
        return this.em;
    }

}
