package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;

import java.io.IOException;
import java.util.HashMap;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {

	private enum State {
		START, GOT_EQUALS, GOT_LT, GOT_GT, GOT_EP, GOT_SLASH, COMMENT, IDENT_PART, BOOLEAN, STRING_LITERAL,KEYWORD, GOT_ZERO, DIGITS, EOF
	}

	private State state;

	// local references to TokenStream objects for convenience
	// final TokenStream stream; //set in constructor

	private int index; // points to the next char to process during scanning, or
						// if none, past the end of the array
	private int begOffset;
	
	private int length;
	
	StringBuilder s = new StringBuilder();
	
	
	private char ch;

	private void getch() {
		// get the next char from the token stream and update index
		index++;
        if (index<length) ch=stream.inputChars[index];
        else ch= (char)-1;
	}

	private TokenStream stream;

	// ADD METHODS AND FIELDS
    HashMap<String, TokenStream.Kind> KEYWORD= new HashMap<String, TokenStream.Kind>();
	
 	public Scanner(TokenStream stream) {
		this.stream = stream;
		//ch=stream.inputChars[0];
		index=0;
		begOffset=0;
		length=stream.inputChars.length;
	    if (length >0) {
				 ch = stream.inputChars[0];
			 }
		     else
		    	 {
		    	 ch = (char)-1;
		 }
		length=stream.inputChars.length;
		//image,_int, _boolean, pixel,pixels, red, green, blue, Z, shape, width, height,location, 
		//x_loc, y_loc, SCREEN_SIZE, visible,x, y,pause, _while, _if, _else,
		KEYWORD.put("image", image);
		KEYWORD.put("int", _int);
		KEYWORD.put("boolean", _boolean);
		KEYWORD.put("pixel", pixel);
		KEYWORD.put("pixels", pixels);
		KEYWORD.put("blue", blue);
		KEYWORD.put("red", red);
		KEYWORD.put("green", green);
		KEYWORD.put("Z", Z);
		KEYWORD.put("shape", shape);
		KEYWORD.put("width", width);
		KEYWORD.put("height", height);
		KEYWORD.put("location", location);
		KEYWORD.put("x_loc", x_loc);
		KEYWORD.put("y_loc", y_loc);
		KEYWORD.put("SCREEN_SIZE", SCREEN_SIZE);
		KEYWORD.put("visible", visible);
		KEYWORD.put("x", x);
		KEYWORD.put("y", y);
		KEYWORD.put("pause", pause);
		KEYWORD.put("while", _while);
		KEYWORD.put("if", _if);
		KEYWORD.put("else", _else);
		//KEYWORD.put("true", BOOLEAN_LIT);
		//KEYWORD.put("false", BOOLEAN_LIT);
	}

	public void scan() throws LexicalException {
		// THIS IS PROBABLY COMPLETE
		Token t;
		do {
			t = next();
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else
				stream.tokens.add(t);
		} while (!t.kind.equals(EOF));
	}

	private Token next() throws LexicalException {
 		state = State.START;
		Token t = null;
		do {
			switch (state) {
			/*
			 * in each state, check the next character. either create a token or
			 * change state
			 */

			case START:
				begOffset = index;
				//char ch = stream.inputChars[index];
				switch (ch) {
				case (char) -1:
					state = State.EOF;
					break; // end of file
				case ' ': case '\t': case '\n':	case '\f':  case '\r':
					break; // white space
 //ASSIGN, OR, AND, EQ, NEQ, LT, GT, LEQ, GEQ, PLUS, MINUS, TIMES, DIV, MOD, NOT, LSHIFT, RSHIFT,

				case '=':
					state = State.GOT_EQUALS;
					break;
				case '|':
					t = stream.new Token(OR, begOffset, index+1);
					break;
				case '&':
					t = stream.new Token(AND, begOffset, index+1);
					break;
				// GOT_LT, GOTGT, GOT_EP, GOT_SLASH,
				case '<':
					state = State.GOT_LT;
					break;
				case '>':
					state = State.GOT_GT;
					break;
				case '!':
					state = State.GOT_EP;
					break;
				case '/':
					state = State.GOT_SLASH;
					break;
				case '*':
					t = stream.new Token(TIMES, begOffset, index+1);
					break;
				case '+':
					t = stream.new Token(PLUS, begOffset, index+1);
					break;
				case '-':
					t = stream.new Token(MINUS, begOffset, index+1);
					break;
				case '%':
					t = stream.new Token(MOD, begOffset, index+1);
					break;
			//DOT, SEMI, COMMA, LPAREN, RPAREN, LSQUARE, RSQUARE, LBRACE, RBRACE, COLON, QUESTION,
				case '.':
					t = stream.new Token(DOT, begOffset, index+1);
					break;
				case ';':
					t = stream.new Token(SEMI, begOffset, index+1);
					break;	
				case ',':
					t = stream.new Token(COMMA, begOffset, index+1);
					break;
				case '(':
					t = stream.new Token(LPAREN, begOffset, index+1);
					break;
				case ')':
					t = stream.new Token(RPAREN, begOffset, index+1);
					break;
				case '[':
					t = stream.new Token(LSQUARE, begOffset, index+1);
					break;
				case ']':
					t = stream.new Token(RSQUARE, begOffset, index+1);
					break;
				case '{':
					t = stream.new Token(LBRACE, begOffset, index+1);
					break;
				case '}':
					t = stream.new Token(RBRACE, begOffset, index+1);
					break;
				case ':':
					t = stream.new Token(COLON, begOffset, index+1);
					break;
				case '?':
					t = stream.new Token(QUESTION, begOffset, index+1);
					break;
				case '0':
					state = State.GOT_ZERO;
					break;
				case '"':
					state = State.STRING_LITERAL;
					break;
				default:
					if (Character.isDigit(ch)) {
						state = State.DIGITS;
					} else if (Character.isJavaIdentifierStart(ch)) {
						s.delete(0,s.length());
						s.append(ch);
						state = State.IDENT_PART;
					} else {
						throw stream.new LexicalException(begOffset, "ILLEGAL CHARACTER AT: " +ch);
						// handle error
					}
				}
				getch();
				break; // end of state START

			case GOT_ZERO:
				return stream.new Token(INT_LIT, begOffset, index);
			case DIGITS:
			//	ch = stream.inputChars[index];

				if (Character.isDigit(ch)) {
					getch();
					break;
					// state = State.DIGITS;

				} else {
					state = State.START;
					return stream.new Token(INT_LIT, begOffset, index);
				}
			//case IDENT_PART: 
				//ch = stream.inputChars[index];
				/*if (Character.isJavaIdentifierPart(ch)) {
				getch();
				break;}
				else 
				 { 
					 state=State.START;
					 return stream.new Token(IDENT,begOffset, index);
				 } */
			case IDENT_PART: 
				if (Character.isJavaIdentifierPart(ch)) {
					s.append(ch);					
					getch();
					break;
				}
				
				else
				{
					//System.out.println(s.toString());
				if (KEYWORD.containsKey(s.toString()))
				 {
					//System.out.println("this is a keyword");
					 state=State.KEYWORD;
				 } 
				else if (s.toString().equals("true") ||s.toString().equals("false"))
				 {
					 //System.out.println("this is a boolean");
					// state=State.START;
					 state=State.BOOLEAN;
					
				 } 
				 else 
				 { 
					 //state=State.START;
					 return stream.new Token(IDENT,begOffset, index);
				 }
				}
				break;
				 
			case KEYWORD:
				//System.out.println("this is a keyword: "+ KEYWORD.get(s.toString()));
				//getch();
				return stream.new Token(KEYWORD.get(s.toString()), begOffset, index);
			case BOOLEAN:
				//System.out.println("this is a boolean: "+s.toString());
				return stream.new Token(BOOLEAN_LIT, begOffset, index);
			case GOT_EQUALS:
				//ch = stream.inputChars[index];
				
				if (ch == '=') {
					getch();
					return stream.new Token(EQ, begOffset, index);
				} else {
					//state = State.START;
					return stream.new Token(ASSIGN, begOffset, index);
				}
				
				// GOT_LT, GOT_GT, GOT_EP, GOT_SLASH
			case GOT_LT:
				//ch = stream.inputChars[index];
				
				if (ch == '=') {
					//state = State.START;
					getch();
					return stream.new Token(LEQ, begOffset, index);
				} else if (ch == '<') {
					getch();
					//state = State.START;
					return stream.new Token(LSHIFT, begOffset, index);
				} else {
					//state = State.START;
					return stream.new Token(LT, begOffset, index);
				}
			case GOT_GT:
				//ch = stream.inputChars[index];
				if (ch == '=') {
					getch();
					//state = State.START;
					return stream.new Token(GEQ, begOffset, index);
				} else if (ch == '>') {
					//state = State.START;
					getch();
					return stream.new Token(RSHIFT, begOffset, index);
				} else {
					//state = State.START;
					return stream.new Token(GT, begOffset, index);
				}
			case GOT_EP:
				//ch = stream.inputChars[index];
				
				if (ch == '=') {
					//state = State.START;
					getch();
					return stream.new Token(NEQ, begOffset, index);
				} else {
					//state = State.START;
					return stream.new Token(NOT, begOffset, index);
				}
			case GOT_SLASH:
				//ch = stream.inputChars[index];
				if (ch == '/')
					{
					getch();
					state = State.COMMENT;
					//System.out.println("findslash");
					break;}
				else
				{
					return stream.new Token(DIV, begOffset, index);
				}
			case COMMENT:
				if (ch == '\n' || ch == '\f' || ch == '\r' || ch == (char) -1) {
				//	System.out.println("gotnl");
					return stream.new Token(COMMENT, begOffset, index);
				} 
				else {
					//System.out.println("continuereading");
					getch();
					break;					
				}
				
			case STRING_LITERAL:
				if(ch==(char) -1)
				{
					throw stream.new LexicalException(begOffset, "Lexical Exception while dealing with \"" ); }
					/*if(ch != (char) -1)
					{
						getch();
					}
					else if (ch == (char) -1)
					{
						throw stream.new LexicalException(begOffset, "Lexical Exception" );
					}
				}*/
				else if(ch == '"')
				{
					//System.out.println("this is a string literal");
					getch();
					return stream.new Token(STRING_LIT, begOffset, index);
					
				} 
				else
				{
					getch();
				}
				break;
			case EOF:
				 return stream.new Token(EOF, begOffset, index);
			default:
				assert false : "should not reach here";
			}// end of switch(state)
		} while (t == null); // loop terminates when a token is created
		return t;

		//;
		// COMPLETE THIS METHOD. THIS IS THE FUN PART!

	}

}
