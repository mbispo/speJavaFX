package pontoeletronico.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import pontoeletronico.util.JFXUtil;

public class ConfiguracaoController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void actionConfigurarArea(ActionEvent event) {

        Main.changeScreen(Screens.CONFIGURAR_AREA);

    }

    @FXML
    public void actionCadastrarDigital(ActionEvent event) {
        Main.changeScreen(Screens.CADASTRO_DIGITAIS);
    }

    @FXML
    public void actionSair(ActionEvent event) {

        Main.changeScreen(Screens.PRINCIPAL);

    }

    @FXML
    public void actionConfigurarDataHora(ActionEvent event) {
        if (Main.iniciaInstanciaData()) {
            JFXUtil.showWarningMessage("Data/hora", "Data e hora sincronizados com o servidor!");
        } else {
            Main.changeScreen(Screens.CONFIGURAR_DATAHORA);
        }

    }

    @FXML
    public void actionAtalhos(ActionEvent event) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Atalhos");
        alert.setHeaderText("Lista de Atalhos");
        alert.setContentText("F11 - Falar a Data\nF12 - Falar a Hora\n#config ou #admin - Acessar Configurações do Sistema\n#versao - Mostrar a versão do Sistema\n#update - Atualizar o Sistema\n#restart ou #reboot ou #reiniciar - Reiniciar o Sistema\n#exit - Sair");
        alert.show();

    }

}
