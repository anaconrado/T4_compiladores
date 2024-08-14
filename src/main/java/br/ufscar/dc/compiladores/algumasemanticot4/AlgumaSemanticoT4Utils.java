/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser.TermoContext;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser.ParcelaContext;
import br.ufscar.dc.compiladores.algumasemanticot4.TabelaDeSimbolos.TipoDeclaracao;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author ana
 */

public class AlgumaSemanticoT4Utils {
    
    public static List <String> errosSemanticos = new ArrayList<>();
    
    public static void adicionarErrorsSemanticos(Token t, String mensagem) {
        int linha = t.getLine();
        errosSemanticos.add(String.format("Linha %d: %s\n", linha, mensagem));
    }
    
    public static TabelaDeSimbolos.TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Exp_aritmeticaContext ctx) {
        TipoDeclaracao tipoRetorno = null;

        for (TermoContext termo : ctx.termo()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, termo);              
            System.out.println("Atual:" + tipoAtual);
            if (tipoRetorno == null)
                tipoRetorno = tipoAtual;
            else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO)
                tipoRetorno = TipoDeclaracao.INVALIDO;
        }
        System.out.println("Retorno:" + tipoRetorno);
        return tipoRetorno;
    }

    private static TipoDeclaracao verificarTipo(Escopo escopo, TermoContext ctx) {
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

        for (ParcelaContext parcela : ctx.parcela()) {
            TabelaDeSimbolos.TipoDeclaracao tipoAtual = verificarTipo(escopo, parcela);
            if (tipoRetorno == null)
                tipoRetorno = tipoAtual;
            else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO) 
                tipoRetorno = TipoDeclaracao.INVALIDO;
        }
        return tipoRetorno;
    }
    
    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.ParcelaContext ctx) {
        TipoDeclaracao tipoRetorno;
        if (ctx.parcela_nao_unario() != null) {
            tipoRetorno = verificarTipo(escopo, ctx.parcela_nao_unario());
        } 
        else {
            tipoRetorno = verificarTipo(escopo, ctx.parcela_unario());
        }
        return tipoRetorno;
    }

    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_nao_unarioContext ctx) {
        if (ctx.identificador() != null) {
            return verificarTipo(escopo, ctx.identificador());
        }
        return TipoDeclaracao.LITERAL;
    }
    
    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.IdentificadorContext ctx) {
        StringBuilder nomeVar = new StringBuilder();
        TipoDeclaracao tipoRetorno = TipoDeclaracao.INVALIDO;

        for (int i = 0; i < ctx.IDENT().size(); i++) {
            nomeVar.append(ctx.IDENT(i).getText());
            if (i != ctx.IDENT().size() - 1) {
                nomeVar.append("."); 
            }
        }
        
        for (TabelaDeSimbolos tabela : escopo.percorrerEscoposAninhados()) {
            if (tabela.existe(nomeVar.toString())) {
                tipoRetorno = verificarTipo(escopo, nomeVar.toString());
            }
        }
        return tipoRetorno;
    }
    
    public static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) {
            return TipoDeclaracao.INTEIRO;
        }
        if (ctx.NUM_REAL() != null) {
            return TipoDeclaracao.REAL;
        }
        if (ctx.identificador() != null) {          
            return verificarTipo(escopo, ctx.identificador());
        }
        if (ctx.IDENT() != null) {
            TipoDeclaracao TipoRetorno;
            TipoRetorno = verificarTipo(escopo, ctx.IDENT().getText());
            return getTipoDeclaracao(escopo, ctx, TipoRetorno);
        } else {
            TipoDeclaracao TipoRetorno = null;
            return getTipoDeclaracao(escopo, ctx, TipoRetorno);
        }
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
    
    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Termo_logicoContext ctx) {
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

    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Fator_logicoContext ctx) {
        return verificarTipo(escopo, ctx.parcela_logica());
    }
    
    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Parcela_logicaContext ctx) {
        TipoDeclaracao tipoRetorno;
        
        if (ctx.exp_relacional() != null) {
            tipoRetorno = verificarTipo(escopo, ctx.exp_relacional());
        } else {
            tipoRetorno = TipoDeclaracao.LOGICO;
        }

        return tipoRetorno;
    }
    
    private static TipoDeclaracao verificarTipo(Escopo escopo, AlgumaParser.Exp_relacionalContext ctx) {
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
    
    private static TipoDeclaracao getTipoDeclaracao(Escopo escopo, AlgumaParser.Parcela_unarioContext ctx, TipoDeclaracao tipoRetorno) {
        for (AlgumaParser.ExpressaoContext expressao : ctx.expressao()) {
            TipoDeclaracao tipoAtual = verificarTipo(escopo, expressao);
            if (tipoRetorno == null) {
                tipoRetorno = tipoAtual;
            } else if (tipoRetorno != tipoAtual && tipoAtual != TipoDeclaracao.INVALIDO) {
                tipoRetorno = TipoDeclaracao.INVALIDO;
            }
        }
        return tipoRetorno;
    }
    
    public static TipoDeclaracao verificarTipo(Escopo escopo, String nomeVar) {
        TipoDeclaracao tipoAtual = null;
        for (TabelaDeSimbolos tabela : escopo.percorrerEscoposAninhados()) {
            tipoAtual = tabela.verificar(nomeVar);
        }
        return tipoAtual;
    }

}
