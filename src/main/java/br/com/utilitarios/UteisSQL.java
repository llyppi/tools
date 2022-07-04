/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisSQL {        
    
    public static String get(String sql,String alias,Connection conn) {        
        return get(sql, alias, conn,null);
    }
    public static String get(String sql,String alias,Connection conn,Object[] filters) {        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            
            if(filters!=null){
                for (Object f : filters) {
                    pstmt.setValue(f);
                }
            }
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return String.valueOf(rs.getObject(alias));
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                pstmt.close();
                
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public static int getCount(String tabela,String campo,Connection conn) {    
        int c = 0;
        try {
            c = Integer.valueOf(get(
                    "SELECT COUNT(" + campo + ") as " + campo
                    + " FROM " + tabela, campo
                ,conn));
        } catch (NumberFormatException numberFormatException) {
        }
        
        return c;
    }
    
    public static boolean executeQuery(Connection conn,String... sql) {        
        PreparedStatement pstmt = null;
        
        conn.setLog(null);
        
        boolean state = conn.isFixConn();
        
        conn.setFixConn(true);
        
        for (String s : sql) {
            try {
                pstmt = conn.prepareStatement(s);

                pstmt.execute();

            } catch (Exception e) {
                conn.setLog(conn.getLog()+"\n"+e.getMessage());
                System.out.println(e);
            }
        }
        try {
//            pstmt.close();
        } catch (Exception e) {
        }
        conn.setFixConn(state);
        
        return conn.getLog()==null;
    }
    
    public static java.sql.Connection getConnAccess(String banco){
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            
            return DriverManager.getConnection(
                    "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};"
                    + "DBQ="+banco);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
    
    public static enum DBServer {
        MYSQL, FIREBIRD, SQLSERVER
    }

    public static DBServer getServer(Connection connection) {
        if (connection.getDriver().toLowerCase().contains("mysql")) {
            return DBServer.MYSQL;
        }
        if (connection.getDriver().toLowerCase().contains("fbdriver")) {
            return DBServer.FIREBIRD;
        }
        if (connection.getDriver().toLowerCase().contains("sqlserver")) {
            return DBServer.SQLSERVER;
        }        
        return null;
    }
    
    public static ISql getISQL(DBServer dbCurrent) {
        if(DBServer.MYSQL.equals(dbCurrent)){
            return new UteisSQLMy() ;
        }
        if(DBServer.FIREBIRD.equals(dbCurrent)){
            return new UteisSQLFire();
        }
        if(DBServer.SQLSERVER.equals(dbCurrent)){
            return new UteisSQLServer();
        }
        return null;
    }

    public static int importXML(Connection connection
                                    ,String entity,String packEntity
                                    ,String packDao
                                    ,InputStream input) throws Exception{        
        if (input == null) {
            throw new Exception("bty null");
        }
        if (connection == null) {
            throw new Exception("connection null");
        }
        Class clsDao = UteisMetodos.findClass(entity+"Dao", packDao);
        if (clsDao == null) {
            throw new Exception("cls not found "+packDao+"."+entity+"Dao");
        }
        Object dao = clsDao.getConstructor(Connection.class).newInstance(connection);
        if (dao == null) {
            throw new Exception("dao not newInstance " + clsDao.getCanonicalName());
        }
        //CONN FIX
//        Method setFix = UteisMetodos.getMethod("setFixConn", clsDao,boolean.class);
//        if (setFix == null) {
//            throw new Exception("setFix not implement " + clsDao.getCanonicalName());
//        }
//        setFix.invoke(dao, true);

        Class clsEnti = UteisMetodos.findClass(entity, packEntity);
        if (clsEnti == null) {
            throw new Exception("cls not found " + packEntity + "." + entity);
        }
        //INSERIR
        Method inserir = UteisMetodos.getMethod("inserir", clsDao,clsEnti);
        if (inserir == null) {
            throw new Exception("inserir null " + clsDao.getCanonicalName());
        }        

        List list = UteisMetodos.getList(input, entity, packEntity);
        if (list == null) {
            throw new Exception("list null "+entity);
        }

        //INSERIR DAO
        for (Object obj : list) {
            Object rtn = inserir.invoke(dao, obj);
            
            if(!(boolean)rtn){
                throw new Exception("Erro ao inserir "+entity);
            } 
        }
        return list.size();
    }
    
    public static boolean exportXML(Connection connection,String pack,File pathExport) throws Exception{      
        if (connection == null) {
            throw new Exception("connection null");
        }
        //CACHE TXT GRAVADO NO ARQUIVO
        List<String> cacheOUT = new ArrayList<>();
                
        DBServer dbServer = UteisSQL.getServer(connection);

        String[] tables = UteisSQL.getISQL(dbServer).getTables(connection);

//            File file = getFileExport(table);
//            List<byte[]> list = new ArrayList<>();

        for (String tab : tables) {
            File fout = new File(pathExport+File.separator+tab+".xml");
            
            if(!fout.getParentFile().exists()){
                fout.getParentFile().mkdirs();
            }
            fout.delete();
            fout.createNewFile();
            
            OutputStream out = new FileOutputStream(fout);

            exportXML(tab, pack, out, cacheOUT, connection);
//                list.add(out.toByteArray());
        }                        
//            return list.toArray(new byte[list.size()][]);
        return true;
    }        

    public static boolean exportXML(String table
                                ,String pack
                                ,OutputStream out
                                ,List<String> cacheOUT
                                ,Connection connection) throws Exception{                

        if (cacheOUT == null) {
            cacheOUT = new ArrayList<>();
        }

        if (cacheOUT.contains(table)) {
            return true;
        }

        Class clsEntidade = 
                UteisMetodos.findClass(table, pack);

        if (clsEntidade == null) {               
//            throw new Exception("Classe não encontrada "+table);            
            return false;
        }
        DBServer dbServer = UteisSQL.getServer(connection);

//        List<String> listDependInt = 
//                UteisSQL.getISQL(dbServer).getDependsInt(table,connection);
//
//        for (String tab : listDependInt) {
//            File fexport = new File(tab);
//            if (fexport.exists() && fexport.length() > 0) {
//                continue;
//            }
//            //RECURCIVO
//            exportXML(tab
//                    ,pack
//                    , new FileOutputStream(fexport)
//                    , cacheOUT
//                    ,connection);
//        }                    
        Map attrColumn = getAttribColumn(table,pack, connection);
        
        if (attrColumn == null) {
            out.close();
            return false;
        }

        List<Map> listMap = 
                UteisSQL.getISQL(dbServer).getListMap(table,attrColumn
                                                    ,connection
                                                    ,1,Integer.MAX_VALUE);
//            JAXBContext jaxbContext = JAXBContext.newInstance(Product.class);
//            Marshaller marshaller = jaxbContext.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(product, new File("product.xml"));
//            marshaller.marshal(product, System.out);
//            Normalizer.normalize(stringAcentuada, Form.NFD)
//                    .replaceAll("[^\\p{ASCII}]", "");
//            StringUtils.
        String tagINI="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                +(char) 13 +"<list size=\""+listMap.size()+"\">";
        out.write(tagINI.getBytes());
        
        String cls = clsEntidade.getSimpleName();
       
        for (Map m : listMap) {
            String xml = UteisMetodos.toXML(m);
            
            xml = (char) 13 + "<" + cls + ">"
                    +(char) 13 +xml
                    + (char) 13 + "</" + cls + ">";
                    
            out.write(xml.getBytes());

            out.flush();
        }                        
        String tagEND="</list>";
        out.write(tagEND.getBytes());

        //CACHE out
        cacheOUT.add(table);
    
        out.close();
        
        return true;
    }

    public static Map getAttribColumn(String table,String pack,Connection connection){    
        String[] columns  = getISQL(getServer(connection)).getColumns(table,connection);
        
        Map map = new LinkedHashMap();
                
        for (String column : columns) {
            String atributo = UteisMetodos.findAttribLike(table,column,pack);
            
            if (atributo!=null) {
                //campo=
                map.put(atributo,column);
            }  
        }
        
        return map;
    }

    /**
     * VERIFICA QT DE COLUNAS E PARAMETROS DO SQL INSERT,PRINT SE NAO CONFERIR
     */
    public static void verificate(String sql) {
        if (sql == null) {
            return;
        }
        if (!new Regex("(?i).*INSERT|UPDATE.*").find(sql)) {
            return;
        }
        String[] param = getParams(sql);
        String[] campos = getColumns(sql);

        if (param == null || campos == null) {
            return;
        }

        if (campos.length != param.length) {
            System.out.println("SQL INVALID");
            System.out.println("FIELDS=" + campos.length);
            System.out.println("PARAMS=" + param.length);
        }
    }

    public static boolean validate(String sql) {
        return sql!=null && !sql.isEmpty() && UteisMetodos.isISO88591(sql);
    }
    
    public static String[] getParams(String sql) {
        if (sql == null) {
            return null;
        }
        Regex regex;
        String value = "";
        
        if (sql.matches("(?i).*INSERT.*")) {
            regex = new Regex("(?i)INTO\\s{1,}.*\\s{1,}VALUES\\s{1,}\\((.*)\\)");
            value = regex.get(sql, 1);
            value = value.replaceAll("\\s{1,}\\,\\s{1,}", "");
        }
        if (sql.matches("(?i).*UPDATE.*")) {
            regex = new Regex("(?i)UPDATE\\s{1,}SET\\s{1,}\\(.*\\)WHERE{0,}");
            value = regex.get(sql, 1);
            value = value.replaceAll("\\s{1,}\\,\\s{1,}", "");
            value = value.replaceAll(".*\\s*\\=\\s*(.*)\\s*\\,{0,}", "$1,");
        }
        if(value.endsWith(",")){
            value = value.substring(value.length()-1);
        }
        if (value.isEmpty()) {
            return null;
        }
        return value.split(",");
    }

    public static String[] getColumns(String sql) {
        if (sql == null) {
            return null;
        }
        Regex regex;
        String value = "";

        if (sql.matches("(?i).*INSERT.*")) {
            regex = new Regex("(?i)INTO\\s{1,}\\((.*)\\)\\s{1,}VALUES");
            value = regex.get(sql, 1);
            value = value.replaceAll("\\s{1,}\\,\\s{1,}", "");
        }
        if (sql.matches("(?i).*UPDATE.*")) {
            regex = new Regex("(?i)UPDATE\\s{1,}SET\\s{1,}(.*)\\s{1,}WHERE{0,}");
            value = regex.get(sql, 1);
            value = value.replaceAll("\\s{1,}\\,\\s{1,}", "");
            value = value.replaceAll(".*\\s*\\=\\s*(.*)\\s*\\,{0,}", "$1,");
        }
        if (value.endsWith(",")) {
            value = value.substring(value.length() - 1);
        }
        if (value.isEmpty()) {
            return null;
        }
        return value.split(",");
    }       
    
    public static String[] getTables(String sql,java.sql.Connection conn) {
        List<String> list = new ArrayList<>();
        try {
            String db = conn.getMetaData().getConnection().getCatalog();
            
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, db);
            
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                list.add(rs.getString("table_name"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list.toArray(new String[list.size()]);
    }
    
    public static String getTable(String sql) {
        if (sql == null) {
            return null;
        }
        Regex regex;
        String value = "";

        if (sql.matches("(?i).*INSERT.*")) {
            regex = new Regex("(?i)INTO\\s{1,}([A-Za-z]*)\\s");
            value = regex.get(sql, 1);
        }
        if (sql.matches("(?i).*UPDATE.*")) {
            regex = new Regex("(?i)UPDATE\\s{1,}([A-Za-z]*)\\s");
            value = regex.get(sql, 1);
        }
        if (sql.matches("(?i).*SELECT.*")) {
            regex = new Regex("(?i)FROM\\s{1,}([A-Za-z]*)\\s");
            value = regex.get(sql, 1);
        }
        return value;
    }       

    public List<RelationTable> getRelationTable(String table,Integer idPK,Connection conn){
        List<RelationTable> list = new ArrayList<RelationTable>();

        try {
            ISql iSql = getISQL(getServer(conn));
            
            String sql = iSql.getSQLForenkey();

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setValue(table);
            
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                String tab = rs.getString("REF_TABLE_NAME");
                String col = rs.getString("REF_COLUMN_NAME");
                
                pstmt = conn.prepareStatement(sql);
                pstmt.setValue(tab);
                
                ResultSet rsPKName= pstmt.executeQuery(iSql.getSQLPrimarykey());
                
                String pkName = rsPKName.getString("COLUMN_NAME");
                
                sql= " select "+pkName+" AS PK_VALUE" 
                            +"	from "+tab 
                            +"	where "+col+"="+idPK;
                
                pstmt = conn.prepareStatement(sql);
                
                ResultSet rsPK= pstmt.executeQuery(sql);

                while(rsPK.next()) {
                    RelationTable relationTable = new RelationTable();

                    relationTable.setTable(tab);
                    relationTable.setPk(rs.getInt("PK_VALUE"));

                    list.addAll(getRelationTable(relationTable.getTable(),relationTable.getPk(),conn));
                    list.add(relationTable);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return list;
    }
    
    public static String getCase(String sql,boolean upper,java.sql.Connection conn) {
        String[] table = getTables(UteisSQLMy.SQL_TABLES,conn);
        
//        Pattern p = Pattern.compile("(\\'.*\\')");
//        Matcher m = p.matcher(sql);
//        List<String> list = new ArrayList<>();
//        while(m.find()) {
//           list.add(m.group(1));
//        }
        
        for (String t : table) {
            sql = sql.replaceAll("(?i)"+t, upper?t.toUpperCase():t.toLowerCase());
        }
        
        return sql;
    }
    
    public static interface ISql {
       public String[] getTables(Connection conn);
    
       public String[] getColumns(String table,Connection conn);
       
       public List<String> getDependsInt(String tabela,Connection connection);
       
       public List<String> getDependsExt(String tabela,Connection connection);
       
       public List<Map> getListMap(String tabela
            , Map<String, String> map,Connection conn
            ,int pag,int reg,Object... filters);
       
       public String getSQLForenkey();
       
       public String getSQLPrimarykey();
    }    
}
