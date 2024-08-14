
grammar Alguma;

//Copiado do T1
PALAVRA_CHAVE: 'algoritmo' | 'declare' | 'literal' | 'inteiro' | 'leia' | 'escreva' | 'fim_algoritmo' | 'real' | 'tipo' | 'ou' | 'e' | 'se' | 'entao' | 'senao' | 'fim_se' | 'nao'
| 'funcao' | 'retorne' | 'fim_funcao' | 'enquanto' | 'fim_enquanto' | 'falso' | 'verdadeiro'
| 'para' | 'ate' | 'faca' | 'fim_para' | 'procedimento' | 'fim_procedimento' | 'registro' | 'fim_registro' |
'caso' | 'fim_caso' | 'seja' | 'logico' | 'var' | 'constante' |
'<=' | '>=' | '=' | '<' | '<>' | '>' | '^' | '&' | '[' | ']' | '+' | '-' | '..' | '*' | '%' | ':' | '.' | '(' | ')' 
| ',' | '/' | '<-' ;                  


NUM_INT: ('0'..'9')+;

// Nessa linguagem, o token é um símbolo separado do número
NUM_REAL: ('0'..'9')+ ('.' ('0'..'9')+)?;

// Começa e termina com " e pode ser seguido de qualquer caractere exceto \n
CADEIA: '"' ~('\n'|'"')* '"';

WB: ' ' -> skip;

QUEBRA_LINHA: '\n' -> skip;

TAB: '\t' -> skip;

IDENT: LETRA (LETRA | DIGITO | '_')*;

/* comentários nessa linguagem são caracterizados por começarem e terminarem 
com {} e ter qualquer caractere dentro exceto \n 
*/
COMENTARIO: '{ ' ~('\n'|'}')* '}' -> skip;

ERRO_CADEIA: '"' ~('\n'|'"')* '\n';

ERRO_COMENTARIO: '{' ~('\n'|'}')* '\n';

NAO_RECONHECIDO: '~' | '}' | '$' | '|' | '@' | '!';

fragment
CARACTERE_ESPECIAL: ' ' | '(' | ')';

fragment
TEXTO: (LETRA | DIGITO | CARACTERE_ESPECIAL)*;

fragment
LETRA: ('a'..'z') | ('A'..'Z');

fragment
DIGITO: ('0'..'9');


programa: declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF;
declaracoes: (declaracao_local | declaracao_global)*;
declaracao_local: 'declare' variavel | 'constante' IDENT ':' tipo_basico '=' valor_constante | 'tipo' IDENT ':' tipo;
variavel: identificador (',' identificador)* ':' tipo;
identificador: IDENT ('.' IDENT)* dimensao;
dimensao: ('[' exp_aritmetica ']')*;
tipo: registro | tipo_estendido;
tipo_basico: 'literal' | 'inteiro' | 'real' | 'logico';
tipo_basico_ident: tipo_basico | IDENT;
tipo_estendido: '^'? tipo_basico_ident;
valor_constante: CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso';
registro: 'registro' variavel* 'fim_registro';
declaracao_global:
    'procedimento' IDENT '(' parametros? ')' corpo 'fim_procedimento' | 'funcao' IDENT '(' parametros? ')' ':' tipo_estendido corpo 'fim_funcao';
parametro: 'var'? identificador (',' identificador)* ':' tipo_estendido;
parametros: parametro (',' parametro)*;
corpo: declaracao_local* cmd*;
cmd: cmdLeia | cmdEscreva | cmdSe | cmdCaso | cmdPara | cmdEnquanto | cmdFaca | cmdAtribuicao | cmdChamada | cmdRetorne;
cmdLeia: 'leia' '(' '^'? identificador (',' '^'? identificador)* ')';
cmdEscreva: 'escreva' '(' expressao (',' expressao)* ')';
cmdSe: 'se' expressao 'entao' cmd* ('senao' cmd*)? 'fim_se';
cmdCaso: 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso';
cmdPara: 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para';
cmdEnquanto: 'enquanto' expressao 'faca' cmd* 'fim_enquanto';
cmdFaca: 'faca' cmd* 'ate' expressao;
cmdAtribuicao: '^'? identificador '<-' expressao;
cmdChamada: IDENT '(' expressao (',' expressao)* ')';
cmdRetorne: 'retorne' expressao;
selecao: item_selecao*;
item_selecao: constantes ':' cmd*;
constantes: numero_intervalo (',' numero_intervalo)*;
numero_intervalo: op_unario? NUM_INT ( '..' op_unario? NUM_INT)?;
op_unario: '-';
exp_aritmetica: termo (op1 termo)*;
termo: fator (op2 fator)*;
fator: parcela (op3 parcela)*;
op1: '+' | '-';
op2: '*' | '/';
op3: '%';
parcela: op_unario? parcela_unario | parcela_nao_unario;
parcela_unario: '^'? identificador | IDENT '(' expressao (',' expressao)* ')' | NUM_INT | NUM_REAL | '(' expressao ')';
parcela_nao_unario: '&' identificador | CADEIA;
exp_relacional: exp_aritmetica (op_relacional exp_aritmetica)?;
op_relacional: '=' | '<>' | '>=' | '<=' | '>' | '<';
expressao: termo_logico (op_logico_1 termo_logico)*;
termo_logico: fator_logico (op_logico_2 fator_logico)*;
fator_logico: 'nao'? parcela_logica;
parcela_logica: 'verdadeiro' | 'falso' | exp_relacional;
op_logico_1: 'ou';
op_logico_2: 'e';