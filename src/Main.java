
import Expr.ExprLexer;
import Expr.ExprParser;
import Objets.Offsets;
import Objets.TableDesSymboles;
import Objets.Tables;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static Expr.TreeParser.analyseRec;

public class Main {
    public static void main(String[] args) throws Exception {

        String file = "ExemplesRust/ex4.rs";

        System.out.println("File to load: " + file);

        try {
            InputStream stream = new FileInputStream(file);
            ANTLRInputStream input = new ANTLRInputStream(stream);

            ExprLexer lexer = new ExprLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ExprParser parser = new ExprParser(tokens);

            ExprParser.fichier_return result = parser.fichier();
            CommonTree t = (CommonTree) result.getTree();

            Tables tables = new Tables();
            Offsets offsets = new Offsets();
            TableDesSymboles tds = new TableDesSymboles(tables);

            analyseRec(tables, t, tds, offsets);

            System.out.print(tables.toString());

            offsets.setDeplacementsGlobaux();
            System.out.print(offsets.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}