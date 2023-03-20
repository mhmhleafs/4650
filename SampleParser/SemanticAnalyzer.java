import absyn.*;
import java.io.*;
import java.util.*;

public class SemanticAnalyzer implements AbsynVisitor{
    final static int SPACES = 4;
    FileWriter myWriter;
    HashMap<String, ArrayList<NodeType>> declarations = new HashMap<String, ArrayList<NodeType>>(10);

    private void indent( int level ) {
        try{
        for( int i = 0; i < level * SPACES; i++ ) myWriter.write( " " );
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
    }

    public void printDec(int level, String key){
        try{
            if(declarations.get(key).get(0).def instanceof SimpleDec)
            {
                SimpleDec simpleNode = (SimpleDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(simpleNode.name + " x" + declarations.get(key).size() + ", l: " + level + ", t: " + simpleNode.typ.getType() + "\n");
            }
            else if(declarations.get(key).get(0).def instanceof ArrayDec)
            {
                ArrayDec arrayNode = (ArrayDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(arrayNode.name + " x" + declarations.get(key).size() + ", l: " + level + ", t: " + arrayNode.typ.getType() + "\n");
            }
            else if(declarations.get(key).get(0).def instanceof FunctionDec)
            {
                FunctionDec functionNode = (FunctionDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(functionNode.func + " x" + declarations.get(key).size() + ", l: " + level + ", t: " + functionNode.result.getType() + "\n");
            }
        } catch (IOException e){
            System.err.println("FileWrite error");
        }
    }

    public int expType(Exp exp)
    {
        if (exp instanceof NilExp)
        {
            return -1;
        }
        if (exp instanceof IntExp)
        {
            return type((IntExp) exp);
        }
        if (exp instanceof BoolExp)
        {
            return type((BoolExp) exp);
        }
        if (exp instanceof VarExp)
        {
            return type((VarExp) exp);
        } 
        if(exp instanceof CallExp)
        {
            return type((CallExp) exp);
        }
        if (exp instanceof OpExp)
        {
            return type((OpExp) exp);
        }
        if(exp instanceof AssignExp)
        {
            return type((AssignExp) exp);
        }

        return -1;
    }

    public int type(IntExp exp)
    {
        return NameTy.INT;
    }

    public int type(BoolExp exp){
        return NameTy.BOOL;
    }

    public int type(CallExp exp)
    {
        FunctionDec temp = (FunctionDec) declarations.get(exp.func).get(0).def;
        return temp.result.typ;
    }

    public int type(VarExp exp)
    {
        if(exp.variable instanceof SimpleVar)
        {
            SimpleVar temp = (SimpleVar) exp.variable;
            if(declarations.containsKey(temp.name))
            {
                SimpleDec tempDec = (SimpleDec) declarations.get(temp.name).get(0).def;
                return tempDec.typ.typ;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            IndexVar temp = (IndexVar) exp.variable;
            if (declarations.containsKey(temp.name))
            {
                ArrayDec tempDec = (ArrayDec) declarations.get(temp.name).get(0).def;
                return tempDec.typ.typ;
            }

            return -1;

        }
    }

    public int type(AssignExp exp)
    {
        return type(exp.lhs);
    }

    public int type(OpExp exp)
    {
        if(exp.left instanceof NilExp)
        {
            return expType(exp.right);
        }
        else
        {
            return expType(exp.left);
        }
    }


    /////////////////////////////////////////////////////////
    public void visit( ExpList expList, int level ) {
        while( expList != null ) {
            if(expList.head != null){
                expList.head.accept( this, level );
                expList = expList.tail;
            }
        } 
    }

    public void visit( AssignExp exp, int level ) {
        int lhsType = type(exp.lhs);
        int rhsType = expType(exp.rhs);
        
        if(lhsType != rhsType)
        {
            System.err.println("Line " + (exp.row+1) +": Assignment type error. Cannot assign type " + rhsType + " to type " + lhsType);
        }

        if(exp.lhs != null)
            exp.lhs.accept( this, level );
        if(exp.rhs != null)
            exp.rhs.accept( this, level );
    }

    public void visit( IfExp exp, int level ) {
        indent( level );
        try {
            myWriter.write( "Entering the scope for IF block: \n" );
        
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

        try {

            List<String> toRemove = new ArrayList<>();
            
            for (String key : declarations.keySet()) {
                
                if(declarations.get(key).get(0).level == level)
                {
                    printDec(level, key);
                    
                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                    }
                    else{
                        declarations.get(key).remove(0);
                    }
                }
            }

            declarations.keySet().removeAll(toRemove);
            
            indent(level-1);
            myWriter.write( "Leaving the scope for IF block.\n" );
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
    }

    public void visit( IntExp exp, int level ) {
    }

    public void visit( OpExp exp, int level ) {   
        System.out.println("DO I/O THING\n");
        int lhsType = expType(exp.left);
        int rhsType = expType(exp.right);

        if(lhsType != rhsType && lhsType != -1)
        {
            System.err.println("Line " + (exp.row+1) + ": Operation expression error. Cannot operate on " + rhsType + " with " + lhsType);
        }

        if(lhsType == rhsType)
        {
            if(exp.op <= 10 && lhsType != NameTy.INT)//int operations
            {
                System.err.println("Line " + (exp.row+1) + ": Operation expression error. Int type expected for this operation.");
            }
            else if(exp.op > 10 && lhsType != NameTy.BOOL){
                System.err.println("Line " + (exp.row+1) + ": Operation expression error. Bool type expected for this operation.");
            }
        }
        else if(lhsType == -1)
        {
            if(exp.op != OpExp.UMINUS && exp.op != OpExp.NOT)
            {
                System.err.println("Line " + (exp.row + 1) + ": Operation expression error. Unexpected NilExp on left side");
            }
        }

        if (exp.left != null)
            exp.left.accept( this, level );
        if(exp.right != null)
            exp.right.accept( this, level );
    }

    public void visit( VarExp exp, int level ) {
        exp.variable.accept(this, level);
    }

    public void visit( WhileExp exp, int level ) {
        indent( level );
        try {
            myWriter.write( "Entering scope for WHILE block: \n" );
            
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
        level++;
        if(exp.test != null)
            exp.test.accept( this, level );
        if(exp.body != null)
            exp.body.accept( this, level );

        try {            
            List<String> toRemove = new ArrayList<>();
            for (String key : declarations.keySet()) {
                
                if(declarations.get(key).get(0).level == level)
                {
                    printDec(level, key);

                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                    }
                    else{
                        declarations.get(key).remove(0);
                    }
                }
            }
            declarations.keySet().removeAll(toRemove);

            myWriter.write( "Leaving scope for WHILE block.\n" );
            
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
    }

    public void visit( ArrayDec exp, int level ) {
        if(!(declarations.containsKey(exp.name))){
            ArrayList<NodeType> al = new ArrayList<NodeType>();
            al.add(0, new NodeType(exp.name, exp, level));
            declarations.put(exp.name, al);
        }
        else{
            if(declarations.get(exp.name).get(0).level == level)
            {
                System.err.println("Line " + (exp.row+1) + ": Redefinition error, " + exp.name + " already defined in this scope");
            }
            else
            {
                declarations.get(exp.name).add(0, new NodeType(exp.name, exp, level));
            }
        }
        
        if(exp.typ != null)
            exp.typ.accept( this, level );
    }
    
    public void visit(BoolExp exp, int level){
        
    }

    public void visit(CallExp exp, int level){
        
        if(exp.args != null)
            exp.args.accept(this, level);
    }

    public void visit( CompoundExp exp, int level ) {      
        if(exp.decs != null)
            exp.decs.accept( this, level );
        if(exp.exps != null)
            exp.exps.accept( this, level );
    }

    public void visit( DecList expList, int level ) {
        try {
            myWriter.write("Entering the global scope:\n");
        } catch (IOException e)
        {
            System.err.println("Filewrite Error");
        }
        level++;
        while( expList != null ) {
            if(expList.head != null){
                expList.head.accept( this, level );
                expList = expList.tail;
            }
        } 

        try {

            List<String> toRemove = new ArrayList<>();

            for (String key : declarations.keySet()) {

                if(declarations.get(key).get(0).level == level)
                {
                    printDec(level, key);
                    
                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                    }
                    else{
                        declarations.get(key).remove(0);
                    }
                }
            }

            declarations.keySet().removeAll(toRemove);

            indent(level-1);
            myWriter.write("Leaving the global scope\n");
        } catch (IOException e)
        {
            System.err.println("Filewrite Error");
        }
    }

    public void visit( FunctionDec exp, int level ) {
        indent( level );
        try {
            if(exp.body != null){
                myWriter.write( "Entering the scope for function " + exp.func + ":\n");
            }
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }

        if(!(declarations.containsKey(exp.func))){
            ArrayList<NodeType> al = new ArrayList<NodeType>();
            al.add(0, new NodeType(exp.func, exp, level));
            declarations.put(exp.func, al);
        }
        else{
            if(declarations.get(exp.func).get(0).level == level)
            {
                System.err.println("Redefinition error, " + exp.func + " already defined in this scope");
            }
            else
            {
                declarations.get(exp.func).add(0, new NodeType(exp.func, exp, level));
            }
        }

        level++;
        if(exp.result != null)
            exp.result.accept( this, level );
        if(exp.params != null)
            exp.params.accept( this, level );
        if(exp.body != null)
            exp.body.accept( this, level );

        try {
            if(exp.body != null){
                List<String> toRemove = new ArrayList<>();

                for (String key : declarations.keySet()) {
                    if(declarations.get(key).get(0).level == level)
                    {
                        printDec(level, key);
                        
                        if(declarations.get(key).size() == 1){
                            toRemove.add(key);
                        }
                        else{
                            declarations.get(key).remove(0);
                        }
                    }
                }

                declarations.keySet().removeAll(toRemove);

                indent(level-1);
                myWriter.write( "Leaving the function scope.\n");
            }
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
    }

    public void visit( IndexVar exp, int level ) {

        if(declarations.containsKey(exp.name))
        {
            if(declarations.get(exp.name).get(0).def instanceof ArrayDec)
            {
                if(expType(exp.index) != NameTy.INT)
                {
                    System.err.println("Line " + (exp.row + 1) + ": Array index is type " + expType(exp.index) + ". Expected INT");
                }
            }
        }
        else
        {
            System.err.println("Line " + (exp.row + 1) + ": Undefined array error. " + exp.name + " does not exist");
        }

        if(exp.index != null)
            exp.index.accept( this, level );
    }
    
    public void visit( NameTy exp, int level ) {
    }

    public void visit( NilExp exp, int level ) {
    }
    
    public void visit( SimpleDec exp, int level ) {

        if(!(declarations.containsKey(exp.name))){
            ArrayList<NodeType> al = new ArrayList<NodeType>();
            al.add(0, new NodeType(exp.name, exp, level));
            declarations.put(exp.name, al);
        }
        else{
            if(declarations.get(exp.name).get(0).level == level)
            {
                System.err.println("Line " + (exp.row + 1) + ": Redefinition error, " + exp.name + " already defined in this scope");
            }
            else
            {
                declarations.get(exp.name).add(0, new NodeType(exp.name, exp, level));
            }
        }
        level++;
        if(exp.typ != null)
            exp.typ.accept( this, level );
    }

    public void visit( SimpleVar exp, int level ) {
        
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
        if (exp.exp != null){
            exp.exp.accept( this, level );
        }
    }
}