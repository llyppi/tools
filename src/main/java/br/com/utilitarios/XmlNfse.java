package br.com.utilitarios;


//import br.com.app.entidade.Contribuintes;
//import br.com.app.entidade.Nfse;
//import br.com.app.entidade.NfseSolicitacaoCancelamento;
//import br.com.app.entidade.Pessoas;
//import br.com.app.bo.NfseBO;
//import br.com.app.bo.NfseErros;
//import br.com.app.dao.BairrosDao;
//import br.com.app.dao.CnaeFiscalDao;
//import br.com.app.dao.ContribuintesDao;
//import br.com.app.dao.LogradourosDao;
//import br.com.app.dao.MunicipiosDao;
//import br.com.app.dao.NfseDao;
//import br.com.app.dao.ParametrosComunsDao;
//import br.com.app.dao.PessoasDao;
//import br.com.utilitarios.UteisData;
//import br.com.utilitarios.UteisData.FormatData;
//import br.com.utilitarios.UteisMetodos;
//import java.io.File;
//import java.math.BigInteger;
//import java.sql.Date;
//import java.text.DecimalFormat;


/**
 *
 * @author Felipe L. Garcia
 */
public class XmlNfse {
//    public static final String cabecalho = 
//              " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
//            + " xsi:schemaLocation=\"http://www.abrasf.org.br/nfse.xsd\""
//            + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
//    
//    private static final String cabecalhoNFSE = "<CompNfse"+ cabecalho;
//    
//    private static final String cabecalho_CONSULTA_ENVIO = 
//            "<ConsultarNfseServicoPrestadoEnvio"+ cabecalho;
//    
//    private static final String cabecalho_CONSULTA_RETORNO = 
//            "<ConsultarNfseServicoPrestadoResposta"+ cabecalho;
//    
//    private static final String cabecalho_CANCELAMENTO_ENVIO = 
//            "<CancelarNfseEnvio"+ cabecalho;
//    
//    private static final String cabecalho_CANCELAMENTO_RETORNO = 
//            "<CancelarNfseResposta"+ cabecalho;
//    
//    private static final String cabecalho_MSG_RETORNO = 
//            "<ListaMensagemRetorno"+ cabecalho;    
//    
//    public static String convert(Nfse nfse) {
//        if(nfse==null){
//            return "";
//        }       
//                
//        Contribuintes contribuinte = 
//                new ContribuintesDao().getDados(nfse.getContribuinte());
//        
//        NfseBO nfseBO = new NfseBO();
//        
//        PessoasDao pessoasDao = new PessoasDao();
//        LogradourosDao logradourosDao = new LogradourosDao();
//        BairrosDao bairrosDao= new BairrosDao();
//        MunicipiosDao municipiosDao= new MunicipiosDao();
//        CnaeFiscalDao cnaeFiscalDao = new CnaeFiscalDao();
//        
//        Pessoas pessoasPref = pessoasDao.getDados(
//                new ParametrosComunsDao().getDados().getPessoaPrefeitura());
//        Pessoas pessoasPrestador = pessoasDao.getDados(contribuinte.getCodigoPessoa());
//        Pessoas pessoasToma = pessoasDao.getDados(nfse.getCpfCnpj());
//        
//        if(pessoasToma==null){//TOMADOR NAO CADASTRADO
//            pessoasToma = new Pessoas();
//        }
//        
//        DecimalFormat df = new DecimalFormat();
//        df.setMinimumFractionDigits(2);
//        df.setMaximumFractionDigits(2);
//        
//        UteisMetodos.nullPointerString(nfse);
//                       
//        String xml = cabecalhoNFSE    
//                        + "<Nfse versao=\"2.02\">" 
//                            + "<InfNfse Id=\""+(nfse.getId() > 0?nfse.getId()+"\"":"")+">" 
//                
//                                + "<Numero>" + getTsNumeroNfse(nfse)+ "</Numero>"
//                                + "<CodigoVerificacao>" + nfse.getDigitoVerificador()+ "</CodigoVerificacao>"
//                                + "<DataEmissao>" 
//                                    + UteisData.converteData(nfse.getDataEmissao()
//                                                            ,FormatData.DATA_HORA) 
//                                + "</DataEmissao>"
//                    //            + "<NfseSubstituida>" + nfse.getNfseSubstituida() + "</NfseSubstituida>"
//                                + "<OutrasInformacoes>" 
//                                    + (nfse.getInformacoes()==null ?""
//                                    :(nfse.getInformacoes().length() > 255)?
//                                        nfse.getInformacoes().substring(0, 255)
//                                        :nfse.getInformacoes()) 
//                                + "</OutrasInformacoes>"
//                                + "<ValoresNfse>" 
//                                    + "<BaseCalculo>"+ df.format(nfseBO.getValorBase(nfse))+ "</BaseCalculo>"
//                                    + "<Aliquota>" + cnaeFiscalDao.getAliquota(nfse.getCnae()) + "</Aliquota>"
//                                    + "<ValorIss>" + df.format(nfse.getIssqn()
//                                                    +nfseBO.getValorISSTomador(nfse)) + "</ValorIss>"
//                                    + "<ValorLiquidoNfse>"+ df.format(nfseBO.getValorLiquido(nfse))+ "</ValorLiquidoNfse>"
//                                + "</ValoresNfse>" 
//                //                    + "<ValorCredito>" 
//                //                    + "</ValorCredito>" 
//                                + "<PrestadorServico>" 
//                                    + "<IdentificacaoPrestador>" 
//                                        +(pessoasPrestador.getCpf()!=null && !pessoasPrestador.getCpf().isEmpty()?
//                                        "<Cpf>" + pessoasPrestador.getCpf()+ "</Cpf>":
//                                        "<Cnpj>" + UteisMetodos.nz(pessoasPrestador.getCnpj(),"") + "</Cnpj>")
//                                        + "<InscricaoMunicipal>" + nfse.getContribuinte() + "</InscricaoMunicipal>"
//                                    + "</IdentificacaoPrestador>" 
//                                    + "<RazaoSocial>" + pessoasPrestador.getNome()+ "</RazaoSocial>"
//                                    + "<NomeFantasia>" + UteisMetodos.nz(pessoasPrestador.getNomeFantasia(),"") + "</NomeFantasia>"
//                                    + "<Endereco>" 
//                                        +"<Endereco>" 
//                                         + logradourosDao.getNome(pessoasPrestador.getLogradouro())
//                                        +" QD:"+ UteisMetodos.nz(pessoasPrestador.getQuadra(),"")
//                                        +" LT:"+ UteisMetodos.nz(pessoasPrestador.getLote(),"")
//                                        + "</Endereco>"
//                                        +"<Numero>" + UteisMetodos.nz(pessoasPrestador.getNumero(),"")+ "</Numero>"
//                                        +"<Complemento>" +UteisMetodos.nz( pessoasPrestador.getComplemento(),"")+ "</Complemento>"
//                                        + "<Bairro>" + bairrosDao.getNome(pessoasPrestador.getBairro()) + "</Bairro>"
//                                        + "<CodigoMunicipio>" + pessoasPrestador.getCidade()+ "</CodigoMunicipio>"
//                                        + "<Uf>" + municipiosDao.getEstado(pessoasPrestador.getCidade())+ "</Uf>"
//        //                                + "<CodigoPais>" +""+ "</CodigoPais>"
//                                        + "<Cep>" + UteisMetodos.nz(pessoasPrestador.getCep(),"") + "</Cep>"
//                                    + "</Endereco>" 
//                                    + "<Contato>" 
//                                        + "<Telefone>" + UteisMetodos.nz(pessoasPrestador.getDddTelefone(),"")
//                                                  +UteisMetodos.nz(pessoasPrestador.getTelefone(),"") + "</Telefone>"
//                                        + "<Email>" + UteisMetodos.nz(pessoasPrestador.getEmail(),"") + "</Email>"
//                                    + "</Contato>" 
//                                + "</PrestadorServico>" 
//                                + "<OrgaoGerador>" 
//                                    + "<CodigoMunicipio>" + pessoasPref.getCidade() + "</CodigoMunicipio>"
//                                    + "<Uf>" + municipiosDao.getEstado(pessoasPref.getCidade())+ "</Uf>"
//                                + "</OrgaoGerador>" 
//                                + "<DeclaracaoPrestacaoServico>" 
//                                    + "<InfDeclaracaoPrestacaoServico>" 
//                                        +(nfse.getRps()>0 ?
//                                        "<Rps>" 
//                                            +"<IdentificacaoRps>"
//                                                +"<Numero>"
//                                                    + nfse.getRps() 
//                                                +"</Numero>"
//                                                +"<Serie>"
//                                                    + UteisMetodos.formatNumber(
//                                                            nfse.getRps(),"00000")
//                                                    + nfse.getId()
//                                                +"</Serie>"
//                                                +"<Tipo>"
//                                                    + 1
//                                                +"</Tipo>"
//                                            +"</IdentificacaoRps>"
//                                            +"<DataEmissao>"
//                                                + nfse.getDataEmissao()
//                                            +"</DataEmissao>"
//                                            +"<Status>"
//                                                + (nfse.getRps()>0 ? "1" : "2" )
//                                            +"</Status>"
//                                        + "</Rps>"
//                                                :"")
//                                        + "<Competencia>" 
//                                            +UteisData.converteData(nfse.getDataEmissao()
//                                                    ,FormatData.DATA_HORA)
//                                        + "</Competencia>" 
//                                        + "<Servico>" 
//                                            + "<Valores>" 
//                                                + "<ValorServicos>" + df.format(nfse.getValorServico()) + "</ValorServicos>"
//                                                + "<ValorDeducoes>" + df.format(nfse.getDeducoes()) + "</ValorDeducoes>"
//                                                + "<ValorPis>" + df.format(nfse.getPis()) + "</ValorPis>"
//                                                + "<ValorCofins>" + df.format(nfse.getCofins()) + "</ValorCofins>"
//                                                + "<ValorInss>" + df.format(nfse.getInss()) + "</ValorInss>"
//                                                + "<ValorIr>" + df.format(nfse.getIr()) + "</ValorIr>"
//                                                + "<ValorCsll>" + df.format(nfse.getCsll()) + "</ValorCsll>"
//        //                                        + "<OutrasRetencoes>" + "" + "</OutrasRetencoes>"
//                                                + "<ValorIss>" + df.format(nfse.getIssqn()
//                                                    +nfseBO.getValorISSTomador(nfse)) + "</ValorIss>"
//                                                + "<Aliquota>" + cnaeFiscalDao.getAliquota(nfse.getCnae()) + "</Aliquota>"
//                                                + "<DescontoIncondicionado>" + df.format(nfse.getDesconto()) + "</DescontoIncondicionado>"
//                //                                    + "<DescontoCondicionado>" + "" + "</DescontoCondicionado>"
//                                            + "</Valores>" 
//                                            + "<IssRetido>" + (nfse.isIssRetido() ? 1 : 2) + "</IssRetido>" 
//                //                                + "<ResponsavelRetencao>" 
//                //                                + "</ResponsavelRetencao>" 
//                                            + "<ItemListaServico>" + cnaeFiscalDao.getNome(nfse.getCnae()) + "</ItemListaServico>"
//                                            + "<CodigoCnae>" + nfse.getCnae() + "</CodigoCnae>"
//                                            + "<Discriminacao>" 
//                                                + (nfse.getDiscriminacao()==null ?""
//                                                :(nfse.getDiscriminacao().length() > 2000)?
//                                                    nfse.getDiscriminacao().substring(0, 2000)
//                                                    :nfse.getDiscriminacao())
//                                            + "</Discriminacao>"
//                                            + "<CodigoMunicipio>" + nfse.getMunicipioServico()+ "</CodigoMunicipio>"
//                //                                + "<CodigoPais>" + "" + "</CodigoPais>"
//                                            + "<ExigibilidadeISS>" + nfse.getExigibilidadeISS() + "</ExigibilidadeISS>"
//                                            + "<MunicipioIncidencia>" + nfse.getMunicipioServico()+ "</MunicipioIncidencia>"
//                                        + "</Servico>" 
//                                        + "<Prestador>" 
//                                            +(UteisMetodos.nz(pessoasPrestador.getCpf(),"").isEmpty()?
//                                            "<Cpf>" + pessoasPrestador.getCpf()+ "</Cpf>":
//                                            "<Cnpj>" 
//                                                + UteisMetodos.nz(pessoasPrestador.getCnpj(),"") 
//                                            + "</Cnpj>")
//                                            + "<InscricaoMunicipal>" 
//                                                + nfse.getContribuinte()
//                                            + "</InscricaoMunicipal>"
//                                        + "</Prestador>" 
//                                        + "<Tomador>" 
//                                            + "<IdentificacaoTomador>" 
//                                                +(!UteisMetodos.nz(nfse.getCpfCnpj(),"").isEmpty()
//                                                ?
//                                                "<Cpf>" + nfse.getCpfCnpj()+ "</Cpf>"
//                                                :"<Cnpj>" + UteisMetodos.nz(nfse.getCpfCnpj(),"") + "</Cnpj>"
//                                                )
//                                                + "<InscricaoMunicipal>" 
//                                                    + UteisMetodos.nz(nfse.getInscricao(),"")
//                                                + "</InscricaoMunicipal>"
//                                            + "</IdentificacaoTomador>"
//                                            + "<RazaoSocial>" + nfse.getNome()+ "</RazaoSocial>"
//                                            +"<Endereco>" 
//                                                +"<Endereco>" 
//                                                     +UteisMetodos.nz(nfse.getEndereco(),"")
//                                                + "</Endereco>"
//                                                +"<Numero>" + UteisMetodos.nz(nfse.getNumero(),"")+ "</Numero>"
//                                                +"<Complemento>" + UteisMetodos.nz(nfse.getComplemento(),"")+ "</Complemento>"
//                                                + "<Bairro>" + UteisMetodos.nz(nfse.getBairro(),"") + "</Bairro>"
//                                                + "<CodigoMunicipio>" + nfse.getMunicipioTomador()+ "</CodigoMunicipio>"
//                                                + "<Uf>" + municipiosDao.getEstado(nfse.getMunicipioTomador())+ "</Uf>"
//        //                                        + "<CodigoPais>" +""+ "</CodigoPais>"
//                                                + "<Cep>" + UteisMetodos.nz(nfse.getCep(),"") + "</Cep>"
//                                            + "</Endereco>"
//
//                                            +"<Contato>"     
//                                                + "<Telefone>" + UteisMetodos.nz(pessoasToma.getDddTelefone(),"")
//                                                            +UteisMetodos.nz(pessoasToma.getTelefone(),"") + "</Telefone>"
//                                                + "<Email>" + UteisMetodos.nz(pessoasToma.getEmail(),"") + "</Email>"
//                                            + "</Contato>"    
//                                        + "</Tomador>" 
//                                        + "<OptanteSimplesNacional>" 
//                                            + (nfse.getSimplesNacional() ? 1 : 2)
//                                        + "</OptanteSimplesNacional>" 
//                                        + "<IncentivoFiscal>" +2+ "</IncentivoFiscal>"                                         
//                                    + "</InfDeclaracaoPrestacaoServico>" 
//                                + "</DeclaracaoPrestacaoServico>" 
//                            + "</InfNfse>" 
//                        + "</Nfse>" 
//                    + "</CompNfse>";
//        
//        xml = UteisMetodos.removerAcentos(xml);
////        xml = xml.replaceAll("null", "");
//        
//        return xml;
//    }
//
//    /**FONTE ABRASF FORMATO DO NUMERO NFSE*/
//    public static String getTsNumeroNfse(int ano,int numeroNota) {
//        String anoStr = UteisMetodos.formatNumber(numeroNota, "0000");
//        String serie = UteisMetodos.formatNumber(numeroNota, "00000000000");
//        
//        return anoStr+serie;
//    }
//    /**FONTE ABRASF FORMATO DO NUMERO NFSE*/
//    public static String getTsNumeroNfse(Nfse obj) {
//        if (obj == null) {
//            return "";
//        }
//        
//        int anoNota = UteisData.getYear(obj.getDataEmissao());
//        
//        String serie = UteisMetodos.formatNumber(obj.getNumeroNfse(), "00000000000");
//        
//        return anoNota+serie;
//    }
//    
//    public static String convertRetorno(NfseSolicitacaoCancelamento obj) {
//        Nfse nfse = new NfseDao().getDados(obj.getIdNota());
//        if (nfse == null) {
//            return "";
//        }
//
//        String numero = getTsNumeroNfse(nfse);
//        String cpfCnpj = new ContribuintesDao().getCpfCnpjResponsavel(nfse.getContribuinte());
//        int inscricaoMunicipal = nfse.getContribuinte();
//        int codigoMunicipio = nfse.getMunicipioServico();
//        
//        String xml = cabecalho_CANCELAMENTO_RETORNO
//                + "<RetCancelamento>"
//                    + "<NfseCancelamento>"
//                        + "<Confirmacao>"
//                            + "<Pedido>"
//                                + "<InfPedidoCancelamento Id=\""+obj.getId()+"\">"
//                                    +"<IdentificacaoNfse>"
//                                        +"<Numero>"
//                                            +numero
//                                        +"</Numero>"
//                                        +"<CpfCnpj>"
//                                            +cpfCnpj
//                                        +"</CpfCnpj>"
//                                        +"<InscricaoMunicipal>"
//                                            +inscricaoMunicipal
//                                        +"</InscricaoMunicipal>"
//                                        +"<CodigoMunicipio>"
//                                            +codigoMunicipio
//                                        +"</CodigoMunicipio>"
//                                    +"</IdentificacaoNfse>"
//
//                                    +"<CodigoCancelamento>"
//                                    +"</CodigoCancelamento>"
//                                + "</InfPedidoCancelamento>"
//                            + "</Pedido>"
//                            + "<DataHora>"
//                                + UteisData.converteData(obj.getData()
//                                                        ,FormatData.DATA_HORA)
//                            + "</DataHora>"
//                        + "</Confirmacao>"
//                    + "</NfseCancelamento>"
//                + "</RetCancelamento>"
//            + "</CancelarNfseResposta>";
//
//        return xml;
//    }
//    
//    public static String convertEnvio(NfseSolicitacaoCancelamento obj) {
//        Nfse nfse = new NfseDao().getDados(obj.getContribuinte(),obj.getIdNota());
//        if (nfse == null) {
//            return "";
//        }
//        
//        String numero = getTsNumeroNfse(nfse);
//        String cpfCnpj = new ContribuintesDao().getCpfCnpjResponsavel(nfse.getContribuinte());
//        int inscricaoMunicipal = nfse.getContribuinte();
//        int codigoMunicipio = nfse.getMunicipioServico();
//        
//        String xml = cabecalho_CANCELAMENTO_ENVIO
//                    +"<Pedido>"
//                        +"<InfPedidoCancelamento>"
//                            +"<IdentificacaoNfse>"
//                                +"<Numero>"
//                                    +numero
//                                +"</Numero>"
//                                +"<CpfCnpj>"
//                                    +cpfCnpj
//                                +"</CpfCnpj>"
//                                +"<InscricaoMunicipal>"
//                                    +inscricaoMunicipal
//                                +"</InscricaoMunicipal>"
//                                +"<CodigoMunicipio>"
//                                    +codigoMunicipio
//                                +"</CodigoMunicipio>"
//                            +"</IdentificacaoNfse>"
//
//                            +"<CodigoCancelamento>"
//                            +"</CodigoCancelamento>"
//                        +"</InfPedidoCancelamento>"
//                    +"</Pedido>"
//                +"</CancelarNfseEnvio>";
//                
//        return xml;
//    }
//    
//    public static String convertNfseConsultaRetorno(Nfse obj) {
//        String xml = convert(obj);
//        
//        return cabecalho_CONSULTA_RETORNO
//                +"<ListaNfse>" 
//                    + xml
//                + "</ListaNfse>"
//            + "</ConsultarNfseServicoPrestadoResposta>";
//    }
//    
//    public static String convertNfseConsultaEnvio(int contribuinte,int numeroNota) {
//        String cpfCnpj = new ContribuintesDao().getCpfCnpjResponsavel(contribuinte);
//        
//        int anoNota = 0;
//        Nfse nfse = new NfseDao().getDados(contribuinte, numeroNota);
//        if (nfse != null) {
//            anoNota = UteisData.getYear(nfse.getDataEmissao());
//        }
//        
//        String xml = cabecalho_CONSULTA_ENVIO
//                    +"<Prestador>"
//                        +"<CpfCnpj>"
//                            +cpfCnpj
//                        +"</CpfCnpj>"
//                        +"<InscricaoMunicipal>"
//                            +contribuinte
//                        +"</InscricaoMunicipal>"
//                    +"</Prestador>"
//                    +"<NumeroNfse>"
//                        +getTsNumeroNfse(anoNota, numeroNota)
//                    +"</NumeroNfse>"
//            +"</ConsultarNfseServicoPrestadoEnvio>";
//                
//        return xml;
//    }
//    public static String getMensagemErros(String xmlTag) {
//        String msg = getValueXML(xmlTag, "Mensagem");
//        
//        return msg;
//    }
//    
//    public static String getMensagemRetorno(NfseErros nfseErros) {
//        if (nfseErros == null) {
//            nfseErros = NfseErros.Erro;
//        }
//        String xml = cabecalho_MSG_RETORNO
//                    +"<MensagemRetorno>"
//                        +"<Codigo>"
//                            +(NfseErros.Vazio.equals(nfseErros)?"":nfseErros.name())
//                        +"</Codigo>"
//                        +"<Mensagem>"
//                            +nfseErros.mensagem
//                        +"</Mensagem>"
//                        +"<Correcao>"
//                            +nfseErros.correcao
//                        +"</Correcao>"
//                    +"</MensagemRetorno>"
//                +"</ListaMensagemRetorno>";
//        
//        return xml;
//    }
//    
//    private static String getValueXML(String xml,String... tags) {
//        return getValueXML(true,xml, tags);
//    }
//    private static String getValueXML(boolean lastIndex,String xml,String... tags) {
//        if (xml == null || xml.isEmpty()) {
//            return "";
//        }
//        String value = xml;
//        
//        for (String tag : tags) {
//            tag = tag.toLowerCase();
//            
//            int ini = value.toLowerCase().indexOf("<" + tag.toLowerCase() + ">");
//            if(ini==-1){
//                return "";
//            }
//            ini += ("<" + tag + ">").length();
//            
//            int fim;
//            if (lastIndex) {
//                fim = value.toLowerCase().lastIndexOf("</" + tag.toLowerCase() + ">");
//            }else{
//                fim = value.toLowerCase().indexOf("</" + tag.toLowerCase() + ">");
//            }
//
//            value = value.substring(ini, fim);
//        }
//        
//        return value;
//    }
//    
//    public static Nfse convertNfse(File fileXml) {
//        if (fileXml == null) {
//            System.out.println("fileXml==null");
//            return null;
//        }
//        if (!fileXml.exists()) {
//            System.out.println("fileXml nao existe");
//            return null;
//        }
//        if (fileXml.isDirectory()) {
//            System.out.println("fileXml nao e arquivo xml");
//            return null;
//        }
//        String xml = UteisFile.readFileTXT(fileXml);
//
//        return convertNfse(xml);
//    }
//
//    public static NfseSolicitacaoCancelamento convertNfseSolicCancel(String xml) {
//        NfseSolicitacaoCancelamento obj = new NfseSolicitacaoCancelamento();
//        
//        if (xml == null || xml.isEmpty()) {
//            System.out.println("xml==null || xml.isEmpty()");
//            return null;
//        }
//
//        String idNota = UteisMetodos.nz(getValueXML(xml, "Numero"),"");
//        idNota = idNota.substring(4);//NUMERO FORMATADO CONFORME PADRAO ABRASF 2014000000000001
//        obj.setIdNota(new BigInteger(idNota).intValue());
//        
//        int inscricao = UteisMetodos.nz(getValueXML(xml, "InscricaoMunicipal"),0);
//        obj.setContribuinte(inscricao);
//        
//        if(inscricao <=0){
//            String cpfCnpj = UteisMetodos.nz(getValueXML(xml, "CpfCnpj"),"");
//            inscricao =  new PessoasDao().getInscricaoMunicipal(cpfCnpj);
//            obj.setContribuinte(inscricao);
//        }
//        
//        obj.setMotivo("SOLICITACAO WEBSERVICE");//NAO HA CAMPO NO XML
//        
//        return obj;
//    }
//    
//    public static int getContribuinte(String xml) {
//        return UteisMetodos.nz(getValueXML(xml, "Prestador","InscricaoMunicipal"),0);
//    }
//    public static int getNumeroNota(String xml,String... tags){
//        String value = getValueXML(xml, tags);
//        
//        if(value==null || value.isEmpty()){
//            return 0;
//        }
//        if(value.length()!=15){
//            System.out.println(XmlNfse.class.getName()+" getNumeroNota!=15");
//            return 0;
//        }
//        value = value.substring(4);
//        
//        return UteisMetodos.nz(value,0);
//    }
//    
//    public static Nfse convertNfse(String xml) {
//        Nfse obj = new Nfse();
//        
//        if(xml==null || xml.isEmpty()){
//            System.out.println(XmlNfse.class.getName()+" xml==null || xml.isEmpty()");
//            return null;
//        }                
//        String value ;
//        
////        obj.setId(UteisMetodos.nz(getValueXML(xml, "Id"),0));
//        
//        obj.setNumeroNfse(getNumeroNota(xml, "InfNfse","Numero"));
//        
//        obj.setRps(UteisMetodos.nz(getValueXML(xml
//                                            , "Rps","Numero"),0));
//        
//        obj.setContribuinte(UteisMetodos.nz(getValueXML(xml
//                ,"PrestadorServico" ,"InscricaoMunicipal"),0));
//      
//        String dt1 = getValueXML(xml,"InfNfse","DataEmissao");
//        Date dt2 = UteisData.converteData(dt1, FormatData.DATA_HORA);
//        obj.setDataEmissao(dt2);
//
//        obj.setExigibilidadeISS(UteisMetodos.nz(
//                getValueXML(xml, "ExigibilidadeISS"), 0));
//        
//        obj.setIssRetido(UteisMetodos.nz(getValueXML(xml, "IssRetido"), 0)==1);
//        
//        String cpfCnpj;
//        cpfCnpj = getValueXML(xml, "Tomador","Cpf");
//        if (cpfCnpj.isEmpty()) {
//            cpfCnpj = getValueXML(xml, "Tomador", "cnpj");
//        }
//        obj.setCpfCnpj(cpfCnpj);
//        
//        obj.setInscricao(UteisMetodos.nz(
//                getValueXML(xml,"Tomador" ,"InscricaoMunicipal"),""));
//        
//        obj.setNome(getValueXML(xml,"Tomador" ,"RazaoSocial"));
//        obj.setEndereco(getValueXML(xml,"Tomador","Endereco","Endereco"));
//        obj.setNumero(getValueXML(xml, "Tomador","Numero"));
//        obj.setComplemento(getValueXML(xml, "Tomador","Complemento"));
//        obj.setBairro(getValueXML(xml, "Tomador","Bairro"));
//        obj.setCep(getValueXML(xml, "Tomador","Cep"));
//        obj.setMunicipioTomador(
//            UteisMetodos.nz(getValueXML(xml,"Tomador", "CodigoMunicipio"), 0));
//        
//        //99999 EXTERIOR
//        obj.setResidenteForaPais(obj.getMunicipioTomador()==99999 ? "S" : "N");
//        
//        obj.setEstado(getValueXML(xml, "Tomador","Uf"));
//        
//        obj.setMunicipioServico(
//            UteisMetodos.nz(getValueXML(xml,"Servico", "MunicipioIncidencia"), 0));
//        
//        obj.setServicoForaPais(obj.getMunicipioServico()==99999 ? "S" : "N");
//        
//        String UF = new MunicipiosDao().getEstado(obj.getMunicipioServico());
//        obj.setEstadoServico(UF);//XML DA NOTA NAO POSSUI ESTE CAMPO
//        
//        obj.setCnae(UteisMetodos.nz(getValueXML(xml,"Servico", "CodigoCnae"), 0));
//        obj.setDiscriminacao(getValueXML(xml, "Servico","Discriminacao"));
//        obj.setInformacoes(getValueXML(xml, "OutrasInformacoes"));
//        
//        value = getValueXML(xml, "ValorServicos")
//                .replaceAll(",", ".");
//        obj.setValorServico(UteisMetodos.nz(value, 0d));
//        
//        value = getValueXML(xml, "DescontoIncondicionado")
//                .replaceAll(",", ".");
//        obj.setDesconto(UteisMetodos.nz(value, 0d));
//
//        value = getValueXML(xml, "Basecalculo")
//                .replaceAll(",", ".");
//        obj.setBaseCalculo(UteisMetodos.nz(value, 0d));
//        
//        value = getValueXML(xml, "ValorDeducoes")
//                .replaceAll(",", ".");
//        obj.setDeducoes(UteisMetodos.nz(value, 0d));
//        
//        value = getValueXML(xml, "ValoresNfse","ValorIss")
//                .replaceAll(",", ".");
//        obj.setIssqn(UteisMetodos.nz(value, 0d));
//        
//        obj.setIssqnTomador(obj.isIssRetido()? obj.getIssqn() : 0);
//        
//        value = getValueXML(xml, "ValorPis")
//                .replaceAll(",", ".");
//        obj.setPis(UteisMetodos.nz(value, 0d));
//        
//        value = getValueXML(xml, "ValorCofins")
//                .replaceAll(",", ".");
//        obj.setCofins(UteisMetodos.nz(value, 0d));
//        
//        value = getValueXML(xml,"ValoresNfse", "Aliquota")
//                .replaceAll(",", ".");
//        obj.setAliquota(UteisMetodos.nz(value,0d));
//        
//        value = getValueXML(xml, "ValorInss")
//                .replaceAll(",", ".");
//        obj.setInss(UteisMetodos.nz(value,0d));
//        
//        value = getValueXML(xml, "ValorIr")
//                .replaceAll(",", ".");
//        obj.setIr(UteisMetodos.nz(value,0d));
//        
//        value = getValueXML(xml, "ValorCsll")
//                .replaceAll(",", ".");
//        obj.setCsll(UteisMetodos.nz(value,0d));
//        
//        obj.setSimplesNacional(
//                UteisMetodos.nz(getValueXML(xml, "OptanteSimplesNacional"),0)==1);
//        return obj;
//    }
}
