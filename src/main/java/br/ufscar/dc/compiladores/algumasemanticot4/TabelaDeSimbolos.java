/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import java.util.HashMap;

/**
 *
 * @author ana
 */
public class TabelaDeSimbolos {
    public enum TipoDeclaracao {
        REAL,
        INTEIRO,
        LOGICO,
        LITERAL,
        TIPO,
        INVALIDO,
    }
    /*
    public enum TipoFuncao {
        PROCEDIMENTO,
        FUNCAO,
        OUTRO
    }
    */
    class EntradaTabelaDeSimbolos {
        String nome;
        TipoDeclaracao tipoD;
        //TipoFuncao tipoF;

        private EntradaTabelaDeSimbolos(String nome, TipoDeclaracao tipoD) {
            this.nome = nome;
            this.tipoD = tipoD;
        }
    }
    
        
    private HashMap<String, EntradaTabelaDeSimbolos> tabelaDeSimbolos;
    
    public TabelaDeSimbolos() {
        this.tabelaDeSimbolos = new HashMap<>();  
    }
    
    public void adicionar(String nome, TipoDeclaracao tipoD) {
        tabelaDeSimbolos.put(nome, new EntradaTabelaDeSimbolos(nome, tipoD));
    }
   
    public boolean existe(String nome) {
        return tabelaDeSimbolos.containsKey(nome);
    } 
    
    public TipoDeclaracao verificar(String nome) {
        return tabelaDeSimbolos.get(nome).tipoD;
    }
}
