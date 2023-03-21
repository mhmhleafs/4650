import absyn.*;
import java.io.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;
  FileWriter myWriter;

  private void indent( int level ) {
    try{
      for( int i = 0; i < level * SPACES; i++ ) myWriter.write( " " );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      if(expList.head != null)
        expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "AssignExp: \n" );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.lhs != null)
      exp.lhs.accept( this, level );
    if(exp.rhs != null)
      exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "IfExp: \n" );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.test != null)
      exp.test.accept( this, level );
    if(exp.thenpart != null)
      exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "IntExp: " + exp.value + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    try{
      myWriter.write( "OpExp:" ); 
      switch( exp.op ) {
        case OpExp.PLUS:
          myWriter.write( " + " );
          break;
        case OpExp.MINUS:
          myWriter.write( " - " );
          break;
        case OpExp.MUL:
          myWriter.write( " * " );
          break;
        case OpExp.DIV:
          myWriter.write( " / " );
          break;
        case OpExp.EQ:
          myWriter.write( " = " );
          break;
        case OpExp.LT:
          myWriter.write( " < " );
          break;
        case OpExp.GT:
          myWriter.write( " > " );
          break;
        case OpExp.UMINUS:
          myWriter.write( " - " );
          break;
        case OpExp.NE:
          myWriter.write( " != " );
          break;
        case OpExp.GE:
          myWriter.write( " >= " );
          break;
        case OpExp.LE:
          myWriter.write( " <= " );
          break;
        case OpExp.NOT:
          myWriter.write( " ~ " );
          break;
        case OpExp.AND:
          myWriter.write( " && " );
          break;
        case OpExp.OR:
          myWriter.write( " || " );
          break;
        default:
          System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
      }
      myWriter.write("\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if (exp.left != null)
       exp.left.accept( this, level );
    if(exp.right != null)
       exp.right.accept( this, level );
  }

  public void visit( VarExp exp, int level ) {
    //indent( level );
    //System.out.println( "VarExp: " + exp.variable );
    exp.variable.accept(this, level);
  }


 //new ones

  public void visit( WhileExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "WhileExp: \n" );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.test != null)
      exp.test.accept( this, level );
    if(exp.body != null)
      exp.body.accept( this, level ); 
  }

  public void visit( ArrayDec exp, int level ) {
    indent( level );
    try {
      myWriter.write( "ArrayDec: " + exp.name + " " + exp.size + "\n");
    
    } catch (IOException e)
    {
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.typ != null)
      exp.typ.accept( this, level );
  }
  
  public void visit(BoolExp exp, int level){
    indent(level);
    try {
      myWriter.write( "BoolExp: " + exp.value + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }

  public void visit(CallExp exp, int level){
    indent(level);
    try {
      myWriter.write( "CallExp: " + exp.func + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.args != null)
      exp.args.accept(this, level);
  }

  public void visit( CompoundExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "CompoundExp: \n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.decs != null)
      exp.decs.accept( this, level );
    if(exp.exps != null)
      exp.exps.accept( this, level );
  }

  public void visit( DecList expList, int level ) {
    while( expList != null ) {
      if(expList.head != null){
        expList.head.accept( this, level );
        expList = expList.tail;
      }
    } 
  }

  public void visit( FunctionDec exp, int level ) {
    indent( level );
    try {
      myWriter.write( "FunctionDec: " + exp.func + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.result != null)
      exp.result.accept( this, level );
    if(exp.params != null)
      exp.params.accept( this, level );
    if(exp.body != null)
      exp.body.accept( this, level );
  }

  public void visit( IndexVar exp, int level ) {
    indent( level );
    try {
      myWriter.write( "IndexVar: " + exp.name + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.index != null)
      exp.index.accept( this, level );
  }
  
  public void visit( NameTy exp, int level ) {
    indent( level );
    try{
      if(exp.typ == 0)
        myWriter.write( "NameTy: BOOL \n");
      else if(exp.typ == 1)
        myWriter.write( "NameTy: INT \n");
      else if(exp.typ == 2)
        myWriter.write( "NameTy: VOID \n");
      else
        myWriter.write( "NameTy: INVALID_NAMETYPE_ERROR \n");
        //System.err.println( "Invalid Type error at " + exp.row + "," +exp.col );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }

  public void visit( NilExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "NilExp: \n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }
  
  public void visit( SimpleDec exp, int level ) {
    indent( level );
    try {
      myWriter.write( "SimpleDec: " + exp.name + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if(exp.typ != null)
      exp.typ.accept( this, level );
  }

  public void visit( SimpleVar exp, int level ) {
    indent( level );
    try {
      myWriter.write( "SimpleVar: " + exp.name + "\n");
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
  }

  public void visit( VarDecList expList, int level ) {
    while( expList != null ) {
      if(expList.head != null){
        expList.head.accept( this, level );
        expList = expList.tail;
      }
    } 
  }

  public void visit( ReturnExp exp, int level ) {
    indent( level );
    try {
      myWriter.write( "ReturnExp: \n" );
    
} catch (IOException e)
{
      System.err.println("FileWrite Error");
    }
    level++;
    if (exp.exp != null){
      exp.exp.accept( this, level );
    }
  }
}
