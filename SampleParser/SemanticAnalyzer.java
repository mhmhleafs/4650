import absyn.*;
import java.io.*;
import java.util.*;


public class SemanticAnalyzer implements AbsynVisitor{
    final static int SPACES = 4;
    FileWriter myWriter;
    HashMap<String, ArrayList<NodeType>> declarations = new HashMap<String, ArrayList<NodeType>>(10);

    ArrayList<String> source = new ArrayList<>();
    HashSet<String> prototypes = new HashSet<>();

    ArrayList<FunctionDec> functionDefs = new ArrayList<>();
    ArrayList<String> toReturn = new ArrayList<>();

    //add input and output functions
    VarDecList inputParams = null;
    FunctionDec inputDec = new FunctionDec(-1, -1, new NameTy(-1, -1, NameTy.INT), "input", inputParams, new NilExp(-1, -1));
    NodeType inputNode = new NodeType("input", inputDec, -1);
    ArrayList<NodeType> inputArray = new ArrayList<>();

    VarDecList outputParams = new VarDecList(new SimpleDec(-1, -1, new NameTy(-1, -1, NameTy.INT), "output"), null);
    FunctionDec outputDec = new FunctionDec(-1, -1, new NameTy(-1, -1, NameTy.VOID), "output", outputParams, new NilExp(-1, -1));
    NodeType outputNode = new NodeType("output", outputDec, -1);
    ArrayList<NodeType> outputArray = new ArrayList<>();


    private void indent( int level ) {
        try{
        for( int i = 0; i < level * SPACES; i++ ) myWriter.write( " " );
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
    }

    public String stringifyType(int typ)
    {
        if(typ == 0)
            return "BOOL";
        else if(typ == 1)
            return "INT";
        else if(typ == 2)
            return "VOID";
        else if(typ == 3)
            return "ERROR";
        return "ERROR";
    }

    public String printParams(VarDecList expList){
        String paramsList = "(";
        while( expList != null ) {
            if(expList.head != null){
                if(expList.head instanceof SimpleDec){
                    paramsList += (((SimpleDec) expList.head).typ.getType());
                    paramsList += ", ";
                }
                else if(expList.head instanceof ArrayDec){
                    paramsList += (((ArrayDec) expList.head).typ.getType());
                    paramsList += ", ";
                }
                expList = expList.tail;
            }
        } 
        paramsList += ")";
        return paramsList;
    }

    public void printDec(int level, String key){
        try{
            if(declarations.get(key).get(0).def instanceof SimpleDec)
            {
                SimpleDec simpleNode = (SimpleDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(simpleNode.name + " : " + simpleNode.typ.getType() + "\n");
            }
            else if(declarations.get(key).get(0).def instanceof ArrayDec)
            {
                ArrayDec arrayNode = (ArrayDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(arrayNode.name + " : " + arrayNode.typ.getType() + "\n");
                //myWriter.write(arrayNode.name + " x" + declarations.get(key).size() + ", l: " + level + ", t: " + arrayNode.typ.getType() + "\n");
            }
            else if(declarations.get(key).get(0).def instanceof FunctionDec)
            {
                FunctionDec functionNode = (FunctionDec) declarations.get(key).get(0).def;
                indent(level);
                myWriter.write(functionNode.func + " : " + printParams(functionNode.params) + " -> " + functionNode.result.getType() + "\n");
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
        if(!(declarations.containsKey(exp.func)))
        {
            return -1;
        }
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
                if(declarations.get(temp.name).get(0).def instanceof ArrayDec)
                {
                    ArrayDec tempDec = (ArrayDec) declarations.get(temp.name).get(0).def;
                    return tempDec.typ.typ;
                }
                else
                {
                    SimpleDec tempDec = (SimpleDec) declarations.get(temp.name).get(0).def;
                    return tempDec.typ.typ;
                }
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
            System.err.println("Line " + (exp.row+1) +": Assignment type error. Cannot assign type " + stringifyType(rhsType) + " to type " + stringifyType(lhsType));
        }

        if(exp.lhs != null)
            exp.lhs.accept( this, level );
        if(exp.rhs != null)
            exp.rhs.accept( this, level );
    }

    public void visit( IfExp exp, int level ) {
        source.add(0, "if");
        
        if(exp.test != null)
            exp.test.accept( this, level );
        if(exp.thenpart != null)
            exp.thenpart.accept( this, level );
        if (exp.elsepart != null )
            exp.elsepart.accept( this, level );

        source.remove(0);
    }

    public void visit( IntExp exp, int level ) {
    }

    public void visit( OpExp exp, int level ) {   
        int lhsType = expType(exp.left);
        int rhsType = expType(exp.right);

        if(lhsType != rhsType && lhsType != -1)
        {
            System.err.println("Line " + (exp.row+1) + ": Operation expression error. Cannot operate on " + stringifyType(rhsType) + " with " + stringifyType(lhsType));
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
            else if(exp.op == OpExp.UMINUS && rhsType != 1){
                System.err.println("Line " + (exp.row+1) + ": Operation expression error. Int type expected for this operation.");
            }
            else if(exp.op == OpExp.NOT && rhsType != 0){
                System.err.println("Line " + (exp.row+1) + ": Operation expression error. Bool type expected for this operation.");
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
        source.add(0, "while");
        
        if(exp.test != null)
            exp.test.accept( this, level );
        if(exp.body != null)
            exp.body.accept( this, level );

        source.remove(0);
    }

    public void visit( ArrayDec exp, int level ) {

        if(exp.typ.typ == 2)
        {
            exp.typ.typ = 1;
        }

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
        if(!(declarations.containsKey(exp.func))){
            System.err.println("Line " + (exp.row+1) + ": Invalid attempt to call undefined function \"" + exp.func + "\".");
        }
        else if(!(declarations.get(exp.func).get(0).def instanceof FunctionDec)){
            System.err.println("Line " + (exp.row+1) + ": Invalid attempt to call non-function \"" + exp.func + "\".");
        }
        else{
            FunctionDec temp = (FunctionDec) declarations.get(exp.func).get(0).def;
            VarDecList vlist = temp.params;
            ExpList elist = exp.args;

            while( vlist != null && elist != null) 
            {
                if(vlist.head != null && elist.head != null){
                    
                    if(vlist.head instanceof SimpleDec) 
                    {
                        if(((SimpleDec) vlist.head).typ.typ != expType(elist.head))
                        {
                            System.err.println("Line " + (exp.row+1) + ": Invalid argument type(s) for call to function \"" + exp.func + "\".");
        
                            break;
                        }
                    }
                    else if(vlist.head instanceof ArrayDec) 
                    {
                        if(((ArrayDec) vlist.head).typ.typ != expType(elist.head))
                        {
                            System.err.println("Line " + (exp.row+1) + ": Invalid argument type(s) for call to function \"" + exp.func + "\".");
        
                            break;
                        }
                    }

                    vlist = vlist.tail;
                    elist = elist.tail;
                }
            } 

            if(elist == null && vlist != null)
            {
                System.err.println("Line " + (exp.row+1) + ": Too few arguments for call to function \"" + exp.func + "\".");
            }
            else if(vlist == null && elist != null)
            {
                System.err.println("Line " + (exp.row+1) + ": Too many arguments for call to function \"" + exp.func + "\".");
            }
        }
        
        if(exp.args != null)
            exp.args.accept(this, level);
    }

    public void visit( CompoundExp exp, int level ) {   

        if(source.size() != level)
        {
            source.add(0, "COMPOUND");
        }

        indent( level );
        try {
            myWriter.write( "Entering scope for block " + source.get(0) + " (level " + level + "): \n" );
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }
        level++;
        
        if(exp.decs != null)
            exp.decs.accept( this, level );
        if(exp.exps != null)
            exp.exps.accept( this, level );



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
            myWriter.write( "Leaving the scope for block " + source.get(0) + "\n");
        
        } catch (IOException e)
        {
            System.err.println("FileWrite Error");
        }

        if(source.get(0) == "COMPOUND")
        {
            source.remove(0);
        }
    }

    public void visit( DecList expList, int level ) {

        if(level == 0)
        {
            inputArray.add(inputNode);
            outputArray.add(outputNode);
            declarations.put("input", inputArray);
            declarations.put("output", outputArray);
        }

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

            if(!(prototypes.isEmpty()))
            {
                for(String s : prototypes)
                {
                    System.err.println("Error: Function prototype left undefined for function " + s + ".");
                }
            }

            if(!(toReturn.isEmpty()))
            {
                for(String s : toReturn)
                {
                    System.err.println("Error: Function " + s + " ends with no return statement.");
                }
            }

            FunctionDec lastFunc = functionDefs.get(0);
            if(lastFunc.func.compareTo("main") != 0)
            {
                //err missing main
                System.err.println("Error: expected \"main\" to be last function");
            }
            else if(lastFunc.params != null || lastFunc.result.typ != 2)
            {
                System.err.println("Error: main must be \"void main(void)\".");
            }


            myWriter.write("Leaving the global scope\n");
        } catch (IOException e)
        {
            System.err.println("Filewrite Error");
        }
    }

    public void visit( FunctionDec exp, int level ) {
        source.add(0, exp.func);
        
        functionDefs.add(0, exp);//add f'n to list to ensure main is last

        if(exp.body instanceof NilExp)//if the function declaration is a prototype
        {
            prototypes.add(exp.func);
        }
        else if(prototypes.contains(exp.func)){
            FunctionDec temp = (FunctionDec) declarations.get(exp.func).get(0).def;
            VarDecList vlist = temp.params;
            VarDecList elist = exp.params;

            while( vlist != null && elist != null) 
            {
                if(vlist.head != null && elist.head != null){
                    
                    if(vlist.head instanceof SimpleDec) 
                    {
                        vlist.head = (SimpleDec) vlist.head;
                        if(elist.head instanceof SimpleDec)
                        {
                            if((((SimpleDec) vlist.head).typ.typ != ((SimpleDec) elist.head).typ.typ)){
                                System.err.println("Line " + (exp.row+1) + ": Mismatched parameter types in definition of function \"" + exp.func + "\".");
                                break;
                            }
                        }
                        else
                        {
                            System.err.println("Line " + (exp.row+1) + ": Unexpected array in parameter definition of function \"" + exp.func + "\".");
        
                            break;
                        }
                    }
                    else if(vlist.head instanceof ArrayDec) 
                    {
                        if(elist.head instanceof ArrayDec)
                        {
                            if((((ArrayDec) vlist.head).typ.typ != ((ArrayDec) elist.head).typ.typ)){
                                System.err.println("Line " + (exp.row+1) + ": Mismatched parameter types in definition of function \"" + exp.func + "\".");
                                break;
                            }
                        }
                        else
                        {
                            System.err.println("Line " + (exp.row+1) + ": Mismatched parameter types in definition of function \"" + exp.func + "\".");
        
                            break;
                        }
                    }

                    vlist = vlist.tail;
                    elist = elist.tail;
                }
            } 

            if(elist == null && vlist != null)
            {
                System.err.println("Line " + (exp.row+1) + ": Too few arguments for definition of function \"" + exp.func + "\".");
            }
            else if(vlist == null && elist != null)
            {
                System.err.println("Line " + (exp.row+1) + ": Too many arguments for definition of function \"" + exp.func + "\".");
            }
            prototypes.remove(exp.func);
            declarations.remove(exp.func);
        }

        if(!(declarations.containsKey(exp.func))){
            ArrayList<NodeType> al = new ArrayList<NodeType>();
            al.add(0, new NodeType(exp.func, exp, level));
            declarations.put(exp.func, al);
            if(!(toReturn.contains(exp.func)) && exp.result.typ != 2){
                toReturn.add(0, exp.func);
            }
        }
        else
        {
            if(declarations.get(exp.func).get(0).level == level)
            {
                System.err.println("Line " + (exp.row+1) + ": Redefinition error, " + exp.func + " already defined in this scope");
            }
            else
            {
                declarations.get(exp.func).add(0, new NodeType(exp.func, exp, level));
            }
            if(!(toReturn.contains(exp.func)) && exp.result.typ != 2)
            {
                toReturn.add(0, exp.func);
            }
        }

        //level++;
        if(exp.result != null)
            exp.result.accept( this, level );
        if(exp.params != null)
            exp.params.accept( this, level + 1);
        if(exp.body != null)
            exp.body.accept( this, level );

        source.remove(0);
    }

    public void visit( IndexVar exp, int level ) {

        if(declarations.containsKey(exp.name))
        {
            if(declarations.get(exp.name).get(0).def instanceof ArrayDec)
            {
                if(expType(exp.index) != NameTy.INT)
                {
                    System.err.println("Line " + (exp.row + 1) + ": Array index is type " + stringifyType(expType(exp.index)) + ". Expected INT");
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

        if(exp.typ.typ == 2)
        {
            exp.typ.typ = 1;
        }

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

        int i = 0;
        String x = source.get(0);
        while(x.equals("if") || x.equals("while") || x.equals("COMPOUND"))
        {
            i++;

            if(i >= source.size())
            {
                break;
            }

            x = source.get(i);
        }

        if(i >= source.size())//return in global scope
        {
            System.err.println("Line " + (exp.row+1) + ": Invalid Return expression in global scope");
        }
        else
        {
            FunctionDec temp = (FunctionDec) declarations.get(x).get(0).def;
            
            if(expType(exp.exp) != temp.result.typ)
            {
                if(!(temp.result.typ == 2 && expType(exp.exp) == -1)){//if not void and nilexp (e.g. "return;" in a void f'n)
                    System.err.println("Line " + (exp.row+1) + ": Invalid return type in function " + temp.func );
                }
                else{
                    toReturn.remove(0);
                }
            }
            else{
                toReturn.remove(0);
            }
        }


        if (exp.exp != null){
            exp.exp.accept( this, level );
        }
    }
}