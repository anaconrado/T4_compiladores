/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import br.ufscar.dc.compiladores.algumasemantico4.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser;
import static br.ufscar.dc.compiladores.algumasemanticot4.AlgumaSemanticoT4Utils.adicionarErrorsSemanticos;
import br.ufscar.dc.compiladores.algumasemanticot4.TabelaDeSimbolos.TipoDeclaracao;

/**
 *
 * @author ana
 */
public class AlgumaSemanticoT4 extends AlgumaBaseVisitor {
    
    Escopo escopo = new Escopo();

    // Visita declaracao local que pode ser 3 tipos (constante, variavel ou tipo)
    @Override
    public Object visitDeclaracao_local(AlgumaParser.Declaracao_localContext ctx) {  
        TabelaDeSimbolos escopoAtual = escopo.obterEscopoAtual();
        
        // Declaracao de variavel
        if(ctx.getChild(0).getText().equals("declare")) {
            //Itera variaveis declaradas
            for (AlgumaParser.IdentificadorContext id : ctx.variavel().identificador()) {
                // Adiciona erro se constante ja existe no escopo atual
                if (escopoAtual.existe(id.getText())) {
                    adicionarErrorsSemanticos(id.start, "identificador " + id.getText() + " ja declarado anteriormente");
                } 
                // Se nao, adiciona no escopo com seu tipo
                else {
                    TipoDeclaracao tipo = null;
                    String tipoTexto = ctx.variavel().tipo().getText();

                    if (tipoTexto.equals("literal")) {
                        tipo = TipoDeclaracao.LITERAL;
                    } else if (tipoTexto.equals("inteiro")) {
                        tipo = TipoDeclaracao.INTEIRO;
                    } else if (tipoTexto.equals("real")) {
                        tipo = TipoDeclaracao.REAL;
                    } else if (tipoTexto.equals("logico")) {
                        tipo = TipoDeclaracao.LOGICO;
                    }
                    //Se nao for um dos tipos no if o tipo guardado e null
                    escopoAtual.adicionar(id.getText(), tipo);
                    //System.out.println("Variable: " + id.getText() + " Type: " + tipo);   
                }
            }
        }
        // Declaracao de constante
        else if (ctx.getChild(0).getText().equals("constante")){
            // Adiciona erro se constante ja existe no escopo atual
            if (escopoAtual.existe(ctx.IDENT().getText())) {
                adicionarErrorsSemanticos(ctx.start, "constante " + ctx.IDENT().getText() + " ja declarado anteriormente");
            // Se nao, adiciona no escopo com seu tipo
            } else {
                TipoDeclaracao tipo = null;
                String tipoBasicoTexto = ctx.tipo_basico().getText();

                if (tipoBasicoTexto.equals("literal")) {
                    tipo = TipoDeclaracao.LITERAL;
                } else if (tipoBasicoTexto.equals("inteiro")) {
                    tipo = TipoDeclaracao.INTEIRO;
                } else if (tipoBasicoTexto.equals("real")) {
                    tipo = TipoDeclaracao.REAL;
                } else if (tipoBasicoTexto.equals("logico")) {
                    tipo = TipoDeclaracao.LOGICO;
                }
                //Se nao for um dos tipos no if o tipo guardado e null
                escopoAtual.adicionar(ctx.IDENT().getText(), tipo);
            }
        }
        
        // Declaracao de tipo 
        else if (ctx.getChild(0).getText().equals("tipo")) {
            // Adiciona erro se constante ja existe no escopo atual
            if (escopoAtual.existe(ctx.IDENT().getText())) {
                adicionarErrorsSemanticos(ctx.start, "tipo " + ctx.IDENT().getText() + " declarado duas vezes num mesmo escopo");
               // Se nao, adiciona no escopo com seu tipo que sempre sera "tipo" 
            } else {
                escopoAtual.adicionar(ctx.IDENT().getText(), TipoDeclaracao.TIPO);
            }
        }
        return super.visitDeclaracao_local(ctx);
    }
    
    // Visita declaracoes global, que pode conter procedimento ou funcao
    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        TabelaDeSimbolos escopoAtual = escopo.obterEscopoAtual();
        // Testa se procedimento ou funcao ja existe no escopo e o adiciona se nao ou joga erro se sim
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            adicionarErrorsSemanticos(ctx.start, ctx.IDENT().getText() + " ja declarado anteriormente");
        } 
        else {
            escopoAtual.adicionar(ctx.IDENT().getText(), TipoDeclaracao.TIPO);
        }
        return super.visitDeclaracao_global(ctx);
    }
    
    
    @Override
    public Object visitTipo_basico_ident(AlgumaParser.Tipo_basico_identContext ctx) {
        // itera todos os os escopos para verificar se o tipo ja foi declarado
        if (ctx.IDENT() != null) {
            for (TabelaDeSimbolos escopoAtual : escopo.percorrerEscoposAninhados()) {
                //Joga erro se nao achar
                if (!escopoAtual.existe(ctx.IDENT().getText())) {
                    adicionarErrorsSemanticos(ctx.start, "tipo " + ctx.IDENT().getText() + " nao declarado");
                }
            }
        }
        return super.visitTipo_basico_ident(ctx);
    }
    
    @Override
    public Object visitIdentificador(AlgumaParser.IdentificadorContext ctx) {
        // itera todos os os escopos para verificar se o identificador ja foi declarado
        for (TabelaDeSimbolos escopoAtual : escopo.percorrerEscoposAninhados()) {
            //Joga erro se nao achar
            if (!escopoAtual.existe(ctx.IDENT(0).getText())) {
                adicionarErrorsSemanticos(ctx.start, "identificador " + ctx.IDENT(0).getText() + " nao declarado");
            }
        }
        return super.visitIdentificador(ctx);
    }

    //Testa se uma atribuicao é valida
    @Override
    public Object visitCmdAtribuicao(AlgumaParser.CmdAtribuicaoContext ctx) {
        TipoDeclaracao tipoExpressao = AlgumaSemanticoT4Utils.verificarTipo(escopo, ctx.expressao());
        boolean error = false;
        String nome = ctx.identificador().getText();
        if (tipoExpressao != TipoDeclaracao.INVALIDO) {
            for (TabelaDeSimbolos escopoAtual : escopo.percorrerEscoposAninhados()) {
                //Testa se a variavel que recebe a expressao ja foi declarada 
                if (escopoAtual.existe(nome)) {
                    TipoDeclaracao tipoVariavel = AlgumaSemanticoT4Utils.verificarTipo(escopo, nome);
                    Boolean expNumeric = tipoExpressao == TipoDeclaracao.INTEIRO || tipoExpressao == TipoDeclaracao.REAL;
                    Boolean varNumeric = tipoVariavel == TipoDeclaracao.INTEIRO || tipoVariavel == TipoDeclaracao.REAL;
                    //Testa de a variavel e a expressao sao do tipo real ou inteiro E se elas sao iguais
                    if (!(varNumeric && expNumeric) && tipoVariavel != tipoExpressao) {
                        error = true;
                    }
                }
            }
        } else {
            //Caso em que a expressao é invalida 
            error = true;
        }

        if (error)
            AlgumaSemanticoT4Utils.adicionarErrorsSemanticos(ctx.identificador().start, "atribuicao nao compativel para " + nome);

        return super.visitCmdAtribuicao(ctx);
    }

}
