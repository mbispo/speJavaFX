package pontoeletronico.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.pontoeletronico.to.DigitalTO;
import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import pontoeletronico.bean.Digital;
import pontoeletronico.dao.DigitalDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import br.jus.tjms.pontoeletronico.client.Constantes;

/**
 *
 * @autor: DDSI
 */
public class DigitalServiceLocal  {

    DigitalDao dao;

    public DigitalServiceLocal() {
        this.dao = DaoFactory.getDigitalDao();
    }
    
    public DigitalServiceLocal(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewDigitalDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }    

    public void salvar(Digital digital) throws ServiceException {

        try {
            dao.salvar(digital);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    public void atualizar(Digital digital) throws ServiceException {
        try {
            dao.atualizar(digital);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }    

    //metodo para obter todas as digitais
    public List<Digital> buscarTodos() throws ServiceException {
        List<Digital> digitais = new ArrayList<Digital>();

        try {
            digitais = dao.buscarTodos();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        return digitais;
    }
    
    public Digital buscarPorId(int id) throws ServiceException {
        Digital digital = null;

        try {
            digital = dao.buscarPorId(id);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        return digital;
    }    

    public void remover(Digital digital) throws ServiceException {
        try {
            dao.remover(digital);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }

    }
    
    /**
     * @author marcosbispo
     * @param ndias
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public List<Digital> buscarNovas(int ndias) throws ServiceException {

        List<Digital> digitais = new ArrayList<Digital>();

        try {
            digitais = dao.buscarNovas(ndias);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
        return digitais;
    }    
    
    /**
     * Define o status de todas as digitais = enviado
     * @author marcosbispo
     * @throws br.jus.tjms.comuns.exceptions.ServiceException
     */
    public void definirTodasStatusEnviado() throws ServiceException {
        try {
            dao.definirTodasStatusEnviado();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    // contexto REST
    
    /**
     * @author marcosbispo
     * @param id
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public DigitalTO buscarRemotoPorId(int id) throws ServiceException {

        try {

            DigitalTO digital = ServiceRemoteFactory.getDigitaServiceRemoto().buscarPorId(id);

            return digital;
        } catch (Exception ex) {   
            ex.printStackTrace();
            throw new ServiceException("Não foi possível receber digitais do servidor!");
        }

    }

    /**
     * @author marcosbispo
     * @param funcionario
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public List<DigitalTO> buscarRemotoPorFuncionario(FuncionarioTO funcionario)  throws ServiceException {
        try {
        	
            FuncionarioTO f = new FuncionarioTO();
            f.setEmpresa(funcionario.getEmpresa());
            f.setMatricula(funcionario.getMatricula());
            List<DigitalTO> digitais = ServiceRemoteFactory.getDigitaServiceRemoto().buscarDigitaisPorFuncionario(f);

            return digitais;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível receber digitais do servidor!");
        }

    }
    
    /**
     * @author marcosbispo
     * @param ndias
     * @param codigoComarcaSecretaria
     * @return
     * @throws br.jus.tjms.comuns.exceptions.ServiceException
     */
    public List<FuncionarioTO> buscarFuncionariosComNovasDigitaisPorLotacao(int ndias, int codigoComarcaSecretaria) throws ServiceException {
        try {

            List<FuncionarioTO> lista = ServiceRemoteFactory.getDigitaServiceRemoto().buscarFuncionariosComNovasDigitaisPorLotacao(ndias,codigoComarcaSecretaria,Constantes.EMPRESA_DEFAULT);

            return lista;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível buscar!");
        }
    }

    /**
     * @author marcosbispo
     * @param ndias
     * @param codigoInstancia
     * @return
     * @throws br.jus.tjms.comuns.exceptions.ServiceException
     */
    public List<FuncionarioTO> buscarFuncionariosComNovasDigitaisPorInstancia(int ndias, int codigoInstancia) throws ServiceException {
        try {

            List<FuncionarioTO> lista = ServiceRemoteFactory.getDigitaServiceRemoto().buscarFuncionariosComNovasDigitaisPorInstancia(ndias,codigoInstancia,Constantes.EMPRESA_DEFAULT);

            return lista;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível buscar!");
        }
    }
    
    /**
     * @author marcosbispo
     * @param codigoComarcaSecretaria
     * @return
     * @throws br.jus.tjms.comuns.exceptions.ServiceException
     */
    public List<FuncionarioTO> buscarFuncionariosSemDigitaisPorLotacao(int codigoComarcaSecretaria) throws ServiceException {
        try {

            List<FuncionarioTO> lista = ServiceRemoteFactory.getDigitaServiceRemoto().buscarFuncionariosSemDigitaisPorLotacao(codigoComarcaSecretaria,Constantes.EMPRESA_DEFAULT);

            return lista;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível buscar!");
        }
    }
    

    /**
     * @author marcosbispo
     * @param digitais
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public boolean gravarRemoto(List<DigitalTO> digitais)  throws ServiceException {
        try {
            return ServiceRemoteFactory.getDigitaServiceRemoto().gravar(digitais);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível enviar digitais para o servidor!");
        }

    }

    /**
     * @author marcosbispo
     * @param fncs
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public boolean gravarRemotoPorFuncionario(List<FuncionarioTO> fncs)  throws ServiceException {
        try {
            return ServiceRemoteFactory.getDigitaServiceRemoto().gravarPorFuncionario(fncs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível enviar digitais para o servidor!");
        }
    }

    /**
     * @author marcosbispo
     * @param digital
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public boolean gravarRemoto(DigitalTO digital)  throws ServiceException {
        try {
            return ServiceRemoteFactory.getDigitaServiceRemoto().gravar(digital);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível enviar digitais para o servidor!");
        }
        
    }

}