package pontoeletronico.jobs;

import br.jus.tjms.pontoeletronico.to.UpdateInfoTO;
import br.jus.tjms.pontoeletronico.to.UpdateScriptAcaoTO;
import br.jus.tjms.pontoeletronico.to.UpdateScriptTO;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.application.Platform;
import javax.persistence.EntityManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.log.LogMachine;
import br.jus.tjms.pontoeletronico.client.Constantes;

/**
 *
 * @author marcosbispo
 */
public class UpdateAppJob implements Job {
    
    private static UpdateAppJob instancia;

    public UpdateAppJob() {
        
    }
    
    public static UpdateAppJob getNovaInstancia() {        
        return new UpdateAppJob();        
    }
    
    public static UpdateAppJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }    
    
    /*
        jobatualizador pode ser disparado por botão;

        jobatualizador:

                executa rest getVersao, getNovidades, getURL, etc, obtendo versão;
                se getVersao.versao != null&& getVersao.versao > versao atual:
                        mostra aviso de atualização e novidades (loga apenas...);
                        obtem dados da url (update.zip*);
                        chama atualizador e fecha aplicação atual:
                                atualizador:
                                        delay 10 segundos;
                                        descompacta update.zip;
                                                executa updatescript.xml**: formato XML, seções executar, excluir, etc
                                                        -executa exclusões;
                                                        -executa executáveis (instalação de drivers, etc);
                                                        -executa sql
                                                        -etc
                                        chama aplicação principal novamente



        exemplo de estrutura do update.zip:
                updatescript.xml**
                file1
                file2
                folder1\file1
                folder1\file2

        exemplo de updatescript.xml:
        <xml>
                <incluir>
                        <registro chave="xyz" valor="etc e talz"/>
                </incluir>
                <alterar>
                        <registro chave="abcd" valor="novo valor"/>
                </alterar>
                <excluir>
                        <arquivo nome="lib/xxxxxxxx.jar" />
                        <arquivo nome="lib/yyyyyy.dll" />
                        <arquivo nome="zzzzzzz.jar" />
                </excluir>
                <executar>
                        <arquivo nome="driver/driverxyz.exe" />
                        <sql>
                                delete from Digital d where d.id = 1
                        </sql>
                </executar>
        </xml>
    */

    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        String msg;
        
        if (context != null) {
            msg = "Iniciando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Iniciando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, UpdateAppJob.class.getName(), "execute");
        
        // aqui estamos usando um novo entitymanager, para que não haja conflito de transações com o entitymanager padrão
        EntityManager em = EntityManagerFactory.getNewEntityManager();
        
        try {
            
            String versaoAtual = Constantes.CLIENTE_VERSAO;
            
            UpdateInfoTO updateInfo = ServiceRemoteFactory.getUpdateServiceRemoto().getUpdateInfo();
            String novidades = updateInfo.getNovidades();
            String versao = updateInfo.getVersao();
            String url = ServiceRemoteFactory.getUpdateServiceRemoto().getUpdateURL();
            
            if (versao!=null && !versao.equals(versaoAtual)) {
                
                // logar aviso de novidades
                LogMachine.getInstancia().logInfo("\nNovidades da versão "+versao+": "+novidades+"\n", UpdateAppJob.class.getName(), "logarNovidades");
                
                // logar chamada ao atualizador  
                LogMachine.getInstancia().logInfo("Chamando atualizador, url = "+url, UpdateAppJob.class.getName(), "execute");
                
                // obter o script de atualização da versão, gravar localmente para execução pelo atualizador;                
                salvarUpdateInfo(updateInfo);
                
                // chama atualizador passando os parametros necessários...                
                chamarAtualizador(url);
                
                // logar fechamento da aplicação atual
                //   necessita de um mecanismo para avisar os outros jobs que vai fechar...
                LogMachine.getInstancia().logInfo("Fechando aplicação...", UpdateAppJob.class.getName(), "execute");
                Platform.exit();
                System.exit(0);
                
            }
            
        } catch (Exception e) {
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, UpdateAppJob.class.getName(), "execute");
            
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
        
        LogMachine.getInstancia().logInfo(msg, UpdateAppJob.class.getName(), "execute");
    }

    private void chamarAtualizador(String url) throws Exception {
        
        try {
            
            String workingDir = Paths.get(".").toAbsolutePath().normalize().toString()+File.separator;
            System.out.println("workingDir = "+workingDir);
            
            if (!workingDir.contains("spe")) {
                workingDir = "c:\\sistemas\\spe-javafx\\";
                System.out.println("workingDir = "+workingDir);
            }
            
            String app = "spe_update.bat";
            
            ProcessBuilder pb = new ProcessBuilder(workingDir+app, "spe.bat", url);
            
            pb.directory(new File(workingDir));
            File log = new File("updateAppJob.log");

            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            Process p = pb.start();
            
            assert pb.redirectInput() == ProcessBuilder.Redirect.INHERIT;
            assert pb.redirectOutput().file() == log;
            assert p.getInputStream().read() == -1;

        } catch (Exception ex) {
            throw new Exception(ex);
        }        
        
    }

    private void salvarUpdateInfo(UpdateInfoTO updateInfo) throws Exception {
        String nome = Constantes.CLIENTE_UPDATEINFOFILE;
        
        File file = new File(nome);
        
        if (file.exists()) {
            file.delete();
        }
        
        XStream xstream = new XStream();
        String xml = xstream.toXML(updateInfo);
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(nome);
            fos.write("<?xml version=\"1.0\"?>".getBytes("UTF-8"));
            byte[] bytes = xml.getBytes("UTF-8");
            fos.write(bytes);
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("Falha ao gravar "+nome+": "+e.getMessage());
        } finally {
            if(fos!=null) {
                try{ 
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new Exception("Falha ao gravar "+nome+": "+e.getMessage());
                }
            }
        }        
    }

}