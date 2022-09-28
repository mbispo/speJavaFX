package pontoeletronico.jobs;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.jus.tjms.pontoeletronico.to.*;
import pontoeletronico.bean.Digital;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.ConfiguracaoService;
import pontoeletronico.service.DigitalServiceLocal;
import pontoeletronico.service.FuncionarioServiceLocal;
import pontoeletronico.service.ParametroService;
import pontoeletronico.tipo.TipoOperacao;
import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.Utils;

/**
 *
 * @author marcosbispo
 */
public class ReceberDigitaisJob implements Job {

    private static ReceberDigitaisJob instancia;

    public ReceberDigitaisJob() {
    }

    /**
     * Chama o serviço remoto para obter a lista dos funcionários cujas digitais foram cadastradas/alteradas em até ndias atrás e<br>
     * Atualiza as digitais destes funcionários localmente
     * @author marcosbispo
     * @param context
     * @throws org.quartz.JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String msg;
        
        if (context != null) {
            msg = "Iniciando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Iniciando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, ReceberDigitaisJob.class.getName(), "execute");
        
        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();        
        ParametroService  parametroService = ServiceLocalFactory.getNewParametroService(em);
        ConfiguracaoService configuracaoService = ServiceLocalFactory.getNewConfiguracaoService(em);
        FuncionarioServiceLocal funcionarioService = ServiceLocalFactory.getNewFuncionarioService(em);
        DigitalServiceLocal digitalService = ServiceLocalFactory.getNewDigitalService(em);
        
        try {
            
            int ndias = Constantes.NDIAS_ENVIO_DIGITAIS_DEFAULT;
            
            try {
                ndias = parametroService.getParametros().getDiasEnvioDigitais();
                if (ndias > Constantes.NDIAS_ENVIO_DIGITAIS_DEFAULT) {
                    ndias = Constantes.NDIAS_ENVIO_DIGITAIS_DEFAULT;
                }
            } catch (Exception e) {
                ndias = Constantes.NDIAS_ENVIO_DIGITAIS_DEFAULT;
                LogMachine.getInstancia().logWarning("Não foi possível obter o parâmetro nº de dias para envio de digitais, usando valor padrao = "+ndias, this.getClass().getName(), "execute");
            }

            int codigoComarcaSecretaria;
            int codigoInstancia;
            
            try {                
                if (!configuracaoService.isConfigurado()) {
                    throw new Exception("Configuração \"Código da comarca/secretaria\" não definida!");
                }                
                codigoComarcaSecretaria = configuracaoService.getConfiguracao().getCodigoComarcaSecretaria();
                codigoInstancia = configuracaoService.getConfiguracao().getCodigoInstancia();
            } catch (Exception e) {
                LogMachine.getInstancia().logWarning("Não foi possível obter o parâmetro \"Código da comarca/secretaria\": "+e.getMessage(), this.getClass().getName(), "execute");
                throw new Exception(e.getMessage());
            }
            
            int ndigitais = 0;
            List<FuncionarioTO> lista = null;

            // obtem a lista dos funcionarios remotos com digitais alteradas/cadastradas recentemente (lotados na comarca/secretaria ou instancia especificada)
            
            // verifico se é segunda instancia, se for, obtem as novas digitais por instancia, senão obtem por codigo da comarca
            if (codigoInstancia == 2) {
                lista = digitalService.buscarFuncionariosComNovasDigitaisPorInstancia(ndias, codigoInstancia);
                LogMachine.getInstancia().logInfo("digitalService.buscarFuncionariosComNovasDigitaisPorInstancia(ndias="+ndias+", codigoInstancia="+codigoInstancia+");", this.getClass().getName(), "execute");
            } else {
                lista = digitalService.buscarFuncionariosComNovasDigitaisPorLotacao(ndias, codigoComarcaSecretaria);
                LogMachine.getInstancia().logInfo("digitalService.buscarFuncionariosComNovasDigitaisPorLotacao(ndias="+ndias+", codigoComarcaSecretaria="+codigoComarcaSecretaria+");", this.getClass().getName(), "execute");
            }
            
            // atualiza localmente
            if (lista != null) {
                for (FuncionarioTO funcTO : lista) {
                    
                    try {

                        Funcionario f = funcionarioService.buscarPorId(funcTO.getMatricula());

                        if ((f == null)||((f!=null)&&(f.getId() == 0))) {
                            // funcionário não existe localmente, devemos então cria-lo...
                            f = new Funcionario(funcTO);
                            String senha = funcionarioService.buscarRemotoSenhaIntranet(f.getId(), Constantes.EMPRESA_DEFAULT);
                            f.setSenhaintranet(senha);
                            if (senha == null) {
                                LogMachine.getInstancia().logErro("Senha da intranet do funcionário "+f.toString()+" é nula...", ReceberDigitaisJob.class.getName(), "execute");
                            }
                            f.setDigitais(new ArrayList<Digital>());
                            funcionarioService.salvar(f);
                        } else {
                            // funcionário existe localmente, removemos as digitais atuais (não remove as que ainda não foram enviadas)
                            if (f.getDigitais().size() > 0) {
                                funcionarioService.removerDigitais(f);
                                funcionarioService.refresh(f);
                            }
                        }

                        List<DigitalTO> digitaisDoFuncionario = ServiceRemoteFactory.getDigitaServiceRemoto().buscarDigitaisPorFuncionario(funcTO);
                        // adicionamos as digitais retornadas pelo serviço remoto ao funcionário atual (se houverem digitais)
                        if ((digitaisDoFuncionario != null) && (digitaisDoFuncionario.size() > 0)) {
                            for (DigitalTO digitalTO : digitaisDoFuncionario) {
                                Digital d = new Digital(digitalTO.getDataCriacao(), digitalTO.getDataModificacao(), digitalTO.getImagem(), digitalTO.getImagemProcessada(), f, true);
                                f.getDigitais().add(d);
                                ndigitais++;
                            }
                        }

                        // salva o funcionario, atualizando as digitais
                        funcionarioService.atualizar(f);
                    } catch (Exception e) {
                        LogMachine.getInstancia().logErro("Falha ao receber digitais do funcionário "+funcTO.getMatricula()+"-"+funcTO.getNome()+": "+e.getMessage(), ReceberDigitaisJob.class.getName(), "execute");
                    }
                }
            }
            
            if (context != null) {
                msg = "Executando job "+context.getJobDetail().getFullName()+": "+ndigitais+" obtidas.";
            } else {
                msg = "Executando job "+this.getClass().getName()+": "+ndigitais+" obtidas.";
            }
            
            LogMachine.getInstancia().logInfo(msg, ReceberDigitaisJob.class.getName(), "execute");
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
                parametroService = null;
                configuracaoService = null;
                funcionarioService = null;
                digitalService = null;
                em.clear();
                em.close();
                em = null;
            }  
            
            Utils.limpaLista(lista);
            
        } catch (Exception e) {
            e.printStackTrace();
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, ReceberDigitaisJob.class.getName(), "execute", null, null, null, TipoOperacao.ERRORECEBIMENTODIGITAIS);
        
            if ((em != null)&&(em.isOpen())) {
                em.clear();
                em.close();
            }            
            
            throw new JobExecutionException(e.getMessage());            
        }
        
        if (context != null) {
            msg = "Finalizando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Finalizando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, ReceberDigitaisJob.class.getName(), "execute");
    }

    public static ReceberDigitaisJob getNovaInstancia() {
        return new ReceberDigitaisJob();
    }

    public static ReceberDigitaisJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
    
}