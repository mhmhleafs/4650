package absyn;

public class ArrayDec extends Dec {
  public NameTy typ;
  public String name;
  public int size;

  public ArrayDec( int row, int col, NameTy typ, String name, int size) {
    this.row = row;
    this.col = col;
    this.typ = typ;
    this.name = name;
    this.size = size;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}