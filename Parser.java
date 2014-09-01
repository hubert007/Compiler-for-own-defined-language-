package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.ast.*;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.Parser.SyntaxException;

public class Parser {

    @SuppressWarnings("serial")
    public class SyntaxException extends Exception {
        Token t;

        public SyntaxException(Token t, String msg) {
            super(msg);
            this.t = t;

        }

        public String toString() {
            return super.toString() + "\n" + t.toString();
        }

        public Kind getKind() {
            return t.kind;
        }
    }

    int i = 0;
    Token t;
    TokenStream stream;

    /* You will need additional fields */

    /**
     * creates a simple parser.
     * 
     * @param initialized_stream
     *          a TokenStream that has already been initialized by the Scanner
     * @throws LexicalException
     */
    public Parser(TokenStream initialized_stream) throws LexicalException {
    	errorList = new ArrayList<SyntaxException>();
        this.stream = initialized_stream;
        Scanner s = new Scanner(stream);
        s.scan();
        consume();

        /* You probably want to do more here */
    }

    /*
     * This method parses the input from the given token stream. If the input is
     * correct according to the phrase structure of the language, it returns
     * normally. Otherwise it throws a SyntaxException containing the Token where
     * the error was detected and an appropriate error message. The contents of
     * your error message will not be graded, but the "kind" of the token will be.
     */


    private boolean isKind(Kind... kinds) {
        Kind k = t.kind;
        for (int i = 0; i != kinds.length; ++i) {
            if (k == kinds[i])
                return true;
        }
        return false;
    }

    private Token match(Kind... kinds) throws SyntaxException {
    	Token t0=null;
    	t0=t;
        if (isKind(kinds)) {
            consume();
        }
        else {
            String s = "";
            for (int i = 0; i < kinds.length; i++) {
                s = s + kinds[i] + ", ";
            }
            error("match() from expected kind is: " + s + " but actual kind is: "
                    + t.kind);
        }
		return t0;
    }

    public void error(String msg) throws SyntaxException {

        throw new SyntaxException(t, msg);

    }

    /* just localize dealing with IOException from next method */
    private void consume() {

        t = stream.getToken(i++);
        //return t;
    }
    
  	
  //ADD THESE FIELDS  TO YOUR PARSER
  	Token progName;  //keep the program name in case you don't generate an AST
  	List<SyntaxException> errorList;  //save the error for grading purposes

  	/*public void ParserExample(TokenStream initialized_stream) {
  	  //...
  		//ADD THIS TO YOUR CONSTRUCTOR
  		errorList = new ArrayList<SyntaxException>();
  	}*/

  	
  	//THIS IS THE MAIN PUBLIC parse method.  Note that it does not throw exceptions.  
  	//If any make it to this level without having been caught, the exception is added to the list.
  	//If the program parsed correctly, return its AST. Otherwise return null.
  	public Program parse() {
  		Program p = null;
  		try{
  		p = parseProgram();
  	    match(EOF);
  		} 
  	 	catch(SyntaxException e){
  			errorList.add(e);
  		}
  		if (errorList.isEmpty()){
  			return p;
  		}
  		else 
  			return null;
  	}
  	
  	public List<SyntaxException> getErrorList(){
  		return errorList;
  	}
  	
  	public String getProgName(){
  		return (progName != null ?  progName.getText() : "no program name");
  	}



  	/**
  	 * Program ::= ident { Dec* Stmt* }
  	 * 
  	 * @throws SyntaxException
  	 */
  	private Program parseProgram() throws SyntaxException {
  		progName = match(IDENT);
  		match(LBRACE);
  		List<Dec> decList = new ArrayList<Dec>();
  		while (isKind(image,pixel,_int,_boolean)) {
  			//YOU PROBABLY HAVE SOMETHING LIKE JUST A CALL TO parseDec
  			//PUT IT IN A TRY-CATCH BLOCK, and IF AN EXCEPTION IS THROWN,
  			//TRY TO SKIP TOKENS UNTIL YOU GET TO EITHER A SEMI, OR
  			//THE BEGINNING OF A NEW DEC. 
  			try{
  				Dec dec=Dec();
  				if(dec!=null)
  			decList.add(dec);
  			}
  			catch(SyntaxException e){
  				errorList.add(e);
  				//skip tokens until next semi, consume it, then continue parsing
  				while (!isKind(SEMI,_int,_boolean,pixel, EOF)){ consume(); }
  				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
  			}
  		}

  		List<Stmt> stmtList = new ArrayList<Stmt>();
  		while (isKind(SEMI, IDENT, pause, _while, _if)) { 
  			try{
  				Stmt stmt = Stmt();
  				if(stmt!=null)
  			stmtList.add(stmt);
  			}
  			catch(SyntaxException e){
  				errorList.add(e);
  				//skip tokens until next semi, consume it, then continue parsing
  				while (!isKind(SEMI, IDENT, pause, _while, _if, EOF)){ consume(); }
  				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
  			}
  		}
  		match(RBRACE);
  		//After parsing has finished- if there were no errors, create and return a Program node, which is the AST of the program
  		if (errorList.isEmpty()) return new Program(progName, decList, stmtList);
  		//otherwise print out the errors and return null
  		//the test program will also look at the error list
  		System.out.println("error" + (errorList.size()>1?"s parsing program ":" parsing program ") + getProgName());
  		for(SyntaxException e: errorList){		
  			System.out.println(e.getMessage() + " at line" + e.t.getLineNumber());
  		}
  		 return null;
  	} 

  	
    //Dec ::=Type IDENT;
    private Dec Dec() throws SyntaxException
    {
      Dec e0=null;
   	  Kind e1=null;
      e1=t.kind;
      Type();
   	  Token e2=null;
      e2=match(IDENT);
   	  match(SEMI);
   	  e0=new Dec(e1,e2);
   	  return e0;
     }
   
    //TYPE :: = image|pixel|int|boolean
     private void Type() throws SyntaxException
     {
   	  if(isKind(image,pixel,_int,_boolean))
   	  {
   		 consume();
   	  } 
   	 else error("Error ! "+t.kind);
     }
     
   //Stmt ::= ; |AssignStmt | PauseStmt |IterationStmt | AlternativeStmt
	  private Stmt Stmt() throws SyntaxException
	 {   
		 Stmt e0=null;
		 if(isKind(SEMI))
			 {
			 match(SEMI);
			 }
		 else if(isKind(IDENT))
		 {
			 e0=AssignStmt();
		 }
		 else if(isKind(pause))
		 {
			 e0=PauseStmt();
		 }
		 else if(isKind(_while))
		 {
			 e0=IterationStmt();
		 }
		 else if(isKind(_if))
		 {
			 e0=AlternativeStmt();
		 }
		 else error("Error ! "+t.kind);
		 return e0;
	 }
	 
	  
	 private Stmt AssignStmt() throws SyntaxException
		 {
			Stmt e=null;
		    //IDNET
			 Token e1=null;
			 e1=match(IDENT);
			 //IDENT=
			 if(isKind(ASSIGN))
			 {
				 consume();
				 //IDENT = Expr
				 if(isKind(IDENT,INT_LIT,BOOLEAN_LIT,x,y,Z,SCREEN_SIZE,LPAREN))
				 {
					 Expr e2=null;
					 e2=Expr();
					 e=new AssignExprStmt(e1,e2);
				 }
				 //IDENT = Pixel
				 else if(isKind(LBRACE))
				 {
					 Pixel e2=null;
					 e2=Pixel();
					 e=new AssignPixelStmt(e1,e2);
				 }
				//IDENT = STRING_LIT
				 else if(isKind(STRING_LIT))
				 {
					 Token e2=null;
					 e2=match(STRING_LIT);
					 e=new FileAssignStmt(e1,e2);
				 }
				 else error("Error ! "+t.kind);
				 //IDENT = ... ;
				 match(SEMI);
			 }
			 //IDENT .
			 else if(isKind(DOT))
			 {
				 match(DOT);
				//IDENT . pixels [ Expr , Expr ]
				if(isKind(pixels))
				{
					match(pixels);
					match(LSQUARE);
					Expr e2=null;
					e2=Expr();
					match(COMMA);
					Expr e3=null;
					e3=Expr();
					match(RSQUARE);
					//IDENT . pixels [ Expr , Expr ] = Pixel ;
					if(isKind(ASSIGN))
					{
						match(ASSIGN);
						Pixel e4=null;
						e4=Pixel();
						match(SEMI);
						e=new SinglePixelAssignmentStmt(e1,e2,e3,e4);
					}
					//IDENT . pixels [ Expr , Expr ] (red | green | blue ) = Expr ;
					else if(isKind(red,green,blue))
					{
					    Token e4=null;
						e4=match(red,green,blue);
					    match(ASSIGN);
					    Expr e5=null;
					    e5=Expr();
					    match(SEMI);
					    e=new SingleSampleAssignmentStmt(e1,e2,e3,e4,e5);
					}
					else error("Error ! "+t.kind);
				}
				//IDENT . shape = [ Expr , Expr ] ;
				else if(isKind(shape))
				{
					match(shape);
					match(ASSIGN);
					match(LSQUARE);
					Expr e2=null;
					e2=Expr();
					match(COMMA);
					Expr e3=null;
					e3=Expr();
					match(RSQUARE);
					match(SEMI);
					e=new ShapeAssignmentStmt(e1,e2,e3);
				}
				//IDENT . location = [ Expr , Expr ] ;
				else if(isKind(location))
				{
					match(location);
					match(ASSIGN);
					match(LSQUARE);
					Expr e2=null;
					e2=Expr();
					match(COMMA);
					Expr e3=null;
					e3=Expr();;
					match(RSQUARE);
					match(SEMI);
					e=new ScreenLocationAssignmentStmt(e1,e2,e3);
				}
				//IDENT . visible = Expr ;
				else if(isKind(visible))
				{
					match(visible);
					match(ASSIGN);
					Expr e2=null;
					e2=Expr();
					match(SEMI);
					e=new SetVisibleAssignmentStmt(e1,e2);
				}
				else error("Error ! "+t.kind);
			 }
			 else error("Error ! "+t.kind);
			 return e;
		 }

	 

	 
	 //Pixel :: = {{ Expr , Expr , Expr }}
	 private Pixel Pixel() throws SyntaxException
	 {  
		 Pixel e0=null;
		 match(LBRACE);
		 match(LBRACE);
		 Expr e1=null;
		 e1=Expr();
		 match(COMMA);
		 Expr e2=null;
		 e2=Expr();
		 match(COMMA);
		 Expr e3=null;
		 e3=Expr();
		 match(RBRACE);
		 match(RBRACE);
		 e0=new Pixel(e1,e2,e3);
		 return e0;
	 }

	//Expr :: = OrExpr ( nothing | ? Expr : Expr )
		 // how to represent ?
	private Expr Expr() throws SyntaxException
	{
		Expr e0=null;
		Expr e1=null;
		e1=OrExpr();	
		e0=e1;
		Expr e2=null;
		Expr e3=null;
		if(isKind(QUESTION))
		{
			consume();
			e2=Expr();
			match(COLON);
			e3=Expr();		
			e0=new ConditionalExpr(e1,e2,e3);
		}
		else
		{
			
		}	
		
		return e0;
	}
   
   
   private Expr OrExpr() throws SyntaxException // OrExpr ::= AndExp( | AndExpr)*
   {
	   Expr e0=null;
       e0=AndExpr();
       Token e1=null;
       Expr e2=null;
       while (isKind(OR)) {           
           e1=match(OR);           
           e2=AndExpr();  
           e0=new BinaryExpr(e0,e1,e2);
       }      
       
       return e0;
   }

 //AndExpr ::= EqualityExpr ( & EqualityExpr )*
 	private Expr AndExpr() throws SyntaxException
 	 {
 		 Expr e0=null;
 		 e0=EqualityExpr();
 		 Token e2=null;
         Expr e3=null;
 		 while(isKind(AND))
 		 {			 
             e2=match(AND);
     		 e3=EqualityExpr();
     		 e0=new BinaryExpr(e0,e2,e3);
 		 }
 		
 		 return e0;
 	 }
 	 
 	//EqualityExpr ::= RelExpr ( ( == | != ) RelExpr ) *
 	private Expr EqualityExpr() throws SyntaxException
 	 {
 		 Expr e0=null;
 		 e0=RelExpr();
 		 Token e2=null;
         Expr e3=null;
 		 while(isKind(EQ)||isKind(NEQ))
 		 {
             e2=match(EQ,NEQ);
     		 e3=RelExpr();
     		e0=new BinaryExpr(e0,e2,e3);
 		 }
		 
 		 return e0;
 	 }

 	//RelExpr ::= ShiftExpr ( ( < | > | ≤ | ≥) ShiftExpr ) *
 	private Expr RelExpr() throws SyntaxException
 	{
 		Expr e0=null;
 		e0=ShiftExpr();
 		Token e2=null;
        Expr e3=null;
 		while(isKind(LT)||isKind(GT)||isKind(LEQ)||isKind(GEQ))
 		{
            e2=match(LT,GT,LEQ,GEQ);
     		e3=ShiftExpr();
     		e0=new BinaryExpr(e0,e2,e3);
		 }
		 
		 return e0;
 	}
 		 
 		 
 	//ShiftExpr ::= AddExpr ( ( << | >>) AddExpr )*
 	private Expr ShiftExpr() throws SyntaxException
 	{
 		Expr e0=null;
 		e0=AddExpr();
 		Token e2=null;
        Expr e3=null;
 		while(isKind(LSHIFT)||isKind(RSHIFT))
 		{
            e2=match(LSHIFT,RSHIFT);
     		e3=AddExpr();
     		e0=new BinaryExpr(e0,e2,e3);
		 }
		 
		 return e0; 
 	}


 	//AddExpr ::= MultExpr ( ( + | - ) MultExpr ) *
 	private Expr AddExpr() throws SyntaxException
 	{
 		Expr e0=null;
 		e0=MultiExpr();
 		Token e2=null;
        Expr e3=null;
 		while(isKind(PLUS)||isKind(MINUS))
 		{
            e2=match(PLUS,MINUS);
 			e3=MultiExpr();
 			e0=new BinaryExpr(e0,e2,e3);
		 }
 		 
		 return e0; 
 	}
 		 
 	//MultExpr ::= PrimaryExpr ( ( * | / | % ) PrimaryExpr )*
 	private Expr MultiExpr() throws SyntaxException
 	{
 		Expr e0=null;
 		e0=PrimaryExpr();
 		Token e2=null;
        Expr e3=null;
 		while(isKind(TIMES)||isKind(DIV)||isKind(MOD))
 		{
            e2=match(TIMES,DIV,MOD);
 			e3=PrimaryExpr();
 			e0=new BinaryExpr(e0,e2,e3);
		 }
		
		 return e0;
 	}


 	private Expr PrimaryExpr() throws SyntaxException 
 	{
 		Expr e=null;
 		if(isKind(IDENT))
		{
 			Token e1=null;
			e1=match(IDENT);
			if(isKind(LSQUARE))
			{
				consume();
				Expr e2=null;
				e2=Expr();
				match(COMMA);
				Expr e3=null;
				e3=Expr();
				match(RSQUARE);
				Token e4=null;
				if(isKind(red,green,blue))
					{					    						
						e4=match(red,green,blue);						
					}
				else error("Error ! "+t.kind);
				e=new SampleExpr(e1,e2,e3,e4);
				 }
			else if (isKind(DOT)) {
				consume();
				Token e2=null;
				if (isKind(height,width,x_loc,y_loc)) {
					
					e2=match(height,width,x_loc,y_loc);
					
				}
				else error("Error ! "+t.kind);
				e=new ImageAttributeExpr(e1,e2);
			}
			else
			{
				e=new IdentExpr(e1);
			}
	   }

 		else if (isKind(INT_LIT)) {
 			Token e1=null;
 			e1=match(INT_LIT);
 			e=new IntLitExpr(e1);
 		}
 		else if (isKind(BOOLEAN_LIT)) {
 			Token e1=null;
 			e1=match(BOOLEAN_LIT);
 			e=new BooleanLitExpr(e1);
 		}
 		else if (isKind(x,y,Z,SCREEN_SIZE)) {
 			Token e1=null;
 			e1=match(x,y,Z,SCREEN_SIZE);
 			e=new PreDefExpr(e1);
 		}
 		else if(isKind(LPAREN))
 		{
 			match(LPAREN);
 			e=Expr();
 			match(RPAREN);
 		}
 		else error("Error ! "+t.kind);
		return e;
 		}

 	// PauseStmt ::= pause Expr ;
 	public Stmt PauseStmt() throws SyntaxException
 	{
 		Stmt e0=null;
 		Expr e1 = null;
 		match(pause);
 		e1=Expr();
 		match(SEMI);
 		e0=new PauseStmt(e1);
 		return e0;
 	}
 		 
 	
 		 
 	//IterationStmt ::= while ( Expr ) { Stmt* }
 	private Stmt IterationStmt() throws SyntaxException
 	{
 		Stmt e0=null;
 		match(_while);
 		match(LPAREN);
 		Expr e1=null;
 		e1=Expr();
 		match(RPAREN);
 		match(LBRACE); 
 		List<Stmt> e2=new ArrayList<Stmt>();
		
 		while(isKind(SEMI,IDENT,pause,_while,_if))
 		{	 try{ 		
 			Stmt stmt = Stmt();
				if(stmt!=null)
 	   		  e2.add(stmt); 	   		  
 			 }
 		catch(SyntaxException e){
 			errorList.add(e);
 			while(!isKind(SEMI,IDENT,pause,_while,_if,EOF)){consume();}
 			if (isKind(SEMI)){consume();}
 		}
 		}
 			 match(RBRACE);
 		e0=new IterationStmt(e1,e2);
 	    return e0;
 	}
 		 

 	//AlternativeStmt ::= if ( Expr ) { Stmt *} | if ( Expr ) { Stmt* } else { Stmt* }
 	private Stmt AlternativeStmt() throws SyntaxException
 	{
 		Stmt e0=null;
 		match(_if);
 		match(LPAREN);
 		Expr e1=null;
 		e1=Expr();
 		match(RPAREN);
 		match(LBRACE); 
 		List<Stmt> e2=new ArrayList<Stmt>();
		
		List<Stmt> e3=new ArrayList<Stmt>();
		
		while (isKind(SEMI, IDENT, pause, _while, _if)) { 
  			try{
  				Stmt stmt = Stmt();
  				if(stmt!=null)
  			e2.add(stmt);
  			}
  			catch(SyntaxException e){
  				errorList.add(e);
  				//skip tokens until next semi, consume it, then continue parsing
  				while (!isKind(SEMI, IDENT, pause, _while, _if, EOF)){ consume(); }
  				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
  			}
  		}
 		match(RBRACE);
 		if(isKind(_else))
 		{
 			match(_else);
 			match(LBRACE); 
 			while (isKind(SEMI, IDENT, pause, _while, _if)) { 
 	  			try{
 	  				Stmt stmt = Stmt();
 	  				if(stmt!=null)
 	  			e3.add(stmt);
 	  			}
 	  			catch(SyntaxException e){
 	  				errorList.add(e);
 	  				//skip tokens until next semi, consume it, then continue parsing
 	  				while (!isKind(SEMI, IDENT, pause, _while, _if, EOF)){ consume(); }
 	  				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
 	  			}
 	  		}
 			match(RBRACE);
 			
 			}
 		else
 			{
 				
 				 }
 		e0=new AlternativeStmt(e1,e2,e3);
 		return e0;
 		}
 	
 		 
}
