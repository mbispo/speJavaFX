package pontoeletronico.controller;

import pontoeletronico.util.JFXUtil;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SenhaController implements Initializable {

    @FXML
    private PasswordField inputSenha;

    private Boolean senhaInformada = false;

    private String senha = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputSenha.setText("");
    }

    @FXML
    public void actionOk(ActionEvent event) {
        logar(event);
    }

    private void logar(Event event) {

        senha = inputSenha.getText();
        this.senhaInformada = senha!=null&&(!senha.trim().equals(""));
        JFXUtil.closeWindow(event);

    }

    @FXML
    public void actionCancelar(ActionEvent event) {
        this.senhaInformada = false;
        JFXUtil.closeWindow(event);
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getSenhaInformada() {
        return senhaInformada;
    }

    public void setSenhaInformada(Boolean senhaInformada) {
        this.senhaInformada = senhaInformada;
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
            this.senhaInformada = false;
            JFXUtil.closeWindow(event);            
        }
    }

    @FXML
    public void onKeyPressCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            this.senhaInformada = false;
            JFXUtil.closeWindow(event);
        }
    }

}