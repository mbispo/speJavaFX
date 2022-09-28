package pontoeletronico.controller;

import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.JFXUtil;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.tipo.TipoOperacao;


public class DataHoraPontoController {

    @FXML
    private DatePicker dpData;

    @FXML
    private TextField tfHora;
    
    @FXML
    private Button btOk;
    
    @FXML
    private Button btSair;

    @FXML
    public void actionOk(ActionEvent event) {

        String strdata = "";
        Date data = null;
        String strhora = "";
        Date hora = null;
        int inthora = 0;
        int intminuto = 0;

        SimpleDateFormat dfdata = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dfhora = new SimpleDateFormat("HH:mm");
        
        strhora = tfHora.getText();

        try {
            LocalDate localDate = dpData.getValue();
            Date date = Date.from(localDate.atStartOfDay(ZoneId.of(Constantes.TIME_ZONE)).toInstant());
            strdata = dfdata.format(date);    
            data = dfdata.parse(strdata);
        } catch (Exception e) {
            JFXUtil.showErrorMessageNow("Data Inválida", "Informe uma data válida!");
            dpData.requestFocus();
            return;
        }

        try {
            hora = dfhora.parse(strhora);
        } catch (Exception e) {
            JFXUtil.showErrorMessageNow("Hora Inválida", "Informe uma hora válida (formato hh:mm)!");
            tfHora.requestFocus();
            return;
        }

        inthora = Integer.parseInt(strhora.substring(0, 2));
        intminuto = Integer.parseInt(strhora.substring(3, 5));

        try {
            RelogioJobService relogio = RelogioJobService.getInstancia();
            relogio.setDataHora(data, inthora, intminuto, 0);
            // TODO logar quem está fazendo a inicialização manual da hora
            LogMachine.getInstancia().logInfo("Hora inicial informada: "+relogio.getDataHora(), this.getClass().getName(), "actionOk", null, null, null, TipoOperacao.DATAHORA);

        } catch (Exception e) {
            e.printStackTrace();
            JFXUtil.showErrorMessage("Configuração Data/Hora", "Erro ao configurar data/hora: " + e.getMessage());
            // TODO logar quem está fazendo a inicialização manual da hora
            LogMachine.getInstancia().logErro("Erro ao configurar data/hora: " + e.getMessage(), this.getClass().getName(), "actionOk", null, null, null, TipoOperacao.DATAHORA);
        }

        // reiniciar todos os jobs
        Main.iniciaJobs(true);

        JFXUtil.showInfoMessageNow("Data/Hora", "Hora configurada e jobs iniciados.");

        Main.showTelaPrincipal();

    }

    @FXML
    public void actionSair(ActionEvent event) {
        Platform.exit();
        System.exit(0);        
    }
    
    @FXML
    public void actionKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE || event.getSource().equals(btSair)) {
            btSair.fire();
        } else if (event.getCode() == KeyCode.ENTER) {
            btOk.fire();
        }
    }

    public DatePicker getDpData() {
        return dpData;
    }

    public void setDpData(DatePicker dpData) {
        this.dpData = dpData;
    }

    public TextField getTfHora() {
        return tfHora;
    }

    public void setTfHora(TextField tfHora) {
        this.tfHora = tfHora;
    }

    public Button getBtOk() {
        return btOk;
    }

    public void setBtOk(Button btOk) {
        this.btOk = btOk;
    }

    public Button getBtSair() {
        return btSair;
    }

    public void setBtSair(Button btSair) {
        this.btSair = btSair;
    }

}