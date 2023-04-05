/*
  Created by: Fei Song
  File Name: Main.java
  To Build: 
  After the Scanner.java, tiny.flex, and tiny.cup have been processed, do:
    javac Main.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/
   
import java.io.*;
import absyn.*;
   
class Main {
  public final static boolean SHOW_TREE = true;
  public static boolean AFLAG = false;
  public static boolean SFLAG = false;
  public static boolean CFLAG = false;
  static public void main(String argv[]) {    
    for (String s: argv)
    {
      //System.out.println(s);
      if(s.equals("-a"))
      {
        //System.out.println("  valid");
        AFLAG = true;
      }
      if(s.equals("-s"))
      {
        //System.out.println("  valid");
        AFLAG = true;
        SFLAG = true;
      }
      if(s.equals("-c"))
      {
        //System.out.println("  valid");
        AFLAG = true;
        SFLAG = true;
        CFLAG = true;
      }
    }
    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);      
      if (SHOW_TREE && result != null && AFLAG) {
         System.out.println("\nThe abstract syntax tree is:");
         ShowTreeVisitor visitor = new ShowTreeVisitor();
         String filename = argv[0].replace(".cm", ".abs");
         System.out.println(filename);
         File outFile = new File(filename);
         FileWriter temp = new FileWriter(filename);
         visitor.myWriter = temp;
         result.accept(visitor, 0);
         visitor.myWriter.close();
         temp.close();
      }
      if (SHOW_TREE && result != null && SFLAG)
      {
        System.out.println("\nThe symbol tree is:");
        
        SemanticAnalyzer visitor = new SemanticAnalyzer();
        
        String filename = argv[0].replace(".cm", ".sym");
        System.out.println(filename);
        
        File outFile = new File(filename); //creates xx.sym
        FileWriter temp = new FileWriter(filename); //creates a file writer
        
        visitor.myWriter = temp;
        result.accept(visitor, 0);
        visitor.myWriter.close();
        
        temp.close();
      }
      if(SHOW_TREE && result != null && CFLAG)
      {
        String filename = argv[0].replace(".cm", ".tm");
        System.out.println("\nTM Code is in: ");
        System.out.println(filename);
        
        File outFile = new File(filename); //creates xx.sym
        FileWriter temp = new FileWriter(filename); //creates a file writer
        
        temp.write(" * Standard prelude:\n");
        temp.write(" 0:     LD  6,0(0) 	load gp with maxaddress\n");
        temp.write(" 1:    LDA  5,0(6) 	copy to gp to fp\n");
        temp.write(" 2:     ST  0,0(0) 	clear location 0\n");
        temp.write(" * Jump around i/o routines here\n");
        temp.write(" * code for input routine\n");
        temp.write(" 4:     ST  0,-1(5) 	store return\n");
        temp.write(" 5:     IN  0,0,0 	input\n");
        temp.write(" 6:     LD  7,-1(5) 	return to caller\n");
        temp.write(" * code for output routine\n");
        temp.write(" 7:     ST  0,-1(5) 	store return\n");
        temp.write(" 8:     LD  0,-2(5) 	load output value\n");
        temp.write(" 9:    OUT  0,0,0 	output\n");
        temp.write(" 10:     LD  7,-1(5) 	return to caller\n");
        temp.write(" 3:    LDA  7,7(7) 	jump around i/o code\n");
        temp.write(" * End of standard prelude.\n");
        temp.write(" * End of execution.\n");
        temp.write(" 11:   HALT  0,0,0 	\n");
        
        temp.close();
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}


