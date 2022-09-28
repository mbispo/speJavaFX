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
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.DigitalServiceLocal;
import pontoeletronico.tipo.TipoOperacao;
import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.Utils;

/**
 *
 * @author marcosbispo
 */
public class EnviarTodasDigitaisJob implements Job {

    private static EnviarTodasDigitaisJob instancia;

    public EnviarTodasDigitaisJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String msg;
        if (context != null) {
            msg = "Iniciando job " + context.getJobDetail().getFullName();
        } else {
            msg = "Iniciando job " + this.getClass().getName();
        }

        LogMachine.getInstancia().logInfo(msg, EnviarTodasDigitaisJob.class.getName(), "execute");

        List<Digital> listadigitais = null;
        List<Funcionario> listafnc = null;

        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();
        DigitalServiceLocal digitalService = ServiceLocalFactory.getNewDigitalService(em);

        try {

            int ndigitais = 0;

            // obtem todas as digitais
            listadigitais = digitalService.buscarTodos();

            if (listadigitais.size() > 0) {

                listafnc = new ArrayList<Funcionario>();

                for (Digital digital : listadigitais) {
                    if (listafnc.indexOf(digital.getFuncionario()) == -1) {
                        listafnc.add(digital.getFuncionario());
                    }
                }

                // monta lista de fncs com suas digitais ...
                List<FuncionarioTO> listafncTO = new ArrayList<FuncionarioTO>();

                // TODO gravar digitais de um funcionário por vez
                for (Funcionario funcionariolocal : listafnc) {

                    FuncionarioTO funcTO = new FuncionarioTO();
                    funcTO.setEmpresa(Constantes.EMPRESA_DEFAULT);
                    funcTO.setMatricula(funcionariolocal.getId());
                    funcTO.setNome(funcionariolocal.getNome());
                    funcTO.setDigitais(new ArrayList<DigitalTO>());

                    for (Digital digital : funcionariolocal.getDigitais()) {
                        DigitalTO digitalTO = new DigitalTO();
                        digitalTO.setDataCriacao(digital.getDataCriacao());
                        digitalTO.setDataModificacao(digital.getDataModificacao());
                        digitalTO.setImagem(digital.getImagem());
                        digitalTO.setImagemProcessada(digital.getImagemProcessada());
                        digitalTO.setMatricula(funcTO.getMatricula());
                        digitalTO.setEmpresa(funcTO.getEmpresa());
                        funcTO.getDigitais().add(digitalTO);
                        ndigitais++;
                    }

                    listafncTO.add(funcTO);
                }

                // envia para o serviço remoto
                digitalService.gravarRemotoPorFuncionario(listafncTO);

                // atualiza as digitais com status = enviada
                digitalService.definirTodasStatusEnviado();

                if (context != null) {
                    msg = "Executando job " + context.getJobDetail().getFullName()+ ": " + ndigitais + " enviadas.";
                } else {
                    msg = "Executando job " + this.getClass().getName() + ": " + ndigitais + " enviadas.";
                }

                LogMachine.getInstancia().logInfo(msg, EnviarTodasDigitaisJob.class.getName(), "execute");

                Utils.limpaLista(listafncTO);
            }

            if ((em != null) && (em.isOpen())) {
                em.clear();
                em.close();
            }

            Utils.limpaLista(listadigitais);
            Utils.limpaLista(listafnc);

        } catch (Exception e) {
            if (context != null) {
                msg = "Erro executando job " + context.getJobDetail().getFullName() + ": " + e.getMessage();
            } else {
                msg = "Erro executando job " + this.getClass().getName() + ": " + e.getMessage();
            }

            LogMachine.getInstancia().logErro(msg, EnviarTodasDigitaisJob.class.getName(), "execute", null, null, null, TipoOperacao.ERROENVIODIGITAIS);

            if ((em != null) && (em.isOpen())) {
                em.clear();
                em.close();
            }

            throw new JobExecutionException(e.getMessage());
        }

        if (context != null) {
            msg = "Finalizando job " + context.getJobDetail().getFullName();
        } else {
            msg = "Finalizando job " + this.getClass().getName();
        }

        LogMachine.getInstancia().logInfo(msg, EnviarTodasDigitaisJob.class.getName(), "execute");
    }

    public static EnviarTodasDigitaisJob getNovaInstancia() {
        return new EnviarTodasDigitaisJob();
    }

    public static EnviarTodasDigitaisJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
}
