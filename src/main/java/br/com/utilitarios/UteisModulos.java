

package br.com.utilitarios;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisModulos {
    
    public static enum Sistemas {
        EVENTOS(1,"G-Eventos",AppVariables.entity[3]),
        AUTO_PEC(2,"G-Auto/Oficina",AppVariables.entity[1]),
        MULTSERVICOS(3,"G-MultService",AppVariables.entity[0]),
        COMMERCE(4,"G-Commerce",AppVariables.entity[2]),
        CURSOS(5,"G-Cursos",AppVariables.entity[2]);

        private Sistemas(int value, String name,String pack) {
            this.value = value;
            this.name = name;
            this.pack = pack;
        }

        public final int value;
        public final String name;
        public final String pack;
        
        public static String getName(int value){
            return get(value).name;
        }
        public static Sistemas get(int value){
            for (Sistemas obj : values()) {
                if (obj.value==value) {
                    return obj;
                }
            }
            return null;
        }
    }        
}
