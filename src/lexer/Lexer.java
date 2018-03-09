package lexer;

import java.io.*;

public class Lexer {

    private static final int END_OF_FILE = -1; // constante para fim do arquivo
    private static int lookahead = 0; // armazena o último caractere lido do arquivo	
    public static int n_line = 1; // contador de linhas
    public static int n_column = 1; // contador de colunas
    private RandomAccessFile instance_file; // referencia para o arquivo
    private static TS tabelaSimbolos; // tabela de simbolos

    public Lexer(String input_data) {

        try {
            instance_file = new RandomAccessFile(input_data, "r");
        } catch (IOException e) {
            System.out.println("Erro de abertura do arquivo " + input_data + "\n" + e);
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Erro do programa ou falha da tabela de simbolos\n" + e);
            System.exit(2);
        }
    }

    public void fechaArquivo() {

        try {
            instance_file.close();
        } catch (IOException errorFile) {
            System.out.println("Erro ao fechar arquivo\n" + errorFile);
            System.exit(3);
        }
    }

    // Reporta erro para o usuário
    public void sinalizaErro(String mensagem) {

        System.out.println("[Erro Lexico]: " + mensagem + "\n");
    }

    // Volta uma posição do buffer de leitura
    public void retornaPonteiro() {

        try {
            if (lookahead != END_OF_FILE) {
                instance_file.seek(instance_file.getFilePointer() - 1);
                n_column -= 1; 
            }
        } catch (IOException e) {
            System.out.println("Falha ao retornar a leitura\n" + e);
            System.exit(4);
        }
    }

    public Token proxToken() {

        // essa variavel armaeza o lexema (palavra) construido
        StringBuilder lexema = new StringBuilder();

        // variavel que representa o estado atual
        int estado = 0;

        // armazena o char corrente
        char c;

        // sai desse loop somente qndo retornar um token
        while (true) {
            c = '\u0000'; 

            // avanca caractere
            try {
                lookahead = instance_file.read();

                if (lookahead != END_OF_FILE) {
                    c = (char) lookahead;
                    n_column++;
                }
            } catch (IOException e) {
                System.out.println("Erro na leitura do arquivo");
                System.exit(3);
            }

            switch (estado) {

                case 0:
                    if (lookahead == END_OF_FILE) // fim de arquivo.
                    {
                        return new Token(Tag.EOF, "EOF", n_line, n_column);
                    } else if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                        
                        if (c == '\n') {
                            
                            n_column = 1;
                            n_line++;
                        }
                        
                    } else if (c == '+') {
                        return new Token(Tag.RELOP_PLUS, "+", n_line, n_column);
                    } else if (c == '-') {
                        return new Token(Tag.RELOP_MINUS, "-", n_line, n_column);
                    } else if (c == '*') {
                        return new Token(Tag.RELOP_MULT, "*", n_line, n_column);
                    } else if (c == '/') {
                        estado = 24;
                    } else if (c == '=') {
                        lexema.append(c);
                        estado = 11;
                    } else if (Character.isLetter(c)) {
                        //Construção de um ID
                        lexema.append(c);
                        estado = 16;
                    } else if (Character.isDigit(c)) {
                        lexema.append(c); //Construção de um número
                        estado = 18; 
                    } else if (c == '<') {
                        lexema.append(c);
                        estado = 5;
                    } else if (c == '>') {
                        lexema.append(c);
                        estado = 6;
                    } else if (c == '!') {
                        lexema.append(c);
                        estado = 14;
                    } else if (c == '"') {
                        //Construção de String
                        lexema.append(c);
                        estado = 23;
                    } else if (c == '(') {
                        return new Token(Tag.SMB_OP, "(", n_line, n_column);
                    } else if (c == ')') {
                        return new Token(Tag.SMB_CP, ")", n_line, n_column);
                    } else if (c == '{') {
                        return new Token(Tag.SMB_OK, "{", n_line, n_column);
                    } else if (c == '}'){
                        return new Token(Tag.SMB_CK, "}", n_line, n_column);
                    } else if (c == ';') {
                        return new Token(Tag.SMB_SEMICOLON, ";", n_line, n_column);
                    } else {
                        sinalizaErro("Simbolo " + c + " invalido na linha " + n_line
                                + " e coluna " + n_column);
                        return null;
                    }
                    break;

                case 5:

                    if (c == '=') {
                        return new Token(Tag.RELOP_LE, "<=", n_line, n_column);
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.RELOP_LT, "<", n_line, n_column);
                    }
                case 6:

                    if (c == '=') {
                        return new Token(Tag.RELOP_GE, ">=", n_line, n_column);
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.RELOP_GT, ">", n_line, n_column);
                    }
                case 11:
                    if (c == '=') {
                        return new Token(Tag.RELOP_EQ, "==", n_line, n_column);
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.RELOP_ASSIGN, "=", n_line, n_column);
                    }
                case 14:
                    if (c == '=') {
                        return new Token(Tag.RELOP_NE, "!=", n_line, n_column);
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.RELOP_NOT, "!", n_line, n_column);
                    }
                case 16:
                    if (Character.isLetterOrDigit(c) || c == '_') {
                        lexema.append(c);
                    } else {
                        retornaPonteiro();
                        Token token = tabelaSimbolos.retornaToken(lexema.toString());

                        if (token == null) {
                            return new Token(Tag.ID, lexema.toString(), n_line, n_column);
                        }
                        return token;
                    }
                    break;
                case 18:
                    if (Character.isDigit(c)) {
                        lexema.append(c);
                    } else if (c == '.') {
                        //Caso apareça um . significa que é um double
                        lexema.append(c);
                        estado = 20;
                    } else {
                        retornaPonteiro();
                        return new Token(Tag.INTEGER, lexema.toString(), n_line, n_column);
                    }
                    break;
                case 20:
                    if (Character.isDigit(c)) {
                        estado = 21; 
                        lexema.append(c); 
                    } else {
                        retornaPonteiro();
                        sinalizaErro("Simbolo " + c + " invalido na linha " + n_line
                                + " e coluna " + n_column);
                        return null;
                    }
                    break;
                case 21:
                    if (Character.isDigit(c)) {
                        lexema.append(c);
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.DOUBLE, lexema.toString(), n_line, n_column);
                    }
                    break;
                case 23:
                    if (c == '"') {
                        lexema.append(c);
                        return new Token(Tag.STRING, lexema.toString(), n_line, n_column);
                    } else if (c == '\n' || c == '\r') {
                        this.sinalizaErro("String não fechada antes da quebra de linha. "
                                + "Linha "+n_line+" Coluna "+n_column);
                        return null;
                    } else if (lookahead == END_OF_FILE) {
                        // situacao de erro: string não fechada antes de fim de arquivo
                        sinalizaErro("String deve ser fechada com \" antes do fim de arquivo");
                        return null;
                    } else { 
                        // Se vier outro, permanece no estado 23
                        lexema.append(c);
                    }
                    break;
                case 24:

                    if (c == '/') {
                        estado = 25;
                    }
                    else if (c == '*') {
                        estado = 26;
                    } else {
                        this.retornaPonteiro();
                        return new Token(Tag.RELOP_DIV, "/", n_line, n_column);
                    }

                    break;
                case 25:
                    
                    if (c != '\n') {
                        
                    }
                    else{
                        n_line++;
                        n_column = 1;
                        estado = 0;
                    }
                    break;
                    
                case 26: //Comentário multiplas linhas
                    
                    if ( c == '*') {
                        estado = 27;                       
                    }
                    else if(c == '\n'){
                        n_line++;
                        n_column = 1;
                    }
                    else{
                        n_column++;
                    }
                    break;
                case 27:
                    
                    if (c == '/') {
                        estado = 0;
                    } else {
                        estado = 26;
                    }
                    
                    
                    break;
            } // fim switch
        } // fim while
    } // fim metodo

    public static void main(String[] args) {
        Lexer lexer = new Lexer("src\\Programas_Javinha\\HelloJavinha.jvn"); // parametro eh um programa em Javinha
        Token token;

        
        tabelaSimbolos = new TS();

        // Enquanto não houver erros ou não for fim de arquivo:
        do {

            token = lexer.proxToken();

            // Imprime token
            if (token != null) {
                System.out.println("Token: " + token.toString() + "\t Linha: "
                        + n_line + "\t Coluna: " + n_column);
            }

        } while (token != null && token.getClasse() != Tag.EOF);
        lexer.fechaArquivo();
    }
}
