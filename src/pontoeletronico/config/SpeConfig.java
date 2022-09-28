package pontoeletronico.config;

import br.jus.tjms.comuns.context.AbstractConfig;

public class SpeConfig extends AbstractConfig {

	private static final long serialVersionUID = -8071528964757292083L;

	@Override
	public boolean isAmbienteProducao() {
		return true;
	}

}