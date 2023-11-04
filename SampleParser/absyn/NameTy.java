package absyn;

public class NameTy extends Absyn {
    public final static int BOOL = 0;
    public final static int INT = 1;
    public final static int VOID = 2;
    public final static int ERROR = 3;

    public int typ;

    public NameTy(int row, int col, int typ){
        this.row = row;
        this.col = col;
        this.typ = typ;
    }

    public String getType()
    {
        if(this.typ == 0)
            return "BOOL";
        else if(this.typ == 1)
            return "INT";
        else if(this.typ == 2)
            return "VOID";
        else if(this.typ == 3)
            return "ERROR";
        return "ERROR";
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }
}