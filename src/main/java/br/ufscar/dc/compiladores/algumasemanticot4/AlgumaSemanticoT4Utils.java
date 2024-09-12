/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser.TermoContext;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser.ParcelaContext;
import static br.ufscar.dc.compiladores.algumasemanticot4.AlgumaSemanticoT4.dadosFuncao;
import br.ufscar.dc.compiladores.algumasemanticot4.TabelaDeSimbolos.TipoDeclaracao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author ana
 */

public class AlgumaSemanticoT4Utils {
    
    public static List<String> errosSemanticos = new ArrayList<>();

    public static void adicionarErrorsSemanticos(Token t, String mensagem) {
        int linha = t.getLine();
        // Adiciona erro só se ele não tiver sido adicionado já
        if (!errosSemanticos.contains("Linha " + linha + ": " + mensagem + "\n")) 
            errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem + "\n"));
    }
    // Função auxiliara para remover simbolos como [] de vetor ou () de funções / procedimentos 
    public static String reduzNome(String nome, String simbolo) {

        if (nome.contains(simbolo)) {

            boolean continua = true;
            int cont = 0;
            String nomeAux;

            while (continua) {
                nomeAux = nome.substring(cont);

                if (nomeAux.startsWith(simbolo))
                    continua = false;
                else
                    cont++;
            }

            nome = nome.substring(0, cont); 
        }

        return nome;

    }   

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Exp_aritmeticaContext ctx) {
        TipoDeclaracao tipoRetorno = null;

        for (TermoContext termo : ctx.termo()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, termo);              
            System.out.println("Atual:" + tipoAtual);
            if (tipoRetorno == null)
                tipoRetorno = tipoAtual;
            else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO)
                tipoRetorno = TipoDeclaracao.INVALIDO;
        }
        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.TermoContext ctx) {
        TipoDeclaracao tipoRetorno = null;
        for (AlgumaParser.FatorContext fa : ctx.fator()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, fa);
            Boolean tipoAtualNum = tipoAtual == TipoDeclaracao.REAL || tipoAtual == TipoDeclaracao.INTEIRO;
            Boolean tipoRetornoNum = tipoRetorno == TipoDeclaracao.REAL || tipoRetorno == TipoDeclaracao.INTEIRO;
            if (tipoRetorno == null) {
                tipoRetorno = tipoAtual;
            } else if (!(tipoAtualNum && tipoRetornoNum) && tipoAtual != tipoRetorno) {
                tipoRetorno = TipoDeclaracao.INVALIDO;
            }
        }
        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.FatorContext ctx) {
        TipoDeclaracao tipoRetorno = null;

        for (AlgumaParser.ParcelaContext parcela : ctx.parcela()) {
            tipoRetorno = verificarTipo(escopo, parcela);

            if (tipoRetorno == TipoDeclaracao.REGISTRO) {                
                String nome = parcela.getText();
                nome = reduzNome(nome, "(");
                tipoRetorno = verificarTipo(escopo, nome);
            }
        }

        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.ParcelaContext ctx) {
        TipoDeclaracao tipoRetorno;
        if (ctx.parcela_nao_unario() != null) {
            tipoRetorno = verificarTipo(escopo, ctx.parcela_nao_unario());
        } 
        else {
            tipoRetorno = verificarTipo(escopo, ctx.parcela_unario());
        }
        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_unarioContext ctx) {
        TipoDeclaracao tipoRetorno = null;
        String nome;

        if (ctx.identificador() != null) {            
            if (!ctx.identificador().dimensao().exp_aritmetica().isEmpty())
                nome = ctx.identificador().IDENT().get(0).getText();
            else
                nome = ctx.identificador().getText();

            if (escopo.obterEscopoAtual().existe(nome)) {
                tipoRetorno = escopo.obterEscopoAtual().verificar(nome);

            }
            else {
                TabelaDeSimbolos tabelaAux = escopo.obterEscopoAtual();

                if (!tabelaAux.existe(nome)) {
                    if (!ctx.identificador().getText().contains(nome)) {
                        adicionarErrorsSemanticos(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                        tipoRetorno = TipoDeclaracao.INVALIDO;
                    } else {
                        adicionarErrorsSemanticos(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                        tipoRetorno = TipoDeclaracao.INVALIDO;
                    }
                } else
                    tipoRetorno = tabelaAux.verificar(nome);
            }
        } else if (ctx.IDENT() != null) {
            if (dadosFuncao.containsKey(ctx.IDENT().getText())) {
                List<TipoDeclaracao> aux = dadosFuncao.get(ctx.IDENT().getText());

                if (aux.size() == ctx.expressao().size()) {
                    for (int i = 0; i < ctx.expressao().size(); i++)
                        if (aux.get(i) != verificarTipo(escopo, ctx.expressao().get(i)))
                            adicionarErrorsSemanticos(ctx.expressao().get(i).getStart(), "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());

                    tipoRetorno = aux.get(aux.size() - 1);

                } else
                    adicionarErrorsSemanticos(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
            } else
                tipoRetorno = TipoDeclaracao.INVALIDO;
        } else if (ctx.NUM_INT() != null)
            tipoRetorno = TipoDeclaracao.INTEIRO;
        else if (ctx.NUM_REAL() != null)
            tipoRetorno = TipoDeclaracao.REAL;
        else
            tipoRetorno = verificarTipo(escopo, ctx.expressao().get(0));

        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_nao_unarioContext ctx) {
        TipoDeclaracao tipoRetorno;
        String nome;

        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();

            if (!escopo.obterEscopoAtual().existe(nome)) {
                adicionarErrorsSemanticos(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                tipoRetorno = TipoDeclaracao.INVALIDO;
            } else 
                tipoRetorno = escopo.obterEscopoAtual().verificar(ctx.identificador().getText());
        } else
            tipoRetorno = TipoDeclaracao.LITERAL;

        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.ExpressaoContext ctx) {
        TipoDeclaracao tipoRetorno = null;
        
        for (AlgumaParser.Termo_logicoContext termo : ctx.termo_logico()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, termo);
            if (tipoRetorno == null) {
                tipoRetorno = tipoAtual;
            } 
            else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO) {
                tipoRetorno = TipoDeclaracao.INVALIDO;
            }
        }
        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Termo_logicoContext ctx) {
        TipoDeclaracao tipoRetorno = null;
        
        for (AlgumaParser.Fator_logicoContext fator : ctx.fator_logico()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, fator);
            if (tipoRetorno == null) {
                tipoRetorno = tipoAtual;
            } 
            else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO) {
                tipoRetorno = TipoDeclaracao.INVALIDO;
            }
        }

        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Fator_logicoContext ctx) {
        return verificarTipo(escopo, ctx.parcela_logica());
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_logicaContext ctx) {
        TipoDeclaracao tipoRetorno;

        if (ctx.exp_relacional() != null)
            tipoRetorno = verificarTipo(escopo, ctx.exp_relacional());
        else
            tipoRetorno = TipoDeclaracao.LOGICO;

        return tipoRetorno;

    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Exp_relacionalContext ctx) {
        TipoDeclaracao tipoRetorno = null;
        
        if (ctx.op_relacional() != null) {
            for (AlgumaParser.Exp_aritmeticaContext ta : ctx.exp_aritmetica()) {
                TipoDeclaracao tipoAtual = verificarTipo(escopo, ta);
                Boolean auxNumeric = tipoAtual == TipoDeclaracao.REAL || tipoAtual == TipoDeclaracao.INTEIRO;
                Boolean retNumeric = tipoRetorno == TipoDeclaracao.REAL || tipoRetorno == TipoDeclaracao.INTEIRO;
                if (tipoRetorno == null) {
                    tipoRetorno = tipoAtual;
                } else if (!(auxNumeric && retNumeric) && tipoAtual != tipoRetorno) {
                    tipoRetorno = TipoDeclaracao.INVALIDO;
                }
            }
            if (tipoRetorno != TipoDeclaracao.INVALIDO) {
                tipoRetorno = TipoDeclaracao.LOGICO;
            }
        } else {
            tipoRetorno = verificarTipo(escopo, ctx.exp_aritmetica(0));
        }

        return tipoRetorno;
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.IdentificadorContext ctx) {
        String nomeVar = ctx.IDENT().get(0).getText();

        return escopo.obterEscopoAtual().verificar(nomeVar);
    }

    public static TipoDeclaracao verificarTipo(Escopo escopo, String nomeVar) {
        return escopo.obterEscopoAtual().verificar(nomeVar);
    }

}
