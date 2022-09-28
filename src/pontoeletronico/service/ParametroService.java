package pontoeletronico.service;

import java.util.List;

import javax.persistence.EntityManager;

import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.pontoeletronico.bean.ParametroEjb;
import pontoeletronico.bean.Parametro;
import pontoeletronico.dao.ParametroDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import br.jus.tjms.pontoeletronico.client.Constantes;


/**
 *
 * @author daren
 */
public class ParametroService  {

    ParametroDao dao;
    
    public ParametroService(){
        this.dao = DaoFactory.getParametroDao();
    }
    
    public ParametroService(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewParametroDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }    

    //retorna os parametros gravados localmente
    public Parametro getParametros() {
        try {
            return dao.buscarPorId(1);
        } catch (DaoException ex) {
            return null;
        }
    }
    
    public void setParametros(Parametro p) {

        p.setId(1);
        
        try{
            if (isParametrizado()){
                dao.atualizar(p);
            } else {
                dao.salvar(p);
            }
        } catch (DaoException ex) {
            ex.printStackTrace();
        }
    }       
    
    public void setParametros(String intervaloEnvioDigitais, String intervaloRecebimentoDigitais, String intervaloEnvioRegistroPonto, String intervaloEnvioRegistroOperacoes, int nivelToleranciaVerificacao, String intervaloSincronizacaoRelogio, int diasEnvioDigitais, String senhaMaster, String versaoBD) {

        Parametro p = new Parametro(intervaloEnvioDigitais, intervaloRecebimentoDigitais, intervaloEnvioRegistroPonto, intervaloEnvioRegistroOperacoes, nivelToleranciaVerificacao, intervaloSincronizacaoRelogio, diasEnvioDigitais, senhaMaster, versaoBD);
        p.setId(1);
        
        try{
            if (isParametrizado()){
                dao.atualizar(p);
            } else {
                dao.salvar(p);
            }
        } catch (DaoException ex) {
            ex.printStackTrace();
        }
    }    

    public void setParametros(ParametroEjb parametro) {
        Parametro p = new Parametro();
        p.setId(parametro.getId());
        p.setIntervaloEnvioDigitais(parametro.getIntervaloEnvioDigitais());
        p.setIntervaloEnvioRegistroOperacoes(parametro.getIntervaloEnvioRegistroOperacoes());
        p.setIntervaloRecebimentoDigitais(parametro.getIntervaloRecebimentoDigitais());
        p.setIntervaloEnvioRegistroPonto(parametro.getIntervaloEnvioRegistroPonto());
        p.setNivelToleranciaVerificacao(parametro.getNivelToleranciaVerificacao());
        p.setIntervaloSincronizacaoRelogio(parametro.getIntervaloSincronizacaoRelogio());
        p.setDiasEnvioDigitais(parametro.getDiasEnvioDigitais());
        p.setSenhaMaster(parametro.getSenhaMaster());
        p.setVersaoBD(Constantes.CLIENTE_VERSAOBD);
        try{
            if (isParametrizado()){
                dao.atualizar(p);
            } else
            {
                dao.salvar(p);
            }
            } catch (DaoException ex) {
        }
    }

    public boolean isParametrizado() {
        try{
            List list = dao.buscarTodos();
            if (list.isEmpty()){
                return false;
            } else
            {
                return true;
            }
        } catch (DaoException ex) {
            return false;
        }
       
    }
    
    public void sincronizarParametros(){
        try {

            ParametroEjb p = ServiceRemoteFactory.getParametroServiceRemoto().getParametros();
            
            if (p != null) {
                this.setParametros(p);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    
    }

    public boolean isSenhaValida(String senha) {

        Parametro parametro = dao.buscarPorId(1);
        
        return (parametro!=null&&senha.equals(parametro.getSenhaMaster()));
        
    }
}