package cop5555fa13.ast;

import static cop5555fa13.TokenStream.Kind.*;
import static cop5555fa13.TokenStream.Kind;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5555fa13.runtime.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	


	private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	private String progName;
	
	private int slot = 0;
	private int getSlot(String name){
		Integer s = slotMap.get(name);
		if (s != null) return s;
		else{
			slotMap.put(name, slot);
			return slot++;
		}		
	}


	HashMap<String,Integer> slotMap = new HashMap<String,Integer>();
	
	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	@Override
	public Object visitDec(Dec dec, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(),l);
		//get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		String jvmType = typeMap.get(t);
		Object initialValue = (t == _int || t==pixel || t== _boolean) ? Integer.valueOf(0) : null;
		//add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		//if this is an image, generate code to create an empty image
		if (t == image){
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		System.out.println("visiting Dec method");
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		//Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);		
		
		getSlot("x");
		getSlot("y");
		
		//visit children
		for(Dec dec : program.decList){
			dec.visit(this,mv);
		}
		for (Stmt stmt : program.stmtList){
			stmt.visit(this, mv);
		}
		
		
		//add a return statement to the main method
		mv.visitInsn(RETURN);
		
		//finish up
		Label end = new Label();
		mv.visitLabel(end);
		//visit local variables. The one is slot 0 is the formal parameter of the main method.
		mv.visitLocalVariable("args","[Ljava/lang/String;",null, start, end, getSlot("args"));
		//if there are any more local variables, visit them now.
		// ......
		mv.visitLocalVariable("x" , "I", null, start, end, getSlot("x"));
		mv.visitLocalVariable("y" , "I", null, start, end, getSlot("y"));
		//finish up method
		mv.visitMaxs(1,1);
		mv.visitEnd();
		//convert to bytearray and return 
		return cw.toByteArray();
	}

	//assignment 6
	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		alternativeStmt.expr.visit(this, mv);
		Label elseLabel = new Label();
	    Label endOfAlternativeLabel = new Label();
	    mv.visitJumpInsn(IFEQ, elseLabel);	    
	    for(int i=0; i<alternativeStmt.ifStmtList.size();i++)
	    {
	    	alternativeStmt.ifStmtList.get(i).visit(this, mv);
	    }
	    mv.visitJumpInsn(GOTO, endOfAlternativeLabel);
	    mv.visitLabel(elseLabel);
	    for(int i=0; i<alternativeStmt.elseStmtList.size();i++)
	    {
	    	alternativeStmt.elseStmtList.get(i).visit(this, mv);
	    }
	    mv.visitLabel(endOfAlternativeLabel);
		System.out.println("visiting AlternativeStmt method");  // TODO Auto-generated method stub
		return null;
	}
   
	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pauseStmt.expr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "pause", PLPImage.pauseDesc);
		System.out.println("visiting PauseStmt method");  // TODO Auto-generated method stub
		return null;
	}

	//assignment6
	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label bodyLabel = new Label();
		Label guardLabel= new Label();
		mv.visitJumpInsn(GOTO, guardLabel);
		mv.visitLabel(bodyLabel);
		for(int i=0; i<iterationStmt.stmtList.size();i++)
		    {
		    	iterationStmt.stmtList.get(i).visit(this, mv);
		    }
		mv.visitLabel(guardLabel);
		iterationStmt.expr.visit(this, mv);
		mv.visitJumpInsn(IFNE, bodyLabel);
		System.out.println("visiting IterationStmt visit method");  // TODO Auto-generated method stub
		return null;
	}

   //assignment 6
	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lhs=assignPixelStmt.lhsIdent.getText();
		Kind lkind = TypeCheckVisitor.symbolTable.get(lhs).type;
		if(lkind==pixel)
		{
			assignPixelStmt.pixel.visit(this, mv);
			mv.visitFieldInsn(PUTSTATIC, progName, lhs, typeMap.get(TypeCheckVisitor.symbolTable.get(lhs).type));
		}
		else if(lkind ==image)
		{
			Label xlabel = new Label();
			Label ylabel = new Label();
			Label bodylabel=new Label();
			Label endlabel=new Label();
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE, getSlot("x"));
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE, getSlot("y"));
			//xxxxxxxxxxxxxxxxxxxxxxx
			mv.visitLabel(xlabel);
			mv.visitVarInsn(ILOAD, getSlot("x"));
			mv.visitFieldInsn(GETSTATIC, progName, lhs, PLPImage.classDesc);
			mv.visitFieldInsn(GETFIELD,PLPImage.className,"width","I");
			mv.visitJumpInsn(IF_ICMPLT, ylabel);
			mv.visitJumpInsn(GOTO, endlabel);
			//yyyyyyyyyyyyyyyyyyyyyyyy
			mv.visitLabel(ylabel);
			mv.visitVarInsn(ILOAD, getSlot("y"));
			mv.visitFieldInsn(GETSTATIC, progName, lhs, PLPImage.classDesc);
			mv.visitFieldInsn(GETFIELD,PLPImage.className,"height","I");
			mv.visitJumpInsn(IF_ICMPLT, bodylabel);
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE, getSlot("y"));
			mv.visitVarInsn(ILOAD,  getSlot("x"));
			mv.visitLdcInsn(1);
			mv.visitInsn(IADD);
			mv.visitVarInsn(ISTORE, getSlot("x"));
			mv.visitJumpInsn(GOTO, xlabel);
			//body
			mv.visitLabel(bodylabel);
			mv.visitFieldInsn(GETSTATIC, progName, lhs, PLPImage.classDesc);
			mv.visitVarInsn(ILOAD, getSlot("x"));
			mv.visitVarInsn(ILOAD, getSlot("y"));
			assignPixelStmt.pixel.visit(this, mv);
	        mv.visitMethodInsn(INVOKEVIRTUAL,PLPImage.className , "setPixel", "(III)V");
	        mv.visitVarInsn(ILOAD, getSlot("y"));
	        mv.visitLdcInsn(1);
	        mv.visitInsn(IADD);
	        mv.visitVarInsn(ISTORE,getSlot("y"));
	        mv.visitJumpInsn(GOTO,ylabel);
	        mv.visitLabel(endlabel);
	        mv.visitFieldInsn(GETSTATIC, progName, lhs, PLPImage.classDesc);
	        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
			
		}
		System.out.println("visiting AssignPixelStmt visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pixel.redExpr.visit(this, mv);
		pixel.greenExpr.visit(this, mv);
		pixel.blueExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, "cop5555fa13/runtime/Pixel", "makePixel", cop5555fa13.runtime.Pixel.makePixelSig);
		System.out.println("visiting Pixel method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String iident = singlePixelAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, iident, PLPImage.classDesc);
		mv.visitInsn(DUP);
		singlePixelAssignmentStmt.xExpr.visit(this,mv);
		singlePixelAssignmentStmt.yExpr.visit(this,mv);
		singlePixelAssignmentStmt.pixel.visit(this,mv);
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel", "(III)V");
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"updateFrame", PLPImage.updateFrameDesc);
		System.out.println("visiting SinglePixelAssignment method "+iident);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv=(MethodVisitor) arg;
		String sident= singleSampleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, sident, PLPImage.classDesc);
		mv.visitInsn(DUP);
		singleSampleAssignmentStmt.xExpr.visit(this, mv);
		singleSampleAssignmentStmt.yExpr.visit(this, mv);
		if (singleSampleAssignmentStmt.color.kind == red)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.RED);
		}
		else if (singleSampleAssignmentStmt.color.kind == green)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.GRN);
		}
		else if (singleSampleAssignmentStmt.color.kind == blue)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.BLU);		
		}
		singleSampleAssignmentStmt.rhsExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setSample", "(IIII)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		System.out.println("visiting SingleSampleAssignmentStmt method " +sident+" "+singleSampleAssignmentStmt.color.kind);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		MethodVisitor mv=(MethodVisitor) arg;
		String sident = screenLocationAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, sident, PLPImage.classDesc);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		screenLocationAssignmentStmt.xScreenExpr.visit(this,mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "x_loc", "I");
		screenLocationAssignmentStmt.yScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "y_loc", "I");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		System.out.println("visiting ScreenLocationAssignmentStmt method " +sident );  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sident = shapeAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,sident,PLPImage.classDesc);  
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		shapeAssignmentStmt.width.visit(this,mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "width", "I");	
		shapeAssignmentStmt.height.visit(this,mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "height", "I");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateImageSize", "()V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		System.out.println("visiting shapeAssignmentStmt method " + sident);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//duplicate address.  Will consume one for updating setVisible field
		//and one for invoking updateFrame.
		mv.visitInsn(DUP);
		//visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this,mv);
		//set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", 
				"Z");	
	    //generate code to update frame, consuming the second image address.
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
	    System.out.println("visiting SetVisibleAssignmentStmt method");
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave address of target image on top of stack
	    String image_name = fileAssignStmt.lhsIdent.getText();
	    mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
	    //generate code to duplicate this address.  We'll need it for loading
	    //the image and again for updating the frame.
	    mv.visitInsn(DUP);
		//generate code to leave address of String containing a filename or url
	    mv.visitLdcInsn(fileAssignStmt.fileName.getText().replace("\"", ""));
		//generate code to get the image by calling the loadImage method
	    mv.visitMethodInsn(INVOKEVIRTUAL, 
	    		PLPImage.className, "loadImage", PLPImage.loadImageDesc);
	    //generate code to update frame
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
	    System.out.println("visiting FileAssignStmt method");
		return null;
	}

	//assignment 6
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		MethodVisitor mv=(MethodVisitor) arg;
		Label falseConditionLabel = new Label();
		Label endOfExprLabel = new Label();
		conditionalExpr.condition.visit(this, mv);
		mv.visitJumpInsn(IFEQ, falseConditionLabel);
		conditionalExpr.trueValue.visit(this, mv);
		mv.visitJumpInsn(GOTO, endOfExprLabel);
		mv.visitLabel(falseConditionLabel);
		conditionalExpr.falseValue.visit(this, mv);
		mv.visitLabel(endOfExprLabel);
		System.out.println("visiting ConditionalExpr method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		MethodVisitor mv=(MethodVisitor) arg;
		binaryExpr.e0.visit(this, mv);
		binaryExpr.e1.visit(this, mv);
		Label oplabel = new Label();
		Label endlabel = new Label();
		if(binaryExpr.op.kind==PLUS)
		{
			mv.visitInsn(IADD);
		}
		else if(binaryExpr.op.kind==MINUS)
		{
			mv.visitInsn(ISUB);
		}
		else if(binaryExpr.op.kind==TIMES)
		{
			mv.visitInsn(IMUL);
		}
		else if(binaryExpr.op.kind==DIV)
		{
			mv.visitInsn(IDIV);
		}
		else if(binaryExpr.op.kind==MOD)
		{
			mv.visitInsn(IREM);
		}
		else if(binaryExpr.op.kind==LSHIFT)
		{
			mv.visitInsn(ISHL);
		}
		else if(binaryExpr.op.kind==RSHIFT)
		{
			mv.visitInsn(ISHR);
		}
		else if(binaryExpr.op.kind==AND)
		{
			mv.visitInsn(IAND);
		}
		else if(binaryExpr.op.kind==OR)
		{
			mv.visitInsn(IOR);
		}
		else
		{
			if(binaryExpr.op.kind==GT)
			{
				mv.visitJumpInsn(IF_ICMPGT, oplabel);
			}
			else if(binaryExpr.op.kind==LT)
			{
				mv.visitJumpInsn(IF_ICMPLT, oplabel);
			}
			else if(binaryExpr.op.kind==GEQ)
			{
				mv.visitJumpInsn(IF_ICMPGE, oplabel);
			}
			else if(binaryExpr.op.kind==LEQ)
			{
				mv.visitJumpInsn(IF_ICMPLE, oplabel);
			}
			else if(binaryExpr.op.kind==EQ)
			{
				mv.visitJumpInsn(IF_ICMPEQ, oplabel);
			}
			else if(binaryExpr.op.kind==NEQ)
			{
				mv.visitJumpInsn(IF_ICMPNE, oplabel);
			}
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, endlabel);
			mv.visitLabel(oplabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endlabel);
		}
		System.out.println("visiting BinaryExpr method " + binaryExpr.op.kind );  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		MethodVisitor mv=(MethodVisitor) arg;
		String sident=sampleExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, sident, PLPImage.classDesc);
		sampleExpr.xLoc.visit(this, mv);
		sampleExpr.yLoc.visit(this, mv);
		if(sampleExpr.color.kind==red)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.RED);
		}
		else if(sampleExpr.color.kind==green)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.GRN);
		}
		else if(sampleExpr.color.kind==blue)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.BLU);
		}
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getSample", "(III)I");	
		System.out.println("visiting SampleExpr method "+sident+" "+ sampleExpr.color.kind);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		MethodVisitor mv= (MethodVisitor) arg;
		String iident=imageAttributeExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, iident, PLPImage.classDesc);
		if(imageAttributeExpr.selector.kind==height)
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeight", "()I");
		}
		else if(imageAttributeExpr.selector.kind==width)
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidth", "()I");
		}
		else if(imageAttributeExpr.selector.kind==x_loc)
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getX_loc", "()I");
		}
		else if(imageAttributeExpr.selector.kind==y_loc)
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getY_loc", "()I");
		}
		System.out.println("visiting ImageAttributeExpr method " +iident + " " +imageAttributeExpr.selector.kind);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		MethodVisitor mv= (MethodVisitor) arg;
		String iident=identExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, identExpr.ident.getText(),typeMap.get(TypeCheckVisitor.symbolTable.get(iident).type ));	
		System.out.println("visiting IdentExpr method " + iident);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		int lit = intLitExpr.intLit.getIntVal();
		mv.visitLdcInsn(lit);
		System.out.println("visiting IntLitExpr method " +lit);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true")? 1 : 0;
		mv.visitLdcInsn(val);
		System.out.println("visiting BooleanLitExpr method " + val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		if(PreDefExpr.constantLit.kind==Z)
		{
		   mv.visitLdcInsn(cop5555fa13.runtime.ImageConstants.Z);
		}
		else if(PreDefExpr.constantLit.kind==SCREEN_SIZE)
		{
			mv.visitLdcInsn(cop5555fa13.runtime.PLPImage.SCREENSIZE);
		}
		else if(PreDefExpr.constantLit.kind==x)
		{
			mv.visitVarInsn(ILOAD, getSlot("x"));
		}
		else if(PreDefExpr.constantLit.kind==y)
		{
			mv.visitVarInsn(ILOAD, getSlot("y"));
		}
		System.out.println("visiting PreDefExpr method "+ PreDefExpr.constantLit.kind);  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String aident = assignExprStmt.lhsIdent.getText();
		String atype = typeMap.get(TypeCheckVisitor.symbolTable.get(aident).type);
		assignExprStmt.expr.visit(this,mv);
		mv.visitFieldInsn(PUTSTATIC, progName, aident, atype);
		System.out.println("visiting AssignExprStmt method "+aident + " " +atype );  // TODO Auto-generated method stub
		return null;
	}

}
