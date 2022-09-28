package pontoeletronico.controller;

import br.jus.tjms.client.GuardianServiceClient;
import br.jus.tjms.seguranca.share.beans.AcessoTO;
import br.jus.tjms.seguranca.share.beans.AreaPessoaTO;
import br.jus.tjms.seguranca.share.beans.PapelTO;
import br.jus.tjms.seguranca.share.beans.PerfilTO;
import br.jus.tjms.seguranca.share.beans.TokenTO;
import br.jus.tjms.seguranca.share.parameters.ParamLogin;
import br.jus.tjms.seguranca.share.parameters.ParamSistema;

import pontoeletronico.util.JFXUtil;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController implements Initializable {

    @FXML
    private TextField inputLogin;

    @FXML
    private PasswordField inputSenha;

    private Boolean loginAdmin = false;

    private Boolean senhaValida = false;

    private Set<AreaPessoaTO> areas = null;

    private Set<PapelTO> papeis = null;

    private Set<PerfilTO> perfis = null;

    private Set<String> permissoes = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputLogin.setText("");
        inputSenha.setText("");
    }

    @FXML
    public void actionLogar(ActionEvent event) {
        logar(event);
    }

    private void logar(Event event) {

        String login = inputLogin.getText();
        String senha = inputSenha.getText();

        if (!loginAdmin) {
            this.senhaValida = false;
            JFXUtil.closeWindow(event);
            return;
        }

        try {
            
            GuardianServiceClient client = new GuardianServiceClient(GuardianServiceClient.URL_PRODUCAO);
            TokenTO tokenTO = client.login(new ParamLogin("PontoEletronico", login, DigestUtils.md5Hex(senha), ""));            
            AcessoTO acessos = client.acesso(tokenTO, new ParamSistema("PontoEletronico"));
            
            papeis = acessos.getPapeis();            
            areas = acessos.getAreas();
            perfis = acessos.getPerfis();
            permissoes = acessos.getPermissoes();

            if (loginAdmin && possuiPapel("PontoEletronico-Administrador")) {
                this.senhaValida = true;
                JFXUtil.closeWindow(event);
            }
            
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro no Login");
            alert.setContentText(e.getMessage());
            alert.show();

        }

    }

    public Boolean possuiPapel(String papel) {
        System.out.println("Possui papel " + papel + "??");
        if (papeis != null) {
            for (PapelTO papelTO : papeis) {
                if (papelTO.getNome().equals(papel)) {
                    System.out.println("Sim!");
                    return true;
                }
            }
        }
        System.out.println("Não...");
        return false;
    }

    public Boolean possuiArea(int codigoReduzido) {
        System.out.println("Possui área " + codigoReduzido + "??");
        if (areas != null) {
            for (AreaPessoaTO areaTO : areas) {
                if (areaTO.getCodigoReduzido().intValue() == codigoReduzido) {
                    System.out.println("Sim!");
                    return true;
                }
            }
        }
        System.out.println("Não...");
        return false;
    }

    public Boolean possuiPerfil(String perfil) {
        System.out.println("Possui perfil " + perfil + "??");
        if (perfis != null) {
            for (PerfilTO perfilTO : perfis) {
                if (perfilTO.getNome().equals(perfil)) {
                    System.out.println("Sim!");
                    return true;
                }
            }
        }
        System.out.println("Não...");
        return false;
    }

    public Boolean possuiPermissao(String permissao) {
        System.out.println("Possui permissão " + permissao + "??");
        if (permissoes != null) {
            for (String p : permissoes) {
                if (p.equals(permissao)) {
                    System.out.println("Sim!");
                    return true;
                }
            }
        }
        System.out.println("Não...");
        return false;
    }

    @FXML
    public void actionCancelar(ActionEvent event) {
        this.senhaValida = false;
        JFXUtil.closeWindow(event);
    }

    public Boolean getLoginAdmin() {
        return loginAdmin;
    }

    public void setLoginAdmin(Boolean loginAdmin) {
        this.loginAdmin = loginAdmin;
    }

    public Boolean getSenhaValida() {
        return senhaValida;
    }

    public void setSenhaValida(Boolean senhaValida) {
        this.senhaValida = senhaValida;
    }

    public TextField getInputLogin() {
        return inputLogin;
    }

    public void setInputLogin(TextField inputLogin) {
        this.inputLogin = inputLogin;
    }

    public PasswordField getInputSenha() {
        return inputSenha;
    }

    public void setInputSenha(PasswordField inputSenha) {
        this.inputSenha = inputSenha;
    }

    @FXML
    public void onKeyPressSenha(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            logar(event);
        } else if (event.getCode() == KeyCode.ESCAPE) {
            this.senhaValida = false;
            JFXUtil.closeWindow(event);
        }
    }

    @FXML
    public void onKeyPressLogin(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            logar(event);
        } else if (event.getCode() == KeyCode.ESCAPE) {
            this.senhaValida = false;
            JFXUtil.closeWindow(event);
        }
    }

    @FXML
    public void onKeyPressCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
            this.senhaValida = false;
            JFXUtil.closeWindow(event);
        }
    }

    public Set<AreaPessoaTO> getAreas() {
        return areas;
    }

    public void setAreas(Set<AreaPessoaTO> areas) {
        this.areas = areas;
    }

    public Set<PapelTO> getPapeis() {
        return papeis;
    }

    public void setPapeis(Set<PapelTO> papeis) {
        this.papeis = papeis;
    }

    public Set<PerfilTO> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<PerfilTO> perfis) {
        this.perfis = perfis;
    }

    public Set<String> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<String> permissoes) {
        this.permissoes = permissoes;
    }

}