/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import br.ufscar.dc.compiladores.algumasemantico4.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser;
import static br.ufscar.dc.compiladores.algumasemanticot4.AlgumaSemanticoT4Utils.adicionarErrorsSemanticos;
import br.ufscar.dc.compiladores.algumasemanticot4.TabelaDeSimbolos.TipoDeclaracao;
import br.ufscar.dc.compiladores.algumasemanticot4.TabelaDeSimbolos.TipoFuncao;
import static br.ufscar.dc.compiladores.algumasemanticot4.AlgumaSemanticoT4Utils.verificarTipo;

import java.util.ArrayList;
import java.util.HashMap;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author ana
 */
public class AlgumaSemanticoT4 extends AlgumaBaseVisitor {
    
    Escopo escopo = new Escopo();
    static HashMap<String, ArrayList<TipoDeclaracao>> dadosFuncao = new HashMap<>();   
    HashMap<String, ArrayList<String>> tabelaRegistro = new HashMap<>();
        
    @Override
    public Object visitPrograma(AlgumaParser.ProgramaContext ctx) {

        //Erro caso haja um "return" fora do escopo principal
        for (AlgumaParser.CmdContext c : ctx.corpo().cmd())
            if (c.cmdRetorne() != null)
                adicionarErrorsSemanticos(c.getStart(), "comando retorne nao permitido nesse escopo");

        return super.visitPrograma(ctx);
    }
    
    public static TipoDeclaracao confereTipo (HashMap<String, ArrayList<String>> tabela, String tipoRetorno) {
        TipoDeclaracao tipoAux;

        // Remoção do ponteiro.
        if (tipoRetorno.charAt(0) == '^') {
            tipoRetorno = tipoRetorno.substring(1);
        }

        if (tabela.containsKey(tipoRetorno))
            tipoAux = TipoDeclaracao.REGISTRO;
        else if (tipoRetorno.equals("literal"))
            tipoAux = TipoDeclaracao.LITERAL;
        else if (tipoRetorno.equals("inteiro"))
            tipoAux = TipoDeclaracao.INTEIRO;
        else if (tipoRetorno.equals("real"))
            tipoAux = TipoDeclaracao.REAL;
        else if (tipoRetorno.equals("logico"))
            tipoAux = TipoDeclaracao.LOGICO;
        else
            tipoAux = TipoDeclaracao.INVALIDO;

        return tipoAux;
    }
    
    public void adicionaSimboloTabela(String nome, String tipo, Token nomeToken, Token tipoToken, TipoFuncao tipoE) {

        TipoDeclaracao tipoItem;

        // Remove simbolo de ponteira se tiver
        if (tipo.charAt(0) == '^')
            tipo = tipo.substring(1);

        // Checagem do tipo
        switch (tipo) {
            case "literal":
                tipoItem = TipoDeclaracao.LITERAL;
                break;
            case "inteiro":
                tipoItem = TipoDeclaracao.INTEIRO;
                break;
            case "real":
                tipoItem = TipoDeclaracao.REAL;
                break;
            case "logico":
                tipoItem = TipoDeclaracao.LOGICO;
                break;
            case "void":
                tipoItem = TipoDeclaracao.VOID;
                break;
            case "registro":
                tipoItem = TipoDeclaracao.REGISTRO;
                break;
            default:
                // Nesse caso, o tiipo é invalido, portanto, lançamos erro
                tipoItem = TipoDeclaracao.INVALIDO;
                adicionarErrorsSemanticos(tipoToken, "tipo " + tipo + " nao declarado");
                break;
        }

        // Erro de não declaração se não exitir no escopo
        if (!escopo.obterEscopoAtual().existe(nome))
            escopo.obterEscopoAtual().adicionar(nome, tipoItem, tipoE);
        else
            adicionarErrorsSemanticos(nomeToken, "identificador " + nome + " ja declarado anteriormente");
    }

    // Visita declaracao local que pode ser 3 tipos (constante, variavel ou tipo)
    @Override
    public Object visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {  
        String tipoVariavel;
        String nomeVariavel;
        
        // Declaracao de variavel
        if(ctx.getChild(0).getText().equals("declare")) {
            // Caso em que é um registro
            if (ctx.variavel().tipo().registro() != null) {
                // Itera lista de registro declaradas e adiciona ao escopo 
                for (AlgumaParser.IdentificadorContext ic : ctx.variavel().identificador()) {
                    adicionaSimboloTabela(ic.getText(), "registro", ic.getStart(), null, TipoFuncao.OUTRO);
                    // Adiciona as variaveis desse registro com seu tipo
                    for (AlgumaParser.VariavelContext vc : ctx.variavel().tipo().registro().variavel()) {
                        tipoVariavel = vc.tipo().getText();
                        for (AlgumaParser.IdentificadorContext icr : vc.identificador()){
                            adicionaSimboloTabela(ic.getText() + "." + icr.getText(), tipoVariavel, icr.getStart(), vc.tipo().getStart(), TipoFuncao.OUTRO);
                        }
                    }
                }
            }
            else {
                tipoVariavel = ctx.variavel().tipo().getText(); 
                // Testa se é um tipo que foi declarado dentro de um registro 
                if (tabelaRegistro.containsKey(tipoVariavel)) {
                    ArrayList<String> variaveisRegistro = tabelaRegistro.get(tipoVariavel);

                    for (AlgumaParser.IdentificadorContext ic : ctx.variavel().identificador()) {
                        nomeVariavel = ic.IDENT().get(0).getText();
                        // Lanca erro se já foi declarado
                        if (escopo.obterEscopoAtual().existe(nomeVariavel) || tabelaRegistro.containsKey(nomeVariavel)) {
                            adicionarErrorsSemanticos(ic.getStart(), "identificador " + nomeVariavel + " ja declarado anteriormente");
                        // Adiciona a tabela se não 
                        } else {  
                            adicionaSimboloTabela(nomeVariavel, "registro", ic.getStart(), ctx.variavel().tipo().getStart(), TipoFuncao.OUTRO);                            
                            for (int i = 0; i < variaveisRegistro.size(); i = i + 2) {
                                adicionaSimboloTabela(nomeVariavel + "." + variaveisRegistro.get(i), variaveisRegistro.get(i+1), ic.getStart(), ctx.variavel().tipo().getStart(), TipoFuncao.OUTRO);
                            }
                        }
                    }
                // Caso não seja uma variável dentro de um registro (variavel normal)
                } else {
                    for (AlgumaParser.IdentificadorContext ident : ctx.variavel().identificador()) {
                        nomeVariavel = ident.getText();

                        // Verifica se a declaração atual é o nome de uma função ou procedimento.
                        if (dadosFuncao.containsKey(nomeVariavel))
                            adicionarErrorsSemanticos(ident.getStart(), "identificador " + nomeVariavel + " ja declarado anteriormente");
                        else
                            adicionaSimboloTabela(nomeVariavel, tipoVariavel, ident.getStart(), ctx.variavel().tipo().getStart(), TipoFuncao.OUTRO); 
                    }
                }
            }
        }
        // Declaracao de constante
        else if (ctx.getChild(0).getText().equals("constante")){
            adicionaSimboloTabela(ctx.IDENT().getText(), ctx.tipo_basico().getText(), ctx.IDENT().getSymbol(), ctx.IDENT().getSymbol(), TipoFuncao.OUTRO);
        }
        // Declaracao de tipo 
        else if (ctx.getChild(0).getText().equals("tipo")) { 
            //Caso em que é registro 
            if (ctx.tipo().registro() != null) {
                ArrayList<String> variaveisRegistro = new ArrayList<>();
                //Coloca todas as variaveis (nome e tipo) do registro em uma lista 
                for (AlgumaParser.VariavelContext vc : ctx.tipo().registro().variavel()) {
                    tipoVariavel = vc.tipo().getText();

                    for (AlgumaParser.IdentificadorContext ic : vc.identificador()) {
                        variaveisRegistro.add(ic.getText());
                        variaveisRegistro.add(tipoVariavel);
                    }
                }
                //Adiciona variaveis à tabela de registros 
                tabelaRegistro.put(ctx.IDENT().getText(), variaveisRegistro);
            }
        }
        return super.visitDeclaracao_local(ctx);
    }
    
    // Visita declaracoes global, que pode conter procedimento ou funcao
    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        // Primeiramente, cria uum novo escopo para essa função / escopo
        escopo.criarNovoEscopo();

        ArrayList<TipoDeclaracao> tiposVariaveis = new ArrayList<>();
        ArrayList<String> variaveisRegistro;

        String tipoVariavel;
        TipoDeclaracao tipoAux;

        // Caso seja proedimento
        if (ctx.getText().contains("procedimento")) {

            for (AlgumaParser.ParametroContext parametro : ctx.parametros().parametro()) {
                //Adicona parametros do procedimento à tabela e a lista de variáveis auxiliar
                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                    adicionaSimboloTabela(parametro.identificador().get(0).getText(), parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), parametro.getStart(), parametro.getStart(), TipoFuncao.OUTRO);
                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);
                } 
                // Caso parâmetro do procedimento seja um registro
                else if (tabelaRegistro.containsKey(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText())) {
                    // Recupera os elementos do registro.
                    variaveisRegistro = tabelaRegistro.get(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText());

                    // Adiciona os tipos do registro para adicionar a tabela de procedimento
                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);

                    for (AlgumaParser.IdentificadorContext ic : parametro.identificador()){
                        // Adiciona cada variável do registro à tabela de simbolos do escopo 
                        for (int i = 0; i < variaveisRegistro.size(); i = i + 2){
                            adicionaSimboloTabela(ic.getText() + "." + variaveisRegistro.get(i), variaveisRegistro.get(i + 1), ic.getStart(), ic.getStart(), TipoFuncao.OUTRO);                 
                        }
                    }
                } 
                // Erro caso tipo não tiver sido declarado 
                else{
                    adicionarErrorsSemanticos(parametro.getStart(), "tipo nao declarado");  
                    System.out.println("Foi aqui 3");
                }
            }
            // Erro de return dentro de procedimento
            for (AlgumaParser.CmdContext c : ctx.cmd())    
                if (c.cmdRetorne() != null)  
                    adicionarErrorsSemanticos(c.getStart(), "comando retorne nao permitido nesse escopo");    

            // Adiciona o nome do procedimento e os tipos dos parâmetros na tabela de dados de funcao 
            dadosFuncao.put(ctx.IDENT().getText(), tiposVariaveis);

        // Caso seja uma função (praticamente o mesmo processo que procedimento mas com tipo funcao sendo adicionado
        } else if (ctx.getText().contains("funcao")) {
            
            for (AlgumaParser.ParametroContext parametro : ctx.parametros().parametro()) {

                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                    
                    adicionaSimboloTabela(parametro.identificador().get(0).getText(), parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), parametro.getStart(), parametro.getStart(), TipoFuncao.OUTRO);

                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);
                } else if (tabelaRegistro.containsKey(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText())) {

                    variaveisRegistro = tabelaRegistro.get(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText());

                    tipoVariavel = parametro.tipo_estendido().tipo_basico_ident().IDENT().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);

                    for (AlgumaParser.IdentificadorContext ic : parametro.identificador())
                        for (int i = 0; i < variaveisRegistro.size(); i = i + 2) {
                            adicionaSimboloTabela(ic.getText() + "." + variaveisRegistro.get(i), variaveisRegistro.get(i + 1), ic.getStart(), ic.getStart(), TipoFuncao.OUTRO);
                        }
                } 
                else{
                    adicionarErrorsSemanticos(parametro.getStart(), "tipo nao declarado");
                    System.out.println("Foi aqui 6");
                }
            }

            // Adiciona o nome da função e os tipos dos parâmetros na tabela de funcao
            dadosFuncao.put(ctx.IDENT().getText(), tiposVariaveis);
        }

        super.visitDeclaracao_global(ctx);

        // Desempilha o escopo atual
        escopo.abandonarEscopo();

        // Adiciona o nome do procedimento na tabela
        if (ctx.getText().contains("procedimento")){    
            adicionaSimboloTabela(ctx.IDENT().getText(), "void", ctx.getStart(), ctx.getStart(), TipoFuncao.PROCEDIMENTO);
        }
        // Adiciona o nome da funcao na tabela
        else if (ctx.getText().contains("funcao")){
            adicionaSimboloTabela(ctx.IDENT().getText(), ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), ctx.getStart(), ctx.getStart(), TipoFuncao.FUNCAO);
        }
        return null;
    }
    
    /*
    @Override
    public Object visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx) {
        // itera todos os os escopos para verificar se o tipo ja foi declarado
        if (ctx.IDENT() != null) {
            for (TabelaDeSimbolos escopoAtual : escopo.percorrerEscoposAninhados()) {
                //Joga erro se nao achar
                if (!escopoAtual.existe(ctx.IDENT().getText())) {
                    adicionarErrorsSemanticos(ctx.start, "tipo " + ctx.IDENT().getText() + " nao declarado");
                    System.out.println("Foi aqui 8");
                }
            }
        }
        return super.visitTipo_basico_ident(ctx);
    }
    */
    // Visitador de comando leia
    @Override
    public Object visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        for (AlgumaParser.IdentificadorContext id : ctx.identificador()) 
            // Caso variavel lida não tenha sido declarada -> adiciona erro 
            if (!escopo.obterEscopoAtual().existe(id.getText())){
                System.out.println("Aqui");
                adicionarErrorsSemanticos(id.getStart(), "identificador " + id.getText() + " nao declarado");
            }

        return super.visitCmdLeia(ctx);
    }

    // Visitador de comando escreve 
    @Override
    public Object visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        TipoDeclaracao tipo;

        for (AlgumaParser.ExpressaoContext expressao : ctx.expressao())
            tipo = verificarTipo(escopo, expressao);

        return super.visitCmdEscreva(ctx);
    }

    // Visitador de comando enquanto 
    @Override
    public Object visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        TipoDeclaracao tipo = verificarTipo(escopo, ctx.expressao());

        return super.visitCmdEnquanto(ctx);
    }

    // Visitador de comando se 
    @Override
    public Object visitCmdSe(AlgumaParser.CmdSeContext ctx) {
        TipoDeclaracao tipo = verificarTipo(escopo, ctx.expressao());

        return super.visitCmdSe(ctx);
    }
    
    // Visitador de comando atribuição -> Testa se uma atribuicao é valida
    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        TipoDeclaracao tipoExpressao = verificarTipo(escopo, ctx.expressao());
        String nome = ctx.identificador().getText();
        if (tipoExpressao != TipoDeclaracao.INVALIDO) {
            //Erro de variavel nao declarada na atribuição 
            if (!escopo.obterEscopoAtual().existe(nome))
                adicionarErrorsSemanticos(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
            else {
                TipoDeclaracao varTipo = verificarTipo(escopo, nome);
                
                if (varTipo == TipoDeclaracao.INTEIRO || varTipo == TipoDeclaracao.REAL) {
                    // Verifica se a variável atual é um ponteiro para apresentar uma mensagem personalizada
                    // para este caso.
                    if (ctx.getText().contains("ponteiro")) {
                        if (!(varTipo == TipoDeclaracao.INTEIRO && tipoExpressao == TipoDeclaracao.REAL) && !(varTipo == TipoDeclaracao.REAL && tipoExpressao == TipoDeclaracao.INTEIRO) && !(varTipo == TipoDeclaracao.REAL && tipoExpressao == TipoDeclaracao.REAL))
                            // Caso o tipo da expressão (restante da parcela sendo analisada) seja diferente de inteiro,
                            // não é possível tratar o valor como um número real, logo, os tipos são incompatíveis, pois
                            // seria a situação de estar comparando um número com um literal, por exemplo.
                            if (tipoExpressao != TipoDeclaracao.INTEIRO){
                                adicionarErrorsSemanticos(ctx.identificador().getStart(), "atribuicao nao compativel para ^" + ctx.identificador().getText());
                            }
                    } 
                    else if (!(varTipo == TipoDeclaracao.INTEIRO && tipoExpressao == TipoDeclaracao.REAL) && !(varTipo == TipoDeclaracao.REAL && tipoExpressao == TipoDeclaracao.INTEIRO) && !(varTipo == TipoDeclaracao.REAL && tipoExpressao == TipoDeclaracao.REAL))
                        if (tipoExpressao != TipoDeclaracao.INTEIRO){
                            adicionarErrorsSemanticos(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());               
                        }
                }
                // Caso a expressão analisada não tenha números que precisem ser tratados de maneira especial,
                // apenas verifica se os tipos são diferentes.
                else if (varTipo != tipoExpressao){
                    adicionarErrorsSemanticos(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());
                }
            }
        }

        return super.visitCmdAtribuicao(ctx);
    }

}
