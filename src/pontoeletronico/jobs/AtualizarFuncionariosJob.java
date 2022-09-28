package pontoeletronico.jobs;


import br.jus.tjms.pontoeletronico.client.Constantes;
import java.util.List;

import javax.persistence.EntityManager;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.jus.tjms.pontoeletronico.to.*;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.FuncionarioServiceLocal;

import pontoeletronico.util.Utils;

/**
 *
 * @author marcosbispo
 */
public class AtualizarFuncionariosJob implements Job {
    
    private static AtualizarFuncionariosJob instancia;

    public AtualizarFuncionariosJob() {
    }

    /**
     * Atualizar os dados de funcionarios localmente com dados do servidor
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
        
        LogMachine.getInstancia().logInfo(msg, AtualizarFuncionariosJob.class.getName(), "execute");
        
        List<Funcionario> listafnc = null;
        
        // aqui estamos usando um novo entitymanager, para que não haja conflito de transações com o entitymanager padrão
        EntityManager em = EntityManagerFactory.getNewEntityManager();
        FuncionarioServiceLocal funcionarioService = ServiceLocalFactory.getNewFuncionarioService(em);
        
        try {

            listafnc = funcionarioService.buscarTodos();
            
            if (listafnc.size() > 0) {

                for (Funcionario f : listafnc) {
                    try {
                        FuncionarioTO funcTO = funcionarioService.buscarRemotoPorId(f.getId(), Constantes.EMPRESA_DEFAULT);

                        f.setNome(funcTO.getNome());
                        f.setLotacao(funcTO.getLotacao()!=null?funcTO.getLotacao():"");
                        f.setIsentadigital(funcTO.getIsentaDigital());
                        String senha = funcionarioService.buscarRemotoSenhaIntranet(f.getId(), Constantes.EMPRESA_DEFAULT);
                        if (senha == null) {
                            LogMachine.getInstancia().logErro("Senha da intranet do funcionário "+f.toString()+" é nula...", AtualizarFuncionariosJob.class.getName(), "execute");
                        }
                        f.setSenhaintranet(senha);
                        funcionarioService.atualizar(f);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogMachine.getInstancia().logErro("Falha ao atualizar dados do funcionário "+f.toString()+": "+e.getMessage(), AtualizarFuncionariosJob.class.getName(), "execute");
                    }
                }

                if (context != null) {
                    msg = "Executando job "+context.getJobDetail().getFullName()+": "+listafnc.size()+" funcionários atualizados.";
                } else {
                    msg = "Executando job "+this.getClass().getName()+": "+listafnc.size()+" funcionários atualizados.";
                }
                
                LogMachine.getInstancia().logInfo(msg, AtualizarFuncionariosJob.class.getName(), "execute");
                
            }            
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
                funcionarioService = null;
                em.clear();
                em.close();
                em = null;
            }
            
            Utils.limpaLista(listafnc);
            
        } catch (Exception e) {
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, AtualizarFuncionariosJob.class.getName(), "execute");
            
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
        
        LogMachine.getInstancia().logInfo(msg, AtualizarFuncionariosJob.class.getName(), "execute");
    }
    
    public static AtualizarFuncionariosJob getNovaInstancia() {        
        return new AtualizarFuncionariosJob();        
    }
    
    public static AtualizarFuncionariosJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
    
    public static void main(String[] args) {
    	
        EntityManager em = EntityManagerFactory.getNewEntityManager();
        FuncionarioServiceLocal funcionarioService = ServiceLocalFactory.getNewFuncionarioService(em);
    	
        
        FuncionarioTO f = funcionarioService.buscarRemotoFuncionarioPorId(9330);
        
        
        System.out.println(f.getNome());
        System.out.println(f.toString());
        
        if (f.getDigitais()!=null) {
        	for (DigitalTO d : f.getDigitais()) {
        		System.out.println("\n\n");
        		System.out.println(d.getMatricula());
        		System.out.println(d.getImagem());
        		System.out.println(d.toString());
				
			}
        }
    	
    }

}
