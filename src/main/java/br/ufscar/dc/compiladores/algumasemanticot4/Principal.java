/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.algumasemanticot4;

import br.ufscar.dc.compiladores.algumasemantico4.AlgumaBaseVisitor;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaLexer;
import br.ufscar.dc.compiladores.algumasemantico4.AlgumaParser;
import java.io.FileWriter;
import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

/**
 *
 * @author ana
 */
public class Principal extends AlgumaBaseVisitor<Double>{

    public static void main(String args[]) throws IOException {

        Token t = null;

        // Programa de entrada
        String arquivo = args[1];
        FileWriter writer = new FileWriter(arquivo);
        
        CharStream cs = CharStreams.fromFileName(args[0]);
        AlgumaLexer lexer = new AlgumaLexer(cs);
        CommonTokenStream cts = new CommonTokenStream(lexer);
        AlgumaSemanticoT4 semantico = new AlgumaSemanticoT4();
        AlgumaParser parser = new AlgumaParser(cts);
        AlgumaParser.ProgramaContext arvore = parser.programa();
        Boolean hasError = false;

        // Analise lexica (t1)
        while ((t = lexer.nextToken()).getType() != Token.EOF) {
            String tokenName = AlgumaLexer.VOCABULARY.getDisplayName(t.getType());

            // Testa para erro de cadeia não fechada
            if (tokenName.equals("ERRO_CADEIA")){
                writer.write("Linha " + t.getLine() + ": cadeia literal nao fechada\n");
                hasError = true;
                break;
            }

            // Testa para erro de comentário não fechado
            else if (tokenName.equals("ERRO_COMENTARIO")){
                writer.write("Linha " + t.getLine() + ": comentario nao fechado\n");
                hasError = true;
                break;
            }

            // Testa para caracteres não reconhecidos
            else if (tokenName.equals("NAO_RECONHECIDO")){
                writer.write("Linha " + t.getLine() + ": " + t.getText() + " - simbolo nao identificado\n");
                hasError = true;
                break;
            }
        }
        // Analise sintatica (t2)
         if (!hasError) {
            cs = CharStreams.fromFileName(args[0]);
            lexer = new AlgumaLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new AlgumaParser(tokens);
    
            // Removendo error listener e adicionando o nosso
            parser.removeErrorListeners();
            CustomErrorListener errorListener = new CustomErrorListener(writer);
            parser.addErrorListener(errorListener);
            parser.programa();
        }
         
        // Analise semantica (t3)
        semantico.visitPrograma(arvore);
        
        for (String error : AlgumaSemanticoT4Utils.errosSemanticos) {
            writer.write(error);
        }
        writer.write("Fim da compilacao\n");
        writer.close();
    }
}
