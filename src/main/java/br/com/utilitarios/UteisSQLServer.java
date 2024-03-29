/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import br.com.utilitarios.UteisSQL.ISql;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisSQLServer implements ISql{
    public static final String SQL_DBPATH = 
            "SELECT RDB$GET_CONTEXT('SYSTEM', 'DB_NAME') AS NAME"
            + " FROM RDB$DATABASE";
    
    public static final String SQL_CONNECTION_CONT = 
            "select MON$STATE from MON$ATTACHMENTS";
    
    public static final String SQL_DB_TABLES_RELATIONS =
            "select rdb$relation_name AS NAME" 
            +" from rdb$relations"
            +" where rdb$view_blr is null"
            +" and (rdb$system_flag is null or rdb$system_flag = 0) "
            +" order by rdb$relation_name";
    
    public static final String SQL_FORENKEY = "SELECT DISTINCT " + 
            "    OBJECT_NAME(f.parent_object_id) REF_TABLE_NAME" + 
            "   ,COL_NAME(fc.parent_object_id, fc.parent_column_id) REF_COLUMN_NAME " + 

            "	FROM sys.foreign_keys AS f" + 
            "	INNER JOIN sys.foreign_key_columns AS fc" + 
            "	   ON f.object_id = fc.constraint_object_id" + 
            "	where OBJECT_NAME (f.referenced_object_id) = ?";

    public static final String SQL_PRIMARYKEY = "SELECT COLUMN_NAME "  
                            +"	FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE"  
                            +"	WHERE OBJECTPROPERTY(OBJECT_ID(CONSTRAINT_SCHEMA + '.' + QUOTENAME(CONSTRAINT_NAME)), 'IsPrimaryKey') = 1"  
                            +"	AND TABLE_NAME =?";
    @Override
    public String getSQLForenkey() {
        return SQL_FORENKEY;
    }
    @Override
    public String getSQLPrimarykey() {
        return SQL_PRIMARYKEY;
    }
    
    
    public String[] getColumns(String table,Connection conn) {        
        String sql = "SELECT COLUMN_NAME " +
            " FROM INFORMATION_SCHEMA.COLUMNS " +
            " WHERE TABLE_SCHEMA= (SELECT DATABASE())"+
            " AND TABLE_NAME='"+table+"'";
        
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }
            
            return list.toArray(new String[list.size()]);
            
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
    
    public String[] getTables(Connection conn) {        
        String sql = "SELECT table_name"
                + " FROM information_schema.tables"
                + " where TABLE_SCHEMA=(select database())";
        
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("table_name"));
            }
            
            return list.toArray(new String[list.size()]);
            
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
    
    public static int getDbConnections(Connection connection) {      
        return UteisMetodos.nz(
                UteisSQL.get("SQL_CONNECTION_CONT"
                        , "MON$STATE",connection),0);
        }
        
     /**
     * RETORNA LISTA QUE A TABELA DEPENDENTE
     */
    public List<String> getDependsInt(String tabela,Connection connection) {
        List<String> list = new ArrayList<String>();

        String sql = "select distinct"
                + " PK.RDB$RELATION_NAME as PKTABLE_NAME"
                + " from"
                + " RDB$RELATION_CONSTRAINTS as  PK"
                + ",RDB$RELATION_CONSTRAINTS  as FK"
                + ",RDB$REF_CONSTRAINTS as RC"
                + " WHERE FK.RDB$RELATION_NAME = '" + tabela + "'"
                + " and FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME"
                + " and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
                + " order by PKTABLE_NAME";

        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String relation = rs.getString("PKTABLE_NAME").trim();

                list.add(relation);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return list;
    }
    
    /**
     * RETORNA LISTA DE TABELAS DEPENDENTES
     */
    public List<String> getDependsExt(String tabela,Connection connection) {
        List<String> list = new ArrayList<String>();

        String sql = "select A.RDB$RELATION_NAME"
                + ",E.RDB$FIELD_NAME as OnField"
                + " from RDB$REF_CONSTRAINTS B"
                + ",RDB$RELATION_CONSTRAINTS A"
                + ", RDB$RELATION_CONSTRAINTS C"
                + ",RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E"
                + " where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') "
                + " and (A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME)"
                + " and (B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME)"
                + " and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME)"
                + " and (A.RDB$INDEX_NAME=E.RDB$INDEX_NAME)"
                + " and (C.RDB$RELATION_NAME = '" + tabela + "')"
                + " order by RDB$RELATION_NAME";

        PreparedStatement pstmt = null;
        try {            
            pstmt = connection.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String relation = rs.getString("RDB$RELATION_NAME").trim();

                list.add(relation);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return list;
    }

    public List<Map> getListMap(String tabela
            , Map<String, String> map,Connection conn
            ,int pag,int reg,Object... filters) {
        
        List<Map> list = new ArrayList<>();
        
        int skip = (pag - 1) * reg;
        String sql = "select  *"
                + " from " + tabela
                +" LIMIT "+skip+","+reg;

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);

            if (filters != null) {
                for (Object f : filters) {
                    pstmt.setValue(f);
                }
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ResultSetUtil rsUtil = new ResultSetUtil(rs);

                Map hash = new HashMap();
                
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String campo = entry.getValue();
                    
                    Object value = rsUtil.getValue(campo);
                    
                    hash.put(key, value == null ? "" : value);
                }   
                list.add(hash);
            }            

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return list;
    }
    
    public static void executeMySQL(String user, String pw, String mysql, File... list) {
        try {
            for (File sql : list) {
                String[] cmd = new String[]{"\"" + mysql + File.separator + "mysql.exe\""
                     ,"--user=\"" + user + "\""
                     ,"--password=\"" + pw + "\""
                     ,"<","\"" + sql.getPath() + "\""};

                System.out.println(String.join(" ", cmd));
                
                Process p = Runtime.getRuntime().exec(cmd);
                if (p.waitFor()!= 0) {
                    throw new Exception(UteisMetodos.toString(p.getErrorStream()));
                }
                p.destroy();

                System.out.println("SQL SCRIPT " + sql.getName());
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }
}