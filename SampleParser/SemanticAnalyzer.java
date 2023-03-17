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

    public void visit( ExpList expList, int level ) {
        while( expList != null ) {
            if(expList.head != null){
                expList.head.accept( this, level );
                expList = expList.tail;
            }
        } 
    }

    public void visit( AssignExp exp, int level ) {
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
                    
                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                        //declarations.remove(key);
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

                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                        //declarations.remove(key);
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
        //myWriter.write( "size: " + exp.size + "\n");
        if(!(declarations.containsKey(exp.name))){
            ArrayList<NodeType> al = new ArrayList<NodeType>();
            al.add(0, new NodeType(exp.name, exp, level));
            declarations.put(exp.name, al);
        }
        else{
            declarations.get(exp.name).add(0, new NodeType(exp.name, exp, level));
        }
        //level -= 1;
        
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
        //level++;    
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
                    
                    if(declarations.get(key).size() == 1){
                        toRemove.add(key);
                        //declarations.remove(key);
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

        if(declarations.get(exp.func) == null)
        {
            ArrayList<NodeType> al = new ArrayList<>();
            al.add(0, new NodeType(exp.func, exp, level));
            declarations.put(exp.func, al);            
        }
        else
        {
            System.err.println("Nested function error, shouldn't get here");
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
                    System.out.println("key = " + key);
                    if(declarations.get(key).get(0).level == level)
                    {
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
                            System.err.println("ERROR, NO NESTED FUNCTIONS ALLOWED");
                        }
                        
                        if(declarations.get(key).size() == 1){
                            toRemove.add(key);
                            //declarations.remove(key);
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
        level++;
        if(exp.index != null)
            exp.index.accept( this, level );
    }
    
    public void visit( NameTy exp, int level ) {
    }

    public void visit( NilExp exp, int level ) {
    }
    
    public void visit( SimpleDec exp, int level ) {
        //try {

            if(!(declarations.containsKey(exp.name)))
            {
                ArrayList<NodeType> al = new ArrayList<>();
                al.add(0, new NodeType(exp.name, exp, level));
                declarations.put(exp.name, al);
                //indent(level);

                //SimpleDec jane = (SimpleDec) declarations.get(exp.name).get(0).def;
                //myWriter.write(exp.name + " x" + declarations.get(exp.name).size() + ", l: " + level + ", t: " + jane.typ.getType() + "\n");
                //myWriter.write(exp.name + " x" + declarations.get(exp.name).size() + ", l: " + level + ", t: " +  + "\n");
            }
            else
            {
                declarations.get(exp.name).add(0, new NodeType(exp.name, exp, level));
                //indent(level);
                //SimpleDec jane = (SimpleDec) declarations.get(exp.name).get(0).def;
                //myWriter.write(exp.name + " x" + declarations.get(exp.name).size() + ", l: " + level + ", t: " + jane.typ.getType() + "\n");
            }
        
        /*} catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }*/
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