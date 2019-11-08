package Objets;

import Exceptions.*;
import Expr.TreeParser;

import java.util.HashMap;

public class TableDesFonctions extends Table<String, Fonction> {
    void ajouterFonction(TableDesSymboles tableSymboles, String name, String returnType, Arguments args, Offsets tabOffsets) throws AlreadyExistantFonction, WrongRegionDeclaration, NonExistantType, NonExistantStructure, NonExistantVecteur, NonExistantVariable {
        Fonction fonction = ((TableDesFonctions) tableSymboles.get(TableType.FONC)).get(name);

        if (!tableSymboles.getName().equals("1"))
            throw new WrongRegionDeclaration(name);
        else if (fonction != null && fonction.getArgs().getTypes().equals(args.getTypes()) && fonction.getReturnType().equals(returnType))
            throw new AlreadyExistantFonction(name);
        else {
            if (args != null) {
                for (String type : args.getTypes()) {
                    String[] types = type.split(" ");

                    if(types[0].equals("VEC")) {
                        for (String vecType : types) {
                            if (!vecType.equals("bool") && !vecType.equals("i32") && !vecType.equals("& bool") && !vecType.equals("& i32") && !vecType.equals("VEC")) {
                                if (tableSymboles.getStructure(tableSymboles, vecType, false) == null)
                                    throw new NonExistantType(type);
                            }
                        }
                    } else if (!type.equals("bool") && !type.equals("i32") && !type.equals("& bool") && !type.equals("& i32") && tableSymboles.getStructure(tableSymboles, type, false) == null)
                        throw new NonExistantType(type);
                }
            }
            
            if(returnType != null) {
	            String[] returnTypes = returnType.split(" ");
	            
	            if(returnTypes[0].equals("VEC")) {
	                for (String returnType2 : returnTypes) {
	                    if (!returnType2.equals("bool") && !returnType2.equals("i32") && !returnType2.equals("& bool") && !returnType2.equals("& i32") && !returnType2.equals("VEC")) {
	                        if (tableSymboles.getStructure(tableSymboles, returnType2, false) == null)
	                            throw new NonExistantType(returnType2);
	                    }
	                }
	            } else if (!returnType.equals("bool") && !returnType.equals("i32") && (tableSymboles.getStructure(tableSymboles, returnType, true) == null))
	                throw new NonExistantType(returnType);
            }
            
            int deplacement = TreeParser.getDeplacement(name,returnType,tableSymboles);
            tableSymboles.updateDeplacements(deplacement);
            tabOffsets.add(new Offset(name, tableSymboles.getName(), tableSymboles.getCurrentDeplacement(),deplacement));
            this.put(name, new Fonction(name, returnType, args, tableSymboles.getCurrentDeplacement()));
        }
    }

    @Override
    public String toString() {
        StringBuilder stringFonctions = new StringBuilder("Fonctions : \n");

        for (HashMap.Entry<String, Fonction> entry : table.entrySet())
            stringFonctions.append("\t").append(entry.getValue().toString());

        return stringFonctions.toString();
    }
}
