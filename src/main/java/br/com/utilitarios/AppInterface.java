

package br.com.utilitarios;

/**
 *
 * @author Felipe L. Garcia
 */
public interface AppInterface {
    
    public String getDataBase();
    public int getUserLogin();
    public int getSysLogin();
    public int getClient();
    
    public void setDataBase(String dataBase);
    public void setUserLogin(int userLogin);
    public void setSysLogin(int sysLogin);
    public void setClient(int client);
    
}
