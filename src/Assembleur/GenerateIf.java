package Assembleur;

import java.io.IOException;

import org.antlr.runtime.tree.CommonTree;

import Objets.Offsets;
import Objets.TableDesSymboles;


public class GenerateIf {
	private static String code="";
<<<<<<< HEAD

	//*
	public String gen_runner(CommonTree t, TableDesSymboles TDScourante, Offsets offsets){
=======
/*
	public String gen_runner(CommonTree t){
>>>>>>> 129a6a42192b795f507666190edd999788311da1
		String test = t.getChild(0).getText();
		String code="";
		String isElse = "";
		
		if (t.getChildCount()==3) {
			isElse="ELSE";
		}else {
			isElse="FI";
		}
		
		if (t.getChild(0).getChildCount()>0) { //plusieurs enfant donc comparaison
			String fg = t.getChild(0).getChild(0).getText();
			String fd = t.getChild(0).getChild(1).getText();
			if ("true".equals(fg) || "false".equals(fg)) {
				code += "LDW R0,#"+fg+"\n";
			} else {
				int offset = offsets.getById(t.getChild(0).getText());
				code += "LDW R0, (BP) - " + offset + "\n";
			}
			if ("true".equals(fd) || "false".equals(fd)) {
				code += "LDW R1,#"+fd+"\n";
			} else {
				int offset = offsets.getById(t.getChild(0).getText());
				code += "LDW R1, (BP) - " + offset + "\n";
			}
			
			code += "CMP R0,R1\n";
			
			switch(test){
			case "==":
				code +=  "BEQ BLOC" + Code_gen.iLoop+"-$-2\n"
						+"BNE" + isElse + Code_gen.iLoop+"-$-2\n";
				break;
			case "!=":
				code +=  "BNE BLOC" + Code_gen.iLoop+"-$-2\n"
						+"BEQ" + isElse + Code_gen.iLoop+"-$-2\n" ;
				break;
			case "<":
				code +=  "BLW BLOC"+ Code_gen.iLoop+"-$-2\n"
						+"BGE" + isElse + Code_gen.iLoop+"-$-2\n";
				break;
			case ">":
				code +=  "BGT BLOC" + Code_gen.iLoop+"-$-2\n"
						+"BLE" + isElse + Code_gen.iLoop+"-$-2\n";
				break;
			case "=>":
				code +=  "BGE BLOC"+ Code_gen.iLoop+"-$-2\n"
						+"BLW" + isElse + Code_gen.iLoop+"-$-2\n";
				break;
			case "<=":
				code +=  "BLE BLOC" + Code_gen.iLoop+"-$-2\n"
						+"BGT" + isElse + Code_gen.iLoop+"-$-2\n";
				break;
			}
		} else { //cas ou il n'y a pas de comparaison mais juste une constante/variable
			
			if("true".equals(test) || "false".equals(test)) {
				code =  "CMP #"+ test + ", #true \n"
					 + "BEQ BLOC"     + Code_gen.iLoop+"-$-2\n"
					 + "BNE" + isElse + Code_gen.iLoop+"-$-2\n";
			} else {// cas ou on a juste une variable 
				int offset = offsets.getById(t.getChild(0).getText());
				code = "LDW R0, (BP) - " + offset + "\n"
						+ "CMP #"+ test + ", #true \n"
						+ "BEQ BLOC"     + Code_gen.iLoop+"-$-2\n"
						+ "BNE" + isElse + Code_gen.iLoop+"-$-2\n";
			}
		}
		return code;
	}
<<<<<<< HEAD

	//*/

	//*
	public static int generationExpression(CommonTree tree,TableDesSymboles globalTable){
=======
	*/
	
	
	public static int generationExpression(CommonTree tree,TDS globalTable){
>>>>>>> 129a6a42192b795f507666190edd999788311da1
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
					code +=  "BEQ $+2\n"
							+"BNE $+2\n";
				case "!=":
					code +=  "BNE $+2\n"
							+"BEQ $+2\n";
				case "<":
					code +=  "BLW $+2\n"
							+"BGE $+2\n";
				case ">":
					code +=  "BGT $+2\n"
							+"BLE $+2\n";
				case "=>":
					code +=  "BGE $+2\n"
							+"BLW $+2\n";
				case "<=":
					code +=  "BLE $+2\n"
							+"BGT $+2\n";
				}
				code += "LDW R"+r3+",#1\n" //on considère que 0 est faux et 1 vrai
						+"JEA $+2\n"
						+"LDW R"+r3+",#0\n";
				
			}
			writeTxt(code);
			code="";
			return r3;
			
			
		}else if (tree.getChildCount()==0){
			if(tree.getText().isInteger()) {
				int nb=Integer.parseInt(tree.getText());
				r1=getRegister();
				code="LDW R"+r1+",  #"+nb+"\n";
				writeTxt(code);
				return r1;
			} else if ("true".equals(tree.getText()) || "false".equals(tree.getText())) {
				int nb;
				if("true".equals(tree.getText())){
					nb=1;
				}else if("false".equals(tree.getText())){
					nb=0;
				}
				r1=getRegister();
				code="LDW R"+r1+",  #"+nb+"\n";
				writeTxt(code);
				return r1;
			}
			
			r1=getVarValueRegister(tree,globalTable);
			return r1;
		}
		
		return 0;
	}
	
	public static int getRegister(){
		int reg=listeRegister.get(listeRegister.size()-1);
		listeRegister.remove(listeRegister.size()-1);
		return reg;
	}
	
	public static void releaseRegister(int reg){
		listeRegister.add(reg);
	}
	
	public static void writeTxt(String txt){
		try {
			cibleFile.write(txt.getBytes());
		} catch (IOException e) {
		}
	}
<<<<<<< HEAD

	//*/
=======
>>>>>>> 129a6a42192b795f507666190edd999788311da1
}


