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

import java.util.ArrayList;
import java.util.HashMap;

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
                    // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
                    if (!escopo.obterEscopoAtual().existe(ic.getText()))
                        escopo.obterEscopoAtual().adicionar(ic.getText(), TipoDeclaracao.REGISTRO, TipoFuncao.OUTRO);
                    else
                        adicionarErrorsSemanticos(ic.getStart(), "identificador " + ic.getText() + " ja declarado anteriormente");
                    // Adiciona as variaveis desse registro com seu tipo
                    for (AlgumaParser.VariavelContext vc : ctx.variavel().tipo().registro().variavel()) {
                        tipoVariavel = vc.tipo().getText();
                        for (AlgumaParser.IdentificadorContext icr : vc.identificador()){
                            TipoDeclaracao tipo;
                            if(tipoVariavel.charAt(0) == '^') {
                                tipoVariavel = tipoVariavel.substring(1);
                            }
                            switch (tipoVariavel) {
                                case "literal":
                                    tipo = TipoDeclaracao.LITERAL;
                                    break;
                                case "inteiro":
                                    tipo = TipoDeclaracao.INTEIRO;
                                    break;
                                case "real":
                                    tipo = TipoDeclaracao.REAL;
                                    break;
                                case "logico":
                                    tipo = TipoDeclaracao.LOGICO;
                                    break;
                                case "void":
                                    tipo = TipoDeclaracao.VOID;
                                    break;
                                case "registro":
                                    tipo = TipoDeclaracao.REGISTRO;
                                    break;
                                default:
                                    tipo = TipoDeclaracao.INVALIDO;
                                    break;
                            }
                            if (tipo == TipoDeclaracao.INVALIDO){
                                adicionarErrorsSemanticos(vc.tipo().getStart(), "tipo " + tipoVariavel + " nao declarado");
                                System.out.println("Foi aqui 1");
                            }
                            // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
                            if (!escopo.obterEscopoAtual().existe(ic.getText() + "." + icr.getText()))
                                escopo.obterEscopoAtual().adicionar(ic.getText() + "." + icr.getText(), tipo, TipoFuncao.OUTRO);
                            else
                                adicionarErrorsSemanticos(icr.getStart(), "identificador " + ic.getText() + "." + icr.getText() + " ja declarado anteriormente");

                        }
                    }
                }
            }
            else {
                //Itera variaveis declaradas             
                for (AlgumaParser.IdentificadorContext id : ctx.variavel().identificador()) {
                    // Adiciona erro se constante ja existe no escopo atual
                    if (escopo.obterEscopoAtual().existe(id.getText())) {
                        adicionarErrorsSemanticos(id.start, "identificador " + id.getText() + " ja declarado anteriormente");
                    } 
                    // Se nao, adiciona no escopo com seu tipo
                    else {
                        TipoDeclaracao tipo = null;
                        String tipoTexto = ctx.variavel().tipo().getText();
                        // Retira identificador de ponteiro para comparar tipo
                        if(tipoTexto.charAt(0) == '^')
                            tipoTexto = tipoTexto.substring(1);
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
                        escopo.obterEscopoAtual().adicionar(id.getText(), tipo, TipoFuncao.OUTRO);
                        //System.out.println("Variable: " + id.getText() + " Type: " + tipo);   
                    }
                }
            }
        }
        // Declaracao de constante
        else if (ctx.getChild(0).getText().equals("constante")){
            // Adiciona erro se constante ja existe no escopo atual
            if (escopo.obterEscopoAtual().existe(ctx.IDENT().getText())) {
                adicionarErrorsSemanticos(ctx.start, "constante " + ctx.IDENT().getText() + " ja declarado anteriormente");
            // Se nao, adiciona no escopo com seu tipo
            } else {
                TipoDeclaracao tipo = null;
                String tipoBasicoTexto = ctx.tipo_basico().getText();
                
                if(tipoBasicoTexto.charAt(0) == '^')
                    tipoBasicoTexto = tipoBasicoTexto.substring(1);

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
                escopo.obterEscopoAtual().adicionar(ctx.IDENT().getText(), tipo, TipoFuncao.OUTRO);
            }
        }
        
        // Declaracao de tipo 
        else if (ctx.getChild(0).getText().equals("tipo")) { 
            if (ctx.tipo().registro() != null) {
                ArrayList<String> variaveisRegistro = new ArrayList<>();

                for (AlgumaParser.VariavelContext vc : ctx.tipo().registro().variavel()) {
                    tipoVariavel = vc.tipo().getText();

                    for (AlgumaParser.IdentificadorContext ic : vc.identificador()) {
                        variaveisRegistro.add(ic.getText());
                        variaveisRegistro.add(tipoVariavel);
                    }
                }
                tabelaRegistro.put(ctx.IDENT().getText(), variaveisRegistro);
            }
        }
        return super.visitDeclaracao_local(ctx);
    }
    
    // Visita declaracoes global, que pode conter procedimento ou funcao
    @Override
    public Object visitDeclaracao_global(AlgumaParser.Declaracao_globalContext ctx) {
        // Cria um novo escopo para cada novo procedimento ou função.
        escopo.criarNovoEscopo();

        // Lista auxiliar que armazenará os tipos das variáveis
        ArrayList<TipoDeclaracao> tiposVariaveis = new ArrayList<>();
        ArrayList<String> variaveisRegistro;

        String tipoVariavel;
        TipoDeclaracao tipoAux;

        // Caso seja proedimento
        if (ctx.getText().contains("procedimento")) {

            for (AlgumaParser.ParametroContext parametro : ctx.parametros().parametro()) {
                // Verifica se é um tipo básico válido.
                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                    TipoDeclaracao tipoItem;
                    String tipo = parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText(); 
                    if (tipo.charAt(0) == '^')
                       tipo = tipo.substring(1);
                    
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
                            tipoItem = TipoDeclaracao.INVALIDO;
                            break;
                    }
                    
                    if (!escopo.obterEscopoAtual().existe(parametro.identificador().get(0).getText()))
                        escopo.obterEscopoAtual().adicionar(parametro.identificador().get(0).getText(), tipoItem, TipoFuncao.OUTRO);
                    else
                        adicionarErrorsSemanticos(parametro.getStart(), "identificador " + parametro.identificador().get(0).getText() + " ja declarado anteriormente");

                    // Obtém os tipos dos parâmetros para adicioná-las na tabela de dados de funções e procedimentos.
                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);
                } 
                // Verifica se é um registro
                else if (tabelaRegistro.containsKey(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText())) {
                    // Recupera os elementos do registro.
                    variaveisRegistro = tabelaRegistro.get(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText());

                    // Obtém os tipos dos parâmetros para adicioná-las na tabela de dados de funções e procedimentos.
                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);

                    for (AlgumaParser.IdentificadorContext ic : parametro.identificador()){
                        // Adiciona os elementos do registro na tabela no formato adequado com seus respectivos tipos individuais.
                        for (int i = 0; i < variaveisRegistro.size(); i = i + 2){
                            String tipo = variaveisRegistro.get(i + 1);
                            if (tipo.charAt(0) == '^')
                                tipo = tipo.substring(1);
                            TipoDeclaracao tipoItem;
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
                                    tipoItem = TipoDeclaracao.INVALIDO;
                                    break;
                            }
                            if (tipoItem == TipoDeclaracao.INVALIDO){
                                adicionarErrorsSemanticos(ic.getStart(), "tipo " + variaveisRegistro.get(i + 1) + " nao declarado");
                                System.out.println("Foi aqui 2");
                            }

                            // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
                            if (!escopo.obterEscopoAtual().existe(ic.getText() + "." + variaveisRegistro.get(i)))
                                escopo.obterEscopoAtual().adicionar(ic.getText() + "." + variaveisRegistro.get(i), tipoItem, TipoFuncao.OUTRO);
                            else
                                adicionarErrorsSemanticos(ic.getStart(), "identificador " + ic.getText() + "." + variaveisRegistro.get(i) + " ja declarado anteriormente");
                        }
                    }
                } 
                else{
                    adicionarErrorsSemanticos(parametro.getStart(), "tipo nao declarado");  
                    System.out.println("Foi aqui 3");
                }
            }
            // Verifica se há algum comando "retorne" dentro de um procedimento e indica o erro.
            for (AlgumaParser.CmdContext c : ctx.cmd())    
                if (c.cmdRetorne() != null)  
                    adicionarErrorsSemanticos(c.getStart(), "comando retorne nao permitido nesse escopo");    

            // Adiciona o nome do procedimento e os tipos dos parâmetros na tabela de dados.
            dadosFuncao.put(ctx.IDENT().getText(), tiposVariaveis);

        // Verifica se o escopo atual pertence a uma função.
        } else if (ctx.getText().contains("funcao")) {
            // A partir daqui, utiliza uma lógica semelhante às verificações para o procedimento.
            for (AlgumaParser.ParametroContext parametro : ctx.parametros().parametro()) {

                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                    
                    TipoDeclaracao tipoItem;
                    String tipo = parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText();
                    // Remoção do ponteiro do tipo pra verificação.
                    if (tipo.charAt(0) == '^')
                        tipo = tipo.substring(1);

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
                            tipoItem = TipoDeclaracao.INVALIDO;
                            break;
                    }

                    // Caso o tipo seja inválido, exibe a mensagem de que o tipo não foi declarado.
                    if (tipoItem == TipoDeclaracao.INVALIDO){
                        adicionarErrorsSemanticos(parametro.getStart(), "tipo " + tipo + " nao declarado");
                        System.out.println("Foi aqui 4");
                    }

                    // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
                    if (!escopo.obterEscopoAtual().existe(parametro.identificador().get(0).getText()))
                        escopo.obterEscopoAtual().adicionar(parametro.identificador().get(0).getText(), tipoItem, TipoFuncao.OUTRO);
                    else
                        adicionarErrorsSemanticos(parametro.getStart(), "identificador " + parametro.identificador().get(0).getText() + " ja declarado anteriormente");

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
                            TipoDeclaracao tipoItem;
                            String tipo = variaveisRegistro.get(i + 1);
                            if (tipo.charAt(0) == '^')
                                tipo = tipo.substring(1);

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
                                    tipoItem = TipoDeclaracao.INVALIDO;
                                    break;
                            }

                            // Caso o tipo seja inválido, exibe a mensagem de que o tipo não foi declarado.
                            if (tipoItem == TipoDeclaracao.INVALIDO){
                                adicionarErrorsSemanticos(ic.getStart(), "tipo " + tipo + " nao declarado");
                                System.out.println("Foi aqui 3");
                            }

                            // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
                            if (!escopo.obterEscopoAtual().existe(ic.getText() + "." + variaveisRegistro.get(i)))
                                escopo.obterEscopoAtual().adicionar(ic.getText() + "." + variaveisRegistro.get(i), tipoItem, TipoFuncao.OUTRO);
                            else
                                adicionarErrorsSemanticos(ic.getStart(), "identificador " + ic.getText() + "." + variaveisRegistro.get(i) + " ja declarado anteriormente");
                        }
                } 
                else{
                    adicionarErrorsSemanticos(parametro.getStart(), "tipo nao declarado");
                    System.out.println("Foi aqui 6");
                }
            }

            // Adiciona o nome da função e os tipos dos parâmetros na tabela de dados.
            dadosFuncao.put(ctx.IDENT().getText(), tiposVariaveis);
        }

        super.visitDeclaracao_global(ctx);

        // Desempilha o escopo atual
        escopo.abandonarEscopo();

        // Adiciona o nome do procedimento/função na tabela
        if (ctx.getText().contains("procedimento")){    
            if (!escopo.obterEscopoAtual().existe(ctx.IDENT().getText()))
                escopo.obterEscopoAtual().adicionar(ctx.IDENT().getText(), TipoDeclaracao.VOID, TipoFuncao.PROCEDIMENTO);
            else
                adicionarErrorsSemanticos(ctx.getStart(), "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente");
        }
        else if (ctx.getText().contains("funcao")){
            TipoDeclaracao tipoItem;
            String tipo = ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText();
            // Remoção do ponteiro do tipo pra verificação.
            if (tipo.charAt(0) == '^')
                tipo = tipo.substring(1);

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
                    tipoItem = TipoDeclaracao.INVALIDO;
                    break;
            }

            // Caso o tipo seja inválido, exibe a mensagem de que o tipo não foi declarado.
            if (tipoItem == TipoDeclaracao.INVALIDO){
                adicionarErrorsSemanticos(ctx.getStart(), "tipo " + tipo + " nao declarado");
                System.out.println("Foi aqui 7");
            }

            // Verifica a existência do símbolo no escopo atual e exibe um erro caso já tenha sido declarado.
            if (!escopo.obterEscopoAtual().existe(ctx.IDENT().getText()))
                escopo.obterEscopoAtual().adicionar(ctx.IDENT().getText(), tipoItem, TipoFuncao.FUNCAO);
            else
                adicionarErrorsSemanticos(ctx.getStart(), "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente");
        }
        return null;
    }
    
    
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
    /*
    @Override
    public Object visitCmdLeia(AlgumaParser.CmdLeiaContext ctx) {
        for (AlgumaParser.IdentificadorContext id : ctx.identificador()) 
            // Verifica se está lendo uma variável que ainda não foi declarada.
            if (!escopo.obterEscopoAtual().existe(id.getText()))
                adicionarErrorsSemanticos(id.getStart(), "identificador " + id.getText() + " nao declarado");

        return super.visitCmdLeia(ctx);
    }

    @Override
    public Object visitCmdEscreva(AlgumaParser.CmdEscrevaContext ctx) {
        TipoDeclaracao tipo;

        for (AlgumaParser.ExpressaoContext expressao : ctx.expressao())
            tipo = verificarTipo(escopo.obterEscopoAtual(), expressao);

        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Object visitCmdEnquanto(AlgumaParser.CmdEnquantoContext ctx) {
        TipoDeclaracao tipo = verificarTipo(escopo.obterEscopoAtual(), ctx.expressao());

        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Object visitCmdSe(AlgumaParser.CmdSeContext ctx) {
        TipoDeclaracao tipo = verificarTipo(escopo.obterEscopoAtual(), ctx.expressao());

        return super.visitCmdSe(ctx);
    }
    */
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
