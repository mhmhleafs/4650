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
  static public void main(String argv[]) {    
    for (String s: argv)
    {
      //System.out.println(s);
      if(s.equals("-a"))
      {
        //System.out.println("  valid");
        AFLAG = true;
      }
    }
    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);      
      if (SHOW_TREE && result != null && AFLAG) {
         System.out.println("The abstract syntax tree is:");
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
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}


