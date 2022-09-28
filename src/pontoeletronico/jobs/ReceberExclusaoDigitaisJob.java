package pontoeletronico.jobs;

import java.util.List;

import javax.persistence.EntityManager;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.ConfiguracaoService ;
import pontoeletronico.service.DigitalServiceLocal ;
import pontoeletronico.service.FuncionarioServiceLocal ;
import pontoeletronico.util.Utils;

/**
 *
 * @author marcosbispo
 */
public class ReceberExclusaoDigitaisJob implements Job {

    private static ReceberExclusaoDigitaisJob instancia;

    public ReceberExclusaoDigitaisJob() {
    }

    /**
     * Chama o serviço remoto para obter a lista dos funcionários cujas digitais foram apagadas e<br>
     * apaga as digitais destes funcionários localmente
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
        
        LogMachine.getInstancia().logInfo(msg, ReceberExclusaoDigitaisJob.class.getName(), "execute");
        
        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();        
        ConfiguracaoService  configuracaoService = ServiceLocalFactory.getNewConfiguracaoService(em);
        FuncionarioServiceLocal  funcionarioService = ServiceLocalFactory.getNewFuncionarioService(em);
        DigitalServiceLocal  digitalService = ServiceLocalFactory.getNewDigitalService(em);
        
        try {
            
            int codigoComarcaSecretaria;
            
            try {                
                if (!configuracaoService.isConfigurado()) {
                    throw new Exception("Configuração \"Código da comarca/secretaria\" não definida!");
                }                
                codigoComarcaSecretaria = configuracaoService.getConfiguracao().getCodigoComarcaSecretaria();
            } catch (Exception e) {
                LogMachine.getInstancia().logWarning("Não foi possível obter a configuração \"Código da comarca/secretaria\": "+e.getMessage(), this.getClass().getName(), "execute");
                throw new Exception(e.getMessage());
            }
            
            // obtem a lista dos funcionarios remotos sem digitais (lotados na comarca/secretaria especificada)
            List<FuncionarioTO> lista = digitalService.buscarFuncionariosSemDigitaisPorLotacao(codigoComarcaSecretaria);
            
            int nfuncdigremov = 0;
            int nfuncremov = 0;
            
            // atualiza localmente (remove as digitais e os funcionários)
            if ((lista != null) && (lista.size() > 0)) {
                for (FuncionarioTO funcTO : lista) {

                    Funcionario f = funcionarioService.buscarPorId(funcTO.getMatricula());

                    if ((f != null)) {
                        // funcionário existe localmente, removemos as digitais atuais (não remove as que ainda não foram enviadas)
                        if ((f.getDigitais() != null)&&(f.getDigitais().size() > 0)) {
                            funcionarioService.removerDigitais(f);
                            funcionarioService.refresh(f);
                            nfuncdigremov++;
                        }
                        
                        // se não sobraram digitais a enviar, então apagamos o funcionario também...
                        // não apaga o funcionário se ele for isento de digital...
                        if ((f.getDigitais() == null) || (f.getDigitais().size() == 0)) {
                            funcionarioService.salvar(f);                            
                            if (!f.getIsentadigital()) {
                                if (funcionarioService.podeExcluir(f)) {
                                    funcionarioService.remover(f);
                                    nfuncremov++;
                                }
                            }
                        }
                    }
                }
            }
            
            if (context != null) {
                msg = "Executando job "+context.getJobDetail().getFullName()+": "+nfuncdigremov+" funcionários tiveram suas digitais removidas, desses "+nfuncremov+" foram removidos.";
            } else {
                msg = "Executando job "+this.getClass().getName()+": "+nfuncdigremov+" funcionários tiveram suas digitais removidas, desses "+nfuncremov+" foram removidos.";
            }
            
            LogMachine.getInstancia().logInfo(msg, ReceberExclusaoDigitaisJob.class.getName(), "execute");
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
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
            
            LogMachine.getInstancia().logErro(msg, ReceberExclusaoDigitaisJob.class.getName(), "execute");
            
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
        
        LogMachine.getInstancia().logInfo(msg, ReceberExclusaoDigitaisJob.class.getName(), "execute");
    }

    public static ReceberExclusaoDigitaisJob getNovaInstancia() {
        return new ReceberExclusaoDigitaisJob();
    }

    public static ReceberExclusaoDigitaisJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
}
