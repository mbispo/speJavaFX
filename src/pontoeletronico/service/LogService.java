package pontoeletronico.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.pontoeletronico.bean.LogEjb;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Log;
import pontoeletronico.dao.LogDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.log.TipoLog;
import pontoeletronico.tipo.TipoOperacao;
import br.jus.tjms.pontoeletronico.client.Constantes;
import java.util.Date;
import pontoeletronico.util.Utils;

/**
 *
 * @author marcosbispo
 */
public class LogService  {

    private LogDao dao;
    
    public LogService() {
        dao = DaoFactory.getLogDao();
    }
    
    public LogService(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewLogDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }
    
    public void criarLog(TipoLog tipo, String msg, String classe, String metodo, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) throws ServiceException {
        
        try {
            Log log = new Log();        
            log.setClasse(classe);
            log.setMetodo(metodo);
            log.setMensagem(msg);        
            log.setDatahora(RelogioJobService.getInstancia().getDataHora());            
            log.setDatahoraLocal(new Date());
            log.setEnviado(false);
            log.setFuncionario(funcionario);        
            log.setTipo(tipo);
            
            log.setEmpresaAdministrador(empresaAdministrador);
            log.setMatriculaAdministrador(matriculaAdministrador);
            log.setTipoOperacao(tipoOperacao);
            
            Integer codigoRdz = null;
            Integer codigoAto = null;
            
            ConfiguracaoService configuracaoService = ServiceLocalFactory.getNewConfiguracaoService(getEntityManager());
            
            if (configuracaoService.getConfiguracao() != null) {
                codigoRdz = configuracaoService.getConfiguracao().getCodigoReduzidoOrganograma();
                codigoAto = configuracaoService.getConfiguracao().getCodigoAto();
            }
            
            log.setCodigoReduzidoOrganograma(codigoRdz);
            log.setCodigoAto(codigoAto);

            salvar(log);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof DaoException) {
                throw new ServiceException(e);
            } else {
                LogMachine.getInstancia().logErro("Não foi possível criar registro de log no banco de dados: "+msg+"\nClasse: "+classe+"\nMétodo: "+metodo+"\nFuncionário: "+funcionario+"\nErro reportado: "+e.getMessage(), this.getClass().getSimpleName(), "criarLog");
            }
        }
        
    }
    
    public void salvar(Log log) throws ServiceException {        
        try {
            dao.salvar(log);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    public void atualizar(Log log) throws ServiceException {        
        try {
            dao.atualizar(log);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public List<Log> buscarTodos() throws ServiceException {
        List<Log> logs = new ArrayList<Log>();

        try {
            logs = dao.buscarTodos();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
        return logs;        
    }

    public Log buscarPorId(Integer id) throws ServiceException {        
        Log log;
        
        try {
            log = dao.buscarPorId(id);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
        return log;
    }

    public void remover(Log log) throws ServiceException {
        try {
            dao.remover(log);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public List<Log> buscarNaoEnviados() throws ServiceException {
        
        List<Log> logs = new ArrayList<Log>();

        try {
            logs = dao.buscarNaoEnviados();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
        return logs;        
    }

    public void limparLogsEnviados() throws ServiceException {
        try {
            dao.limparLogsEnviados();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public boolean gravarRemoto(Log log) throws ServiceException {
        
        try {
            
            LogEjb logejb = new LogEjb();

            logejb.setDatahora(log.getDatahora());
            logejb.setMensagem(log.getMensagem());
            logejb.setTipo(log.getTipo().toString());            
            logejb.setClasse(log.getClasse());
            logejb.setMetodo(log.getMetodo());
            
            logejb.setMatricula(log.getFuncionario().getId());
            logejb.setEmpresa(Constantes.EMPRESA_DEFAULT);
            
            logejb.setMatriculaAdministrador(log.getMatriculaAdministrador());
            logejb.setEmpresaAdministrador(log.getEmpresaAdministrador());
            
            logejb.setTipoOperacao(log.getTipoOperacao().toString());
            logejb.setCodigoReduzidoOrganograma(log.getCodigoReduzidoOrganograma());
            logejb.setCodigoAto(log.getCodigoAto());
            
            boolean retorno = ServiceRemoteFactory.getLogServiceRemoto().gravar(logejb);           
            return retorno;
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível enviar registro de log para o servidor!");
        }

    }

    public boolean gravarRemoto(List<Log> logs) throws ServiceException {
        try {
            
            List<LogEjb> logsEjb = new ArrayList<LogEjb>();

            for (Log log : logs) {
                
                LogEjb logejb = new LogEjb();

                logejb.setDatahora(log.getDatahora());
                logejb.setMensagem(log.getMensagem());
                
                logejb.setTipo(log.getTipo().toString());
                logejb.setTipoOperacao(log.getTipoOperacao().toString());
                
                logejb.setClasse(log.getClasse());
                logejb.setMetodo(log.getMetodo());

                if (log.getFuncionario() != null) {
                    logejb.setMatricula(log.getFuncionario().getId());
                    logejb.setEmpresa(Constantes.EMPRESA_DEFAULT);
                } else {
                    logejb.setMatricula(null);
                    logejb.setEmpresa(null);
                }

                logejb.setMatriculaAdministrador(log.getMatriculaAdministrador());
                logejb.setEmpresaAdministrador(log.getEmpresaAdministrador());

                                
                logejb.setCodigoReduzidoOrganograma(log.getCodigoReduzidoOrganograma());
                logejb.setCodigoAto(log.getCodigoAto());
                
                logsEjb.add(logejb);

            }
            
            boolean retorno = ServiceRemoteFactory.getLogServiceRemoto().gravar(logsEjb);
            
            Utils.limpaLista(logsEjb);
            
            return retorno;
        } catch (Exception ex) {
            throw new ServiceException("Não foi possível enviar registro de log para o servidor!");
        }
    }
}
