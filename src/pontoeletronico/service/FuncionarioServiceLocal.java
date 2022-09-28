package pontoeletronico.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.dao.FuncionarioDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;

/**
 *
 * autor: DDSI
 */
public class FuncionarioServiceLocal  {

    //Objeto Dao da Classe FuncionarioServiceImpl utilizado para realizar
    //os servicos de persistencia do bean Funcionario
    FuncionarioDao dao;

    public FuncionarioServiceLocal() {
        //seta o dao que a implementacao irá utilizar
        this.dao = DaoFactory.getFuncionarioDao();
    }
    
    public FuncionarioServiceLocal(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewFuncionarioDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }    

    //metodo para obter todos os funcionario
    public List<Funcionario> buscarTodos() throws ServiceException {
        List<Funcionario> funcionarios = new ArrayList<Funcionario>();

        try {
            funcionarios = dao.buscarTodos();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        return funcionarios;
    }

    public void salvar(Funcionario funcionario) throws ServiceException {
        try {
            dao.salvar(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public void atualizar(Funcionario funcionario) throws ServiceException {
        try {
            dao.atualizar(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public void refresh(Funcionario funcionario) throws ServiceException {
        try {
            dao.refresh(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    public void remover(Funcionario funcionario) throws ServiceException {
        try {
            dao.remover(funcionario);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    public void removerDigitais(Funcionario funcionario) throws ServiceException {
        try {
            dao.removerDigitais(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    // marca todas as digitais do funcionarios como não enviadas e atualiza a data de modificação, forçando o reenvio na próxima sincronização
    public void redefinirEnvioDigitais(Funcionario funcionario) throws ServiceException {
        try {
            dao.redefinirEnvioDigitais(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }        
    }

    public boolean podeExcluir(Funcionario funcionario) throws ServiceException {
        try {
            return dao.podeExcluir(funcionario);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    public Funcionario buscarPorId(int id) throws ServiceException {
        Funcionario funcionario = null;

        try {
            funcionario = dao.buscarPorId(id);
        } catch (DaoException ex) {
            ex.printStackTrace();
            throw new ServiceException(ex);
        }
        return funcionario;
    }

    // métodos para busca via ejb/webservice
    public FuncionarioTO buscarRemotoPorId(int matricula, int empresa) throws ServiceException {
        try {

            FuncionarioTO uoFnc = ServiceRemoteFactory.getFuncionarioServiceRemoto().buscarPorId(matricula, empresa);

            return uoFnc;
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível consultar a matrícula no banco de dados!");
        }

    }

    /**
     * @author marcosbispo
     * @param matricula
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public FuncionarioTO buscarRemotoFuncionarioPorId(int matricula) throws ServiceException {
        try {

            FuncionarioTO uoFnc = ServiceRemoteFactory.getFuncionarioServiceRemoto().buscarPorId(matricula);

            return uoFnc;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServiceException("Não foi possível consultar a matrícula no banco de dados!");
        }

    }

    /**
     * @author marcosbispo
     * @param matricula
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public FuncionarioTO buscarRemotoMagistradoPorId(int matricula) throws ServiceException {
        try {

           FuncionarioTO uoFnc = ServiceRemoteFactory.getFuncionarioServiceRemoto().buscarPorId(matricula, 2);

            return uoFnc;
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível consultar a matrícula no banco de dados!");
        }
    }

    /**
     * @author marcosbispo
     * @param nome
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.ServiceException
     */
    public List<FuncionarioTO> buscarRemotoPorNome(String nome) throws ServiceException {
        try {

            List<FuncionarioTO> uoFncs = ServiceRemoteFactory.getFuncionarioServiceRemoto().buscarPorNome(nome);

            return uoFncs;
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível consultar a matrícula no banco de dados!");
        }
    }
    
    /**
     * Retorna a senha da intranet do funcionario via ejb
     * @param matricula
     * @param empresa
     * @return
     * @throws br.jus.tjms.comuns.exceptions.ServiceException
     */
    public String buscarRemotoSenhaIntranet(int matricula, int empresa) throws ServiceException {
        try {
            return ServiceRemoteFactory.getFuncionarioServiceRemoto().buscarSenhaIntranet(matricula, empresa);            
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível consultar a matrícula no banco de dados!");
        }
    }
}