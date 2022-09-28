package pontoeletronico.controller;

import pontoeletronico.util.JFXUtil;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.organizacional.beans.UnidadeOrganizacional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import pontoeletronico.bean.Configuracao;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.tipo.TipoOperacao;

public class ConfiguracaoAreaController implements Initializable {
	
    private Configuracao configuracao;
    private Boolean configurado;
    private UnidadeOrganizacional comarcaSecretariaSelecionada;
    private UnidadeOrganizacional varaDepartamentoSelecionado;
    
    private UnidadeOrganizacional[] comarcaSecretaria;
    private UnidadeOrganizacional[] varaDepartamento;
    
    private Integer empresaAdm;
    private Integer matriculaAdm;
    
    
    @FXML
    private ComboBox<String> cbInstancia;
    
    @FXML
    private ComboBox<String> cbComarcaSecretaria;
    
    @FXML
    private ComboBox<String> cbVaraDepartamento;
    
    @FXML
    private Button btEditar;
    @FXML
    private Button btSalvar;
    @FXML
    private Button btCancelar;
    @FXML
    private Button btSair;
    
    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	cbInstancia.getItems().clear();
    	cbInstancia.getItems().addAll(Stream.of("Primeira Instância", "Segunda Instância").collect(Collectors.toList()));
    	cbInstancia.getSelectionModel().select(-1);
    	
        desabilitarCombos();
        
        desabilitarBotoesEdicao();

        carregaConfiguracao();
	}
    
    @FXML
    public void comboInstanciaActionPerformed(ActionEvent event) {                                            

        if (cbInstancia.getSelectionModel().getSelectedIndex() > -1) {
            cbComarcaSecretaria.getItems().clear();

            comarcaSecretaria = ServiceLocalFactory.getConfiguracaoService().listarComarcaSecretaria(cbInstancia.getSelectionModel().getSelectedIndex() + 1);

            for (int i = 0; i < comarcaSecretaria.length; i++) {
                cbComarcaSecretaria.getItems().add(comarcaSecretaria[i].getDescricaoComarcaSecretaria());
            }

            cbComarcaSecretaria.getSelectionModel().select(-1);
        }
        
    }
    
    @FXML
    private void comboComarcaSecretariaActionPerformed(ActionEvent event) {                                                    
        //guardando a comarca ou secretaria selecionada
        if (cbComarcaSecretaria.getSelectionModel().getSelectedIndex() > -1) {
            for (int i = 0; i < comarcaSecretaria.length; i++) {
                if (comarcaSecretaria[i].getDescricaoComarcaSecretaria() == cbComarcaSecretaria.getSelectionModel().getSelectedItem()) {
                    comarcaSecretariaSelecionada = comarcaSecretaria[i];
                }
            }
            cbVaraDepartamento.getItems().clear();

            varaDepartamento = ServiceLocalFactory.getConfiguracaoService().listarVaraDepartamento(cbInstancia.getSelectionModel().getSelectedIndex() + 1, comarcaSecretariaSelecionada);

            for (int i = 0; i < varaDepartamento.length; i++) {
                cbVaraDepartamento.getItems().add(varaDepartamento[i].getDescricaoVaraDepartamento());
            }
            
            cbVaraDepartamento.getSelectionModel().select(-1);
        }
    }                                                   

    @FXML
    public void actionSalvar(ActionEvent event) {

        if (cbComarcaSecretaria.getSelectionModel().getSelectedItem() == null) {
            if (cbInstancia.getSelectionModel().getSelectedIndex() == 0) {
                exibirMensagem("Informe a Comarca!");
                return;
            } else {
                exibirMensagem("Informe a secretaria!");
                return;
            }
        }
        
        configurado = ServiceLocalFactory.getConfiguracaoService().isConfigurado();
        
        if (!configurado) {
            configuracao = new Configuracao();
            configuracao.setCodigoAto(comarcaSecretariaSelecionada.getCodigoAto());
            configuracao.setCodigoInstancia(comarcaSecretariaSelecionada.getCodigoInstancia());
            configuracao.setCodigoComarcaSecretaria(comarcaSecretariaSelecionada.getCodigoComarcaSecretaria());
            configuracao.setDescricaoComarcaSecretaria(comarcaSecretariaSelecionada.getDescricaoComarcaSecretaria());
            configuracao.setCodigoReduzidoOrganograma(comarcaSecretariaSelecionada.getCodigoReduzido());
            configuracao.setCodigoLocalidade(comarcaSecretariaSelecionada.getCodigoLocalidade());

            if (cbVaraDepartamento.getSelectionModel().getSelectedItem() != null) {
                configuracao.setCodigoVaraDepartamento(varaDepartamentoSelecionado.getCodigoVaraDepartamento());
                configuracao.setDescricaoVaraDepartamento(varaDepartamentoSelecionado.getDescricaoVaraDepartamento());
                configuracao.setCodigoReduzidoOrganograma(varaDepartamentoSelecionado.getCodigoReduzido());
            }
            try {
            	ServiceLocalFactory.getConfiguracaoService().salvar(configuracao);

                if (ServiceLocalFactory.getConfiguracaoService().isConfigurado()) {
                    configurado = true;
                    configuracao = ServiceLocalFactory.getConfiguracaoService().getConfiguracao();
                }
                LogMachine.getInstancia().logInfo("Configuração incluída", getClass().getName(), "btSalvarActionPerformed", null, getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.CONFIGURACAO);
            } catch (ServiceException ex) {
                ex.printStackTrace();
                LogMachine.getInstancia().logErro("Erro ao incluir configuração: " + ex.getMessage(), getClass().getName(), "btSalvarActionPerformed", null, getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.CONFIGURACAO);
            }
            
        } else {

            configuracao.setCodigoInstancia(null);
            configuracao.setCodigoComarcaSecretaria(null);
            configuracao.setDescricaoComarcaSecretaria(null);
            configuracao.setCodigoReduzidoOrganograma(null);
            configuracao.setCodigoVaraDepartamento(null);
            configuracao.setDescricaoVaraDepartamento(null);

            configuracao.setCodigoInstancia(comarcaSecretariaSelecionada.getCodigoInstancia());
            configuracao.setCodigoAto(comarcaSecretariaSelecionada.getCodigoAto());
            configuracao.setCodigoComarcaSecretaria(comarcaSecretariaSelecionada.getCodigoComarcaSecretaria());
            configuracao.setDescricaoComarcaSecretaria(comarcaSecretariaSelecionada.getDescricaoComarcaSecretaria());
            configuracao.setCodigoReduzidoOrganograma(comarcaSecretariaSelecionada.getCodigoReduzido());
            configuracao.setCodigoLocalidade(comarcaSecretariaSelecionada.getCodigoLocalidade());

            if (cbVaraDepartamento.getSelectionModel().getSelectedItem() != null) {
                configuracao.setCodigoVaraDepartamento(varaDepartamentoSelecionado.getCodigoVaraDepartamento());
                configuracao.setDescricaoVaraDepartamento(varaDepartamentoSelecionado.getDescricaoVaraDepartamento());
                configuracao.setCodigoReduzidoOrganograma(varaDepartamentoSelecionado.getCodigoReduzido());
                configuracao.setCodigoLocalidade(varaDepartamentoSelecionado.getCodigoLocalidade());
            }
            try {
            	ServiceLocalFactory.getConfiguracaoService().atualizar(configuracao);
                LogMachine.getInstancia().logInfo("Configuração alterada", getClass().getName(), "btSalvarActionPerformed", null, getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.CONFIGURACAO);
            } catch (ServiceException ex) {
                ex.printStackTrace();
                LogMachine.getInstancia().logErro("Erro ao alterar configuração: " + ex.getMessage(), getClass().getName(), "btSalvarActionPerformed", null, getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.CONFIGURACAO);
            }
        }
        desabilitarBotoesEdicao();
        desabilitarCombos();
        exibirMensagem("Configuração realizada.");
     
    }
    
	private void exibirMensagem(String mensagem) {
		JFXUtil.showInfoMessage("Atenção!", mensagem);
	}

    @FXML
    public void comboVaraDepartamentoActionPerformed(ActionEvent event) {                                                   
        //guardando a vara ou departamento selecionado
        if (cbVaraDepartamento.getSelectionModel().getSelectedIndex() > -1) {
            for (int i = 0; i < varaDepartamento.length; i++) {
                if (varaDepartamento[i].getDescricaoVaraDepartamento() == cbVaraDepartamento.getSelectionModel().getSelectedItem()) {
                    varaDepartamentoSelecionado = varaDepartamento[i];
                }
            }
        }
    } 
    
    private void desabilitarCombos() {
        cbInstancia.setDisable(true);
        cbComarcaSecretaria.setDisable(true);
        cbVaraDepartamento.setDisable(true);
    }

    private void habilitarCombos() {
        cbInstancia.setDisable(false);
        cbComarcaSecretaria.setDisable(false);
        cbVaraDepartamento.setDisable(false);
    }

    private void habilitarBotoesEdicao() {
        btSalvar.setDisable(false);
        btSair.setDisable(true);
        btCancelar.setDisable(false);
        btEditar.setDisable(true);
    }
    
    private void desabilitarBotoesEdicao() {
        btSalvar.setDisable(true);
        btSair.setDisable(false);
        btCancelar.setDisable(true);
        btEditar.setDisable(false);
    }
    
    private void carregaConfiguracao() {
        cbComarcaSecretaria.getItems().clear();
        cbVaraDepartamento.getItems().clear();
        
        configurado = ServiceLocalFactory.getConfiguracaoService().isConfigurado();

        if (configurado) {
            configuracao = ServiceLocalFactory.getConfiguracaoService().getConfiguracao();
            cbInstancia.getSelectionModel().select(configuracao.getCodigoInstancia() - 1);
            cbComarcaSecretaria.getItems().add(configuracao.getDescricaoComarcaSecretaria());
            cbComarcaSecretaria.getSelectionModel().select(0);
            cbVaraDepartamento.getItems().add(configuracao.getDescricaoVaraDepartamento());
            cbVaraDepartamento.getSelectionModel().select(0);
        } else {
            cbInstancia.getSelectionModel().select(-1);
            cbComarcaSecretaria.getSelectionModel().select(-1);
            cbVaraDepartamento.getSelectionModel().select(-1);
        }
        
        btEditar.setDisable(false);
    }    

	@FXML
	public void actionSair(ActionEvent event) {
		Main.showTelaConfiguracao();
	}
	
	@FXML
    public void actionEditar(ActionEvent event) {
        habilitarCombos();
        habilitarBotoesEdicao();
    }

	@FXML
    public void actionCancelar(ActionEvent event) {        
        desabilitarCombos();
        desabilitarBotoesEdicao();
        carregaConfiguracao();
    }

    public Integer getEmpresaAdm() {
        return empresaAdm;
    }

    public void setEmpresaAdm(Integer empresaAdm) {
        this.empresaAdm = empresaAdm;
    }

    public Integer getMatriculaAdm() {
        return matriculaAdm;
    }

    public void setMatriculaAdm(Integer matriculaAdm) {
        this.matriculaAdm = matriculaAdm;
    }

	public Configuracao getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}

	public Boolean getConfigurado() {
		return configurado;
	}

	public void setConfigurado(Boolean configurado) {
		this.configurado = configurado;
	}

	public UnidadeOrganizacional getComarcaSecretariaSelecionada() {
		return comarcaSecretariaSelecionada;
	}

	public void setComarcaSecretariaSelecionada(UnidadeOrganizacional comarcaSecretariaSelecionada) {
		this.comarcaSecretariaSelecionada = comarcaSecretariaSelecionada;
	}

	public UnidadeOrganizacional getVaraDepartamentoSelecionado() {
		return varaDepartamentoSelecionado;
	}

	public void setVaraDepartamentoSelecionado(UnidadeOrganizacional varaDepartamentoSelecionado) {
		this.varaDepartamentoSelecionado = varaDepartamentoSelecionado;
	}

	public UnidadeOrganizacional[] getComarcaSecretaria() {
		return comarcaSecretaria;
	}

	public void setComarcaSecretaria(UnidadeOrganizacional[] comarcaSecretaria) {
		this.comarcaSecretaria = comarcaSecretaria;
	}

	public UnidadeOrganizacional[] getVaraDepartamento() {
		return varaDepartamento;
	}

	public void setVaraDepartamento(UnidadeOrganizacional[] varaDepartamento) {
		this.varaDepartamento = varaDepartamento;
	}

}