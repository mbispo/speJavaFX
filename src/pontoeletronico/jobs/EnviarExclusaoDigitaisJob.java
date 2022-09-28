package pontoeletronico.jobs;

import pontoeletronico.bean.Funcionario;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.jus.tjms.pontoeletronico.to.DigitalTO;
import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.DigitalServiceLocal;
import pontoeletronico.service.FuncionarioServiceLocal;
import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.Utils;


/**
 *
 * @author marcosbispo
 */
public class EnviarExclusaoDigitaisJob implements Job {
    
    private static EnviarExclusaoDigitaisJob instancia;

    public EnviarExclusaoDigitaisJob() {
    }

    /**
     * Monta a lista dos funcionários que não têm digitais cadastradas localmente<br>
     * Chama servico gravarRemotoPorFuncionario passando a lista de funcionarios sem digitais,
     * fazendo com que as digitais do funcionario sejam apagadas remotamente
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
        
        LogMachine.getInstancia().logInfo(msg, EnviarExclusaoDigitaisJob.class.getName(), "execute");
        
        List<Funcionario> listafnc = null;
        
        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();        
        FuncionarioServiceLocal funcionarioService = ServiceLocalFactory.getNewFuncionarioService(em);
        DigitalServiceLocal digitalService = ServiceLocalFactory.getNewDigitalService(em);        
        
        try {
            
            listafnc = funcionarioService.buscarTodos();
            
            if (listafnc.size() > 0) {

                // monta lista de fncs sem digitais ...
                List<FuncionarioTO> listafncTO = new ArrayList<FuncionarioTO>();
                
                for (Funcionario funcionariolocal : listafnc) {
                    if ((funcionariolocal.getDigitais() == null) || (funcionariolocal.getDigitais().size() == 0)) {
                    
                    	FuncionarioTO funcTO = new FuncionarioTO();

                        funcTO.setEmpresa(Constantes.EMPRESA_DEFAULT);
                        funcTO.setMatricula(funcionariolocal.getId());                
                        funcTO.setNome(funcionariolocal.getNome());
                        funcTO.setLotacao(funcionariolocal.getLotacao());
                        funcTO.setDigitais(new ArrayList<DigitalTO>());

                        listafncTO.add(funcTO);
                    }
                }
                
                if (listafncTO.size() > 0) {
                    // envia para o serviço remoto
                    digitalService.gravarRemotoPorFuncionario(listafncTO);
                    int nfuncremov = 0;
                    // apaga localmente os funcionários sem digitais (que não são isentos de digital)
                    for (FuncionarioTO func : listafncTO) {
                        Funcionario f = funcionarioService.buscarPorId(func.getMatricula());
                        
                        funcionarioService.refresh(f);
                        
                        if (!f.getIsentadigital()) {
                            if (funcionarioService.podeExcluir(f)) {
                                funcionarioService.remover(f);
                                nfuncremov++;
                            }
                        }
                    }

                    if (context != null) {
                        msg = "Executando job "+context.getJobDetail().getFullName()+": "+listafncTO.size()+" funcionários sem digitais enviados. Desses, "+nfuncremov+" foram removidos.";
                    } else {
                        msg = "Executando job "+this.getClass().getName()+": "+listafncTO.size()+" funcionários sem digitais enviados. Desses, "+nfuncremov+" foram removidos.";
                    }
                } else {
                    if (context != null) {
                        msg = "Executando job "+context.getJobDetail().getFullName()+": não há funcionários sem digitais.";
                    } else {
                        msg = "Executando job "+this.getClass().getName()+": não há funcionários sem digitais.";
                    }                    
                }
                
                LogMachine.getInstancia().logInfo(msg, EnviarExclusaoDigitaisJob.class.getName(), "execute");
                
                Utils.limpaLista(listafncTO);
            }
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
                funcionarioService = null;
                digitalService = null;
                em.clear();
                em.close();
                em = null;
            }         
            
            Utils.limpaLista(listafnc);
            
        } catch (Exception e) {            
            e.printStackTrace();
            
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, EnviarExclusaoDigitaisJob.class.getName(), "execute");

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
        
        LogMachine.getInstancia().logInfo(msg, EnviarExclusaoDigitaisJob.class.getName(), "execute");
    }
    
    public static EnviarExclusaoDigitaisJob getNovaInstancia() {        
        return new EnviarExclusaoDigitaisJob();        
    }
    
    public static EnviarExclusaoDigitaisJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }

}
