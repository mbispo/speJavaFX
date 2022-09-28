package pontoeletronico.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.pontoeletronico.bean.FuncionarioEjb;
import br.jus.tjms.pontoeletronico.bean.PontoEjb;
import br.jus.tjms.pontoeletronico.service.PontoServiceRemoto;
import pontoeletronico.bean.Digital;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Ponto;
import pontoeletronico.dao.PontoDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.tipo.SituacaoPonto;
import br.jus.tjms.pontoeletronico.client.Constantes;
import java.util.Date;
import pontoeletronico.controller.PrincipalController;

import pontoeletronico.util.Utils;

/**
 * autor: DDSI
 */
public class PontoServiceLocal  {

    private PontoDao dao;
    // contexto EJB
    private PontoServiceRemoto pontoServiceRest;
    
    public static long ultimaDataHoraRegistro = new Date().getTime();

    public PontoServiceLocal() {
        this.dao = DaoFactory.getPontoDao();
    }

    public PontoServiceLocal(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewPontoDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }    

    //regra de negócio relativa ao registro de ponto do funcionário
    public Ponto registraPonto(int matricula, Digital digitalInformada, Object comparador, boolean validarDigital) throws ServiceException {
        // verifica se o relógio travou em relação ao último registro de ponto feito pelo usuário...
        if (RelogioJobService.getInstancia().getDataHora().getTime() == ultimaDataHoraRegistro) {
            
            try {
                PrincipalController.reiniciar(true, "Relógio travado!\n\nO sistema será reiniciado agora.\n\nAguarde, verifique o horário e tente fazer o registro de ponto novamente.\n\nPersistindo o problema, contacte o suporte técnico!");
                return null;
            } catch (Exception e) {
                LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "registraPonto");
            }             
        }
        
        // verifica se existe uma diferença maior que 5 minutos em relação ao relógio da máquina
        long dif = RelogioJobService.getInstancia().getDataHora().getTime() - (new Date().getTime());
        
        if (Math.abs(dif) >= (Long.valueOf(Constantes.MINUTO)*5l)) {            
            try {
                PrincipalController.reiniciar(true, "Relógio inconsistente!\n\nO sistema será reiniciado agora.\n\nAguarde, verifique o horário e tente fazer o registro de ponto novamente.\n\nPersistindo o problema, contacte o suporte técnico!");
                return null;
            } catch (Exception e) {
                LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "registraPonto");
            }             
        }
        
        boolean validou = false;
        
        List<Digital> digitaisCadastradas = null;
        Ponto ponto = null;
        Funcionario funcionario = null;     
        
        FuncionarioServiceLocal funcionarioService = ServiceLocalFactory.getNewFuncionarioService(getEntityManager());

        //buscar o funcionario correspondente a matricula informada
        funcionario = funcionarioService.buscarPorId(matricula);
        
        //se encontrou um funcionario com a matricula informada
        if (funcionario != null) {
            
            funcionarioService.refresh(funcionario);

            if (validarDigital) {
                
                //obter digitais do funcionario;
                digitaisCadastradas = funcionario.getDigitais();

                //verifica se o funcionario possui digitais cadastradas
                if (digitaisCadastradas.size() == 0) {
                    throw new ServiceException("Digitais não cadastradas!");
                }

                //para cada digital cadastrada do funcionário, comparar com
                //a que ele informou no registro do seu ponto, retornando 
                //verdadeiro se elas são compatíveis e falso caso contrário.
                for (Digital digitalCadastrada : digitaisCadastradas) {

                    //solicita servico de leitora para comparar as digitais
                    validou = ServiceLocalFactory.getLeitoraService().comparaDigital(digitalInformada, digitalCadastrada, comparador);

                    //se a digital é do funcionario, entao persistir o ponto
                    if (validou) {
                        //sai do laço
                        break;
                    }//end if (validou)
                }//end for            

            } else {
                validou = true;
            }
            
            Utils.limpaLista(digitaisCadastradas);

            if (validou) {
                
                //criar um objeto ponto para persistir
                ponto = new Ponto();

                //seta o horário
                ponto.setDataHora(RelogioJobService.getInstancia().getDataHora());
                
                ponto.setDataHoraLocal(new Date());
                
                ultimaDataHoraRegistro = ponto.getDataHora().getTime();
                        
                //funcionário que registrou o ponto
                ponto.setFuncionario(funcionario);

                //seta a situacao do ponto como "nao enviada" 
                //para o banco de horas do SGP. 
                ponto.setSituacao(SituacaoPonto.NAO_ENVIADO);

                //seta onde o ponto está sendo registrado
                Integer localidade = null;
                
                ConfiguracaoService configuracaoService = ServiceLocalFactory.getNewConfiguracaoService(getEntityManager());
                
                if (configuracaoService.getConfiguracao() != null) {                
                    localidade = configuracaoService.getConfiguracao().getCodigoLocalidade();
                }
                
                if (localidade != null) {
                    ponto.setLocalidade(localidade);
                } else {
                    ponto.setLocalidade(Integer.valueOf(Constantes.LOCALIDADE_DEFAULT));
                }

                ponto.setHorarioVerao(RelogioJobService.getInstancia().getHorarioDeVerao());
                ponto.setTimezone(RelogioJobService.getInstancia().getTimezone());
                
                //persistir o objeto ponto no banco de dados
                salvar(ponto);
                
            } else {
                throw new ServiceException("Digital inválida!");
            }      
        
        }//end if (funcionario != null)   

        return ponto;
    }

    public void salvar(Ponto ponto) throws ServiceException {
        try {
            dao.salvar(ponto);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    public List<Ponto> buscarTodos() throws ServiceException {
        List<Ponto> pontos = new ArrayList<Ponto>();

        try {
            pontos = dao.buscarTodos();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        return pontos;
    }

    public void limparPontosEnviados() throws ServiceException {
        try {
            dao.limparPontosEnviados();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }

    public boolean processarPontoRemoto() throws Exception {
        List<Ponto> listaPontos = null;
        List<PontoEjb> listaPontosEjb = new ArrayList<PontoEjb>();

        try {
            pontoServiceRest = ServiceRemoteFactory.getPontoServiceRemoto();

            //busca os pontos nao enviados no banco de dados local
            listaPontos = this.buscarPontosNaoEnviados();

            String msg;
            
            if ((listaPontos != null) && (listaPontos.size() > 0)) {
                //criar uma lista de pontos Ejb para o servidor de aplicacao
                for (Ponto ponto : listaPontos) {

                    PontoEjb pontoEjb = new PontoEjb();
                    FuncionarioEjb f = new FuncionarioEjb();
                    f.setId(ponto.getFuncionario().getId());
                    f.setEmpresa(Constantes.EMPRESA_DEFAULT);
                    f.setNome(ponto.getFuncionario().getNome());
                    pontoEjb.setFuncionarioEjb(f);
                    pontoEjb.setDataHora(ponto.getDataHora());
                    pontoEjb.setDataHoraLocal(ponto.getDataHoraLocal());
                    pontoEjb.setLocalidade(ponto.getLocalidade());
                    pontoEjb.setTimeZone(ponto.getTimezone());
                    pontoEjb.setHorarioVerao(ponto.isHorarioVerao());
                    listaPontosEjb.add(pontoEjb);
                }

                pontoServiceRest.processarPonto(listaPontosEjb);
                
                setaPontosComoEnviados(listaPontos);

                msg = listaPontos.size()+ " registro(s) de ponto enviado(s)";
            } else {
                msg = "Nao há registro(s) de ponto para enviar.";
            }
            
            LogMachine.getInstancia().logInfo(msg, PontoServiceLocal.class.getName(), "processarPontoRemoto");
            
            Utils.limpaLista(listaPontos);
            Utils.limpaLista(listaPontosEjb);

            return true;

        } catch (Exception ex) {
            Utils.limpaLista(listaPontos);
            Utils.limpaLista(listaPontosEjb);            
            ex.printStackTrace();
            throw new Exception("Erro ao processar ponto remoto: "+ex.getMessage());
        }
    }

    public List<Ponto> buscarPontosNaoEnviados() throws ServiceException {

        try {
            return dao.buscarPontosNaoEnviados();
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
    }
    
    private void setaPontosComoEnviados(List<Ponto> listaPontos) throws ServiceException{
        
        for (Ponto ponto : listaPontos){
            try {
                ponto.setSituacao(SituacaoPonto.ENVIADO);
                dao.atualizar(ponto);
            } catch (DaoException ex) {
                throw new ServiceException(ex);
            }
        }
        
    }
    
}
