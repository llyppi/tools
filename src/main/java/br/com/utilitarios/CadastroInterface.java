

package br.com.utilitarios;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

/**
 *
 * @author Felipe L. Garcia
 */

public interface CadastroInterface {
    public void setEnabledNovo(boolean enabled);
    public void setEnabledAlterar(boolean enabled);
    public void setEnabledSalvar(boolean enabled);
    public void setEnabledExcluir(boolean enabled);
    public void setEnabledImprimir(boolean enabled);
    
    public Button getBtnAlterar();

    public Button getBtnExcluir();

    public Button getBtnImprimir();

    public Button getBtnNovo();

    public Button getBtnSalvar();

    public Table getTabela();
    
    public void verificarDireitos(int usuario);
}
