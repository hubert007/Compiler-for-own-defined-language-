package cop5555fa13.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cop5555fa13.TokenStream.Kind.*;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class TypeCheckVisitor implements ASTVisitor {
	
	List<ASTNode> errorNodeList;
	StringBuilder errorLog;
	static HashMap<String,Dec> symbolTable;
	
	public TypeCheckVisitor(){
		
		symbolTable= new HashMap<String,Dec>();
		errorNodeList = new ArrayList();
		errorLog = new StringBuilder();
	}
	
	
	public List getErrorNodeList(){return errorNodeList;}
	public boolean isCorrect(){
		return errorNodeList.size()==0;
	}
	
	public String getLog(){
		return errorLog.toString();
	}
	
	
	private void check(boolean b, ASTNode node, String msg) {
		// TODO Auto-generated method stub
		if(b==false)
		{
			errorNodeList.add(node);
			errorLog.append(msg);
		}
	}	
	
	private Kind lookupType(Token ident) {
		// TODO Auto-generated method stub
		if( symbolTable.containsKey(ident.getText()))
		{
			
			return symbolTable.get(ident.getText()).type;
		}
		else
		{
			return null;
		}
		
	}
	
	Token ProgramName;
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ProgramName=program.ident;
		for(int i=0;i<program.decList.size();i++)
		{
			program.decList.get(i).visit(this, null);
		}
		for(int j=0;j<program.stmtList.size();j++)
		{
			program.stmtList.get(j).visit(this, null);
		}
		return null;
	}
	
	
	@Override
	public Object visitDec(Dec dec, Object arg) {
		// TODO Auto-generated method stub
		if (symbolTable.containsKey(dec.ident.getText()))  
			check(false, dec, "type of ident must not be in the symbol talbe");
		else if (dec.ident.equals( ProgramName ))  
			check(false, dec, "ident cannot be program name");
		else  
			symbolTable.put(dec.ident.getText(), dec);  
		return null;
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind eType=(Kind)alternativeStmt.expr.visit(this,null);
		check(eType==_boolean,alternativeStmt,"expr must be boolean");
		for (int i=0; i<alternativeStmt.ifStmtList.size(); i++) {
			alternativeStmt.ifStmtList.get(i).visit(this, null);
		}
		for (int j=0; j<alternativeStmt.elseStmtList.size(); j++){
			alternativeStmt.elseStmtList.get(j).visit(this, null);
		}
		return null;
	}



	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind iType=lookupType(assignExprStmt.lhsIdent);
		Kind eType=(Kind)assignExprStmt.expr.visit(this,null);
		check(eType==iType, assignExprStmt,"assignExpr must be the same as ident");
		return null;
	}

	
	
	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(assignPixelStmt.lhsIdent)==pixel||lookupType(assignPixelStmt.lhsIdent)==image , assignPixelStmt,assignPixelStmt + "must be pixel");
		assignPixelStmt.pixel.visit(this,null);
		return null;
	}
	
	@Override
	public Object FileAssignStmt(FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(fileAssignStmt.lhsIdent)==image, fileAssignStmt,fileAssignStmt + "must be image");
		return null;
	}
	
	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(screenLocationAssignmentStmt.lhsIdent)==image, screenLocationAssignmentStmt,screenLocationAssignmentStmt + "must be image");
		Kind xType=(Kind)screenLocationAssignmentStmt .xScreenExpr.visit(this,null); //get type at xExpr
		check(xType==_int, screenLocationAssignmentStmt,"xExpr must be int");
		Kind yType=(Kind)screenLocationAssignmentStmt .yScreenExpr.visit(this,null);
		check(yType==_int, screenLocationAssignmentStmt,"yExpr must be int");
		return null;
	}
	
	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(setVisibleAssignmentStmt.lhsIdent)==image, setVisibleAssignmentStmt,setVisibleAssignmentStmt + "must be image");
		Kind eType=(Kind)setVisibleAssignmentStmt.expr.visit(this,null); 
		check(eType==_boolean, setVisibleAssignmentStmt,"expr must be boolean");
		return null;
	}
	
	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(shapeAssignmentStmt.lhsIdent)==image, shapeAssignmentStmt,shapeAssignmentStmt + "must be image");
		Kind wType=(Kind)shapeAssignmentStmt .width.visit(this,null); //get type at xExpr
		check(wType==_int, shapeAssignmentStmt,"width must be int");
		Kind hType=(Kind)shapeAssignmentStmt .height.visit(this,null);
		check(hType==_int, shapeAssignmentStmt,"height must be int");
		return null;
	}
	
	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(singlePixelAssignmentStmt.lhsIdent)==image, singlePixelAssignmentStmt,singlePixelAssignmentStmt + "must be image");
		Kind xType=(Kind)singlePixelAssignmentStmt .xExpr.visit(this,null); //get type at xExpr
		check(xType==_int,singlePixelAssignmentStmt,"xExpr must be int");
		Kind yType=(Kind)singlePixelAssignmentStmt .yExpr.visit(this,null);
		check(yType==_int, singlePixelAssignmentStmt,"yExpr must be int");
		singlePixelAssignmentStmt.pixel.visit(this,null);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		check(lookupType(singleSampleAssignmentStmt.lhsIdent)==image, singleSampleAssignmentStmt,singleSampleAssignmentStmt + "must be image");
		Kind xType=(Kind)singleSampleAssignmentStmt .xExpr.visit(this,null); //get type at xExpr
		check(xType==_int, singleSampleAssignmentStmt,"xExpr must be int");
		Kind yType=(Kind)singleSampleAssignmentStmt .yExpr.visit(this,null);
		check(yType==_int,singleSampleAssignmentStmt,"yExpr must be int");
		Kind rType=(Kind)singleSampleAssignmentStmt .rhsExpr.visit(this,null);
		check(rType==_int, singleSampleAssignmentStmt,"rExpr must be int");
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind yType=(Kind)iterationStmt .expr.visit(this,null);
		check(yType==_boolean,iterationStmt,"Expr must be boolean");
		for (int i=0; i<iterationStmt.stmtList.size(); i++) {
			iterationStmt.stmtList.get(i).visit(this, null);
		}
		return null;
	}
			
	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind yType=(Kind)pauseStmt .expr.visit(this,null);
		check(yType==_int, pauseStmt,"yExpr must be int");
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind thetype = null;
		Kind oType=binaryExpr.op.kind;
		if(oType==OR||oType==AND)
		{
			Kind e0Type=(Kind)binaryExpr.e0.visit(this,null);
			check(e0Type==_boolean, binaryExpr,"Expr must be boolean");
			Kind e1Type=(Kind)binaryExpr.e1.visit(this,null);
			check(e1Type==_boolean, binaryExpr,"Expr must be boolean");
			thetype= _boolean;
		}
		else if(oType==PLUS||oType==MINUS||oType==TIMES||oType==DIV||oType==MOD)
		{
			Kind e0Type=(Kind)binaryExpr.e0.visit(this,null);
			check(e0Type==_int, binaryExpr,"Expr must be int");
			Kind e1Type=(Kind)binaryExpr.e1.visit(this,null);
			check(e1Type==_int, binaryExpr,"Expr must be int");
			thetype=_int;
		}
		else if(oType==EQ||oType==NEQ)
		{
			Kind e0Type=(Kind)binaryExpr.e0.visit(this,null);
			Kind e1Type=(Kind)binaryExpr.e1.visit(this,null);
			check(e0Type==e1Type, binaryExpr,"Expr0 must be the same as Expr1");
			thetype=_boolean;
		}
		else if(oType==LSHIFT||oType==RSHIFT)
		{
			Kind e0Type=(Kind)binaryExpr.e0.visit(this,null);
			check(e0Type==_int, binaryExpr,"Expr must be int");
			Kind e1Type=(Kind)binaryExpr.e1.visit(this,null);
			check(e1Type==_int, binaryExpr,"Expr must be int");
			thetype= _int;
		}
		else if(oType==LT||oType==GT||oType==LEQ||oType==GEQ)
		{
			Kind e0Type=(Kind)binaryExpr.e0.visit(this,null);
			check(e0Type==_int, binaryExpr,"Expr must be int");
			Kind e1Type=(Kind)binaryExpr.e1.visit(this,null);
			check(e1Type==_int, binaryExpr,"Expr must be int");
			thetype=_boolean;
		}
		return thetype;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		return _boolean;
	}
	
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind cType=(Kind)conditionalExpr.condition.visit(this,null);
		check(cType==_boolean, conditionalExpr,"Expr must be boolean");
		Kind tType=(Kind)conditionalExpr.trueValue.visit(this,null);
		Kind fType=(Kind)conditionalExpr.falseValue.visit(this,null);
		check(tType==fType, conditionalExpr,"truevalue must be the same type as falsevalue");
		return tType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		return lookupType(identExpr.ident);
	}
	
	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(imageAttributeExpr.ident)==image, imageAttributeExpr,imageAttributeExpr + "must be image");
		return _int;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		return _int;
	}
	

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		return _int;
	}
	
	
	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		check(lookupType(sampleExpr.ident)==image,sampleExpr,sampleExpr + "must be image");
		Kind xType=(Kind)sampleExpr .xLoc.visit(this,null); 
		check(xType==_int,sampleExpr,"xLoc must be int");
		Kind yType=(Kind)sampleExpr.yLoc.visit(this,null);
		check(yType==_int, sampleExpr,"yExpr must be int");
		return _int;
	}
	
	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind rType=(Kind)pixel .redExpr.visit(this,null); 
		check(rType==_int,pixel,"Expr must be int");
		Kind gType=(Kind)pixel.greenExpr.visit(this,null);
		check(gType==_int, pixel,"Expr must be int");
		Kind bType=(Kind)pixel.blueExpr.visit(this,null);
		check(bType==_int, pixel,"Expr must be int");
		return null;
	}
}
