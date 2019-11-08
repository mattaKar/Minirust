package Assembleur;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import Exceptions.NonExistantFunction;
import Expr.TreeParser;
import Objets.TableDesSymboles;
import Objets.Offsets;
import Objets.Tables;

public class Code_gen {
	//le code micropiup que nous allons ecrire tout du long et ajouter a notre fichier excecutable
	private static String code ="";
	
	//le fichier dans lequel on stockera le code
	private String filename;
	
	// objets pour ecrire dans le fichier
	private File fichier = null;
	private static FileWriter out = null;
	
	// Tables neccessaire a "l'intérpretation" de l'assembleur
	private Tables listTDS;
	public static TableDesSymboles TDScourante;
	public static Offsets offsets;
	
	// Indice d'evolution pour les boucle for/while...
	public static int iLoop = 0;
	public static int iBoucle = 0;
	
	
	// une liste des registre couramment utiliser pour le suivi
	private static ArrayList<Integer> Registerlist;
	
	public Code_gen(String filename, Tables t)
	{
		this.listTDS = t;
		Code_gen.offsets = new Offsets();
		Code_gen.TDScourante = (TableDesSymboles) this.listTDS.get(0);
		
		Registerlist = new ArrayList<Integer>();
		for (int j = 0 ; j <= 10 ; j++ ){
			Registerlist.add(j);
		}
		
		String[] split = filename.split("\\.");
		this.filename = split[0] + ".src";
		this.fileDeleter();
		this.fichier = new File(this.filename);
		try {
			Code_gen.out = new FileWriter(this.fichier, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public void gen_runner(CommonTree t, TableDesSymboles tds) throws IOException {
			this.initialize();
			this.parcoursTree(t, tds);
			Code_gen.out.close();
	}
	
	private void initialize()
	{
		
		code =  "EXIT_EXC 	EQU 64\n"+  	//num d'exception de EXIT
				"READ_EXC 	EQU 65\n"+  	// num d'exception de READ
				"WRITE_EXC 	EQU 66\n"+		// num d'exception de WRITE
				"STACK_ADRS EQU 0x1000\n" + //base de la pile en 1000h
				"LOAD_ADRS 	EQU 0xF000\n"+  //adresse de chargement de l'executable
				"INT_SIZE EQU 4\n"+ 		// chaque entier prend 4 case mémoire soit 32 bits
				"NIL EQU 0\n"+				// fin de liste: contenu initial de BP
				"SP		EQU R15\n"+			// alias pour R15: Stack Pointer (pointeur de pile)
				"WR		EQU R14\n"+			// alias pour R14: Work Register (reg. de travail)
				"BP 	EQU R13\n" +		//alias pour R13: frame Base Pointer (point. envir.)
				"ORG LOAD_ADRS\n"+			// adresse de chargement
<<<<<<< HEAD
				"START main_\n\n\n"; 		// adresse de démarrage 
		         
=======
				"START main\n\n\n";  		// adresse de démarrage 
>>>>>>> 129a6a42192b795f507666190edd999788311da1
		
				/*
				"main LDW SP,#STACK_ADRS\n "+
				"LDW BP, #NIL\n" +
				"STW BP,-(SP)\n" +
				"LDW BP,SP\n";
				*/

		writeTxt(code);
	}
	
	private void parcoursTree(CommonTree t, TableDesSymboles tds) {
		if (t.isNil()) {
			int nbChilds = t.getChildCount();
			for (int i = 0; i < nbChilds; i++)
				parcoursTree((CommonTree) t.getChild(i),tds);
		
		} else {
			int r1;
			
			switch (t.getText()) {
			
			case "DECL": 
				r1=getVarAdressRegister((CommonTree) t.getChild(0), tds);
				int r2= generationExpression((CommonTree) t.getChild(1).getChild(0),tds);
				code = "LDW R1, R" + r2 + "\n"
						+ "STW R1,(BP)-"+ offsets.getById(t.getChild(0).getText())+"\n";
				writeTxt(code);
				break;
				 
			case "BLOC": {
				for(int i=0; i<t.getChildCount();i++) {
					parcoursTree((CommonTree) t.getChild(i),tds);
				}
				break;
			}
			case "IF":
				r1 = generationExpression((CommonTree) t, tds);
				code ="IF_	CMP R" + r1 + ",#1\n"
						+"BEQ THEN";
				writeTxt(code);
				if(t.getChildCount() > 2)
					if(t.getChild(2).equals("ELSE")) {
						code = "BNE ELSE\n"; //déplacement à étudier
						writeTxt(code);
						code = "THEN_ \n";
						writeTxt(code);
						parcoursTree((CommonTree) t.getChild(1), tds);
						code = "ELSE_ \n";
						writeTxt(code);
						parcoursTree((CommonTree) t.getChild(2).getChild(0), tds);
					}else {
						code = "THEN_ \n";
						writeTxt(code);
						parcoursTree((CommonTree) t.getChild(1), tds);
					}	
				
				/*
				 
				  r1 = generationExpression((CommonTree) t, tds);
				 
				code = "CMP R" + r1 + ",#1\n"
						+"BEQ BLOC -$-2\n";		  //déplacement à étudier
				writeTxt(code);
				if(t.getChildCount() > 2)
					if(t.getChild(2).equals("ELSE")) {
						code = "BNE ELSE -$-2\n"; //déplacement à étudier
						writeTxt(code);
						parcoursTree((CommonTree) t.getChild(1), tds);
						parcoursTree((CommonTree) t.getChild(2).getChild(0), tds);
					}else {
						parcoursTree((CommonTree) t.getChild(1), tds);
					}	
					*/
				break;
			case "WHILE":
				
				code="BOUCLE_\n";
				r1 = generationExpression((CommonTree) t.getChild(0), tds);
				code +=  "CMP R" + r1 + ",#1\n"
						+"BEQ BOUCLEBLOC\n"
						+"BNE BOUCLEFIN\n";
				
				code +="BOUCLEBLOC_ \n";
				parcoursTree((CommonTree) t.getChild(1), tds);
				code +="BMP BOUCLE\n";
				code +="BOUCLEFIN_ \n";
				break;
				
			case "FUNC":
				//ecriture du bloc de fonction
				if (t.getChild(0).getText().equals("main")) {
					code= "main_\n"
							+ "LDW SP,#STACK_ADRS\n"
							+ "LDQ NIL,BP\n";
				}else {
					code= t.getChild(0).getText()+"_\n";
				}
				code+= "LDQ" /*+tailletotale*/ + ",R1\n"
						+ "ADQ -2,SP\n"
						+ "STW BP,(SP)\n"
						+ "LDW BP,SP\n"
						+ "SUB SP,R1,SP\n";
				writeTxt(code);
				code="";
				parcoursTree((CommonTree) t.getChild(t.getChildCount()-1),  tds);
				
				if (t.getChild(0).getText().equals("main")) {
					code+=    "LDW SP,BP\n"
							+ "LDW BP,(SP)\n"
							+ "ADQ 2,SP\n";
		
					
					code= "TRP #EXIT_EXC\n"
							+ "JEA @main_\n";
					writeTxt(code);
					code="";
					
				} else
					try {
						if (tds.getFonction(tds, t.getChild(0).getText()).getReturnType()==null){
							code+=    "LDW SP,BP\n"
									+ "LDW BP,(SP)+\n";
							code+=    "LDW WR,(SP)\n"
									+ "ADQ 2,SP\n"
									+ "JEA (WR)\n";
							writeTxt(code);
							code="";
						} else {}
					} catch (NonExistantFunction e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;
				
			case "PRINT":
				int r3 = generationExpression((CommonTree) t.getChild(0),tds);
				code +=   "LDW R0,R" + r3 + "\n"				// on charge dans R0 la valeur en jeu car c'est R0 qui est lu par la trappe
						+ "LDQ WRITE_EXC , WR\n"  // on charge la trappe d'écriture dans WR
						+ "TRP WR\n" ;		 // lance la trappe 
				break;
				
			case "RETURN":
				code =  "LDW SP,BP\n"
						+ "LDW BP,(SP)\n"
						+ "ADQ 2,SP\n";
				
				code+=    "LDW WR,(SP)\n"
						+ "ADQ 2,SP\n"
						+ "JEA (WR)\n";
				writeTxt(code);
				code="";
				break;

				
			}
		}
	}
	
	private void fileDeleter()
	{
		File file = new File(filename);
		if(file.exists())
		{
			file.delete();
		}
	}
	
	private static int generationExpression(CommonTree tree, TableDesSymboles globalTable){
		int r1,r2,r3;
		
		if (tree.getChildCount()==2){
			
			r1=generationExpression((CommonTree) tree.getChild(0),globalTable);
			r2=generationExpression((CommonTree) tree.getChild(1),globalTable);
			r3=getRegister();
			String test=tree.getText();
			if(test=="+"||test=="-"||test=="*"||test=="/"||test=="&&"||test=="||") {
			switch(test) {
			case "+":
				code="ADC";
			case "-":
				code="SUB";
			case "*":
				code="MUL";
			case "/":
				code="DIV";
			case "&&":
				code="AND";
			case "||":
				code="OR";
				}
			releaseRegister(r1);
			releaseRegister(r2);
			code+=" R"+r1+", R"+r2+", R"+r3+"\n";
			
			} else if(test=="<"||test==">"||test=="<="||test==">="||test=="=="||test=="!=") {
				code += "CMP R" + r1 + ",R" + r2 + "\n";
				
				switch(test){
				case "==":
					code +=  "BEQ -$-2\n"
							+"BNE -$-2\n";
					break;

				case "!=":
					code +=  "BNE -$-2\n"
							+"BEQ -$-2\n";
					break;
				case "<":
					code +=  "BLW -$-2\n"
							+"BGE -$-2\n";
					break;
				case ">":
					code +=  "BGT -$-2\n"
							+"BLE -$-2\n";
					break;
				case "=>":
					code +=  "BGE -$-2\n"
							+"BLW -$-2\n";
					break;
				case "<=":
					code +=  "BLE -$-2\n"
							+"BGT -$-2\n";
					break;
				}
				code += "LDW R"+r3+", #1 \n" //on considère que 0 est faux et 1 vrai
						+"JEA -$-2\n"
						+"LDW R"+r3+",#0\n";
				
			}
			writeTxt(code);
			code="";
			return r3;
			
			
		}else if (tree.getChildCount()==0){
			try {
				int nb=Integer.parseInt(tree.getText());
				r1=getRegister();
				code="LDW R"+r1+",  #"+nb+"\n";
				writeTxt(code);
				return r1;
				
			}catch(Exception e) {
				if ("true".equals(tree.getText()) || "false".equals(tree.getText())) {
				r1=getRegister();
				int nb;
				if("true".equals(tree.getText())){
					nb=1;
				}else {
					nb=0;
				}
				code="LDW R"+r1+",  #"+nb+"\n";
				writeTxt(code);
				code="";
				return r1;
				} else if(tree.getChildCount()>0 ) {
					r1=getRegister();
					if (tree.getChild(0).equals("CALL_ARGS")) {
						int compteur=0;
						for (int i=tree.getChild(0).getChildCount();i>0;i--) {
							code += "LDW R1,(BP)-"+offsets.getById(tree.getChild(0).getChild(i).getText())+"\n"
									+ "STW R1,-(SP)\n";	
							compteur +=offsets.getById(tree.getChild(0).getChild(i).getText());
						}
						code += "JSR @"+tree.getChild(0).getText()+"\n"
								+ "ADQ "+ compteur +",SP\n"
										+ "LDW R"+r1+",r0\n";
					}
				} else {
					r1=getRegister();
					globalTable.getVariable(globalTable,tree.getText());
					code ="LDW R"+r1+""
				}
			}
			r1=getVarValueRegister(tree,globalTable);
			return r1;
		}
		
		return 0;
	}
	

	public static int getRegister(){
		int reg=Registerlist.get(Registerlist.size()-1);
		Registerlist.remove(Registerlist.size()-1);
		return reg;
	}
	
	public static void releaseRegister(int reg){
		Registerlist.add(reg);
	}
	
	public static void writeTxt(String w) {
		try {
			out.write(w);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getVarValueRegister(CommonTree tree,TableDesSymboles globalTable){
		int regad=getVarAdressRegister(tree,globalTable);
		int regval=getRegister();
		code="LDW R"+regval+", (R"+regad+")\n";
		writeTxt(code);
		code="";
		releaseRegister(regad);
		return regval;
	}
	
	public static int getVarAdressRegister(CommonTree tree,TableDesSymboles globalTable){
		int regbp=getRegister();
		code="LDW R"+regbp+", BP\n"
				+ "ADQ 4"+/*getdeplacement*/",R"+regbp+"\n";
		writeTxt(code);
		code="";
		return regbp;
	}
	
}

