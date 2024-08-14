/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

/**
 *
 * @author ana
 */
public class CustomErrorListener implements ANTLRErrorListener{
    
    private final FileWriter arquivo;
    Boolean hasError = false;
    
    public CustomErrorListener(FileWriter arquivo){
        this.arquivo = arquivo;
    }
      
    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
 
    }
    
    @Override
    public void	reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

    }
    
    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
  
    }

    @Override
    public void	syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        try{
            if(!hasError){
                Token t = (Token) offendingSymbol;

                String tokenText = t.getText();
                if ("<EOF>".equals(tokenText)){
                    tokenText = "EOF";
                }

                arquivo.write("Linha "+line+": erro sintatico proximo a "+ tokenText+"\n");
                hasError = true;
            }
        }catch (IOException ex) {
        }
    }
}
