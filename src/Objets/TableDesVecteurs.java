package Objets;

import Exceptions.InvalidTypeAffectation;
import Exceptions.InvalidVecteurVariableType;
import Exceptions.NonExistantVecteur;
import Exceptions.NonMutable;

import java.util.ArrayList;
import java.util.HashMap;

import static Expr.TreeParser.findType;

public class TableDesVecteurs extends Table<String, Vecteur> {
    void ajouterVecteur(TableDesSymboles tableDesSymbolesCour, TableDesSymboles tableSymboles, String name, String type, ArrayList<String> valeurs, boolean pointeur, boolean param, int deplacement, Offsets tabOffsets) throws InvalidVecteurVariableType, InvalidTypeAffectation {
        Vecteur vecteur = ((TableDesVecteurs) tableSymboles.get(TableType.VEC)).get(name);

        if (vecteur != null) {
            if (!vecteur.getType().equals(type) && !vecteur.getType().equals(""))
                throw new InvalidTypeAffectation(name, vecteur.getType(), type);
            else tableSymboles.get(TableType.VEC).put(name,  new Vecteur(name, type, valeurs, vecteur.isPointeur(), vecteur.isParam(), vecteur.getDeplacement()));
        } else if (tableSymboles.getParent().get(TableType.VAR) == null || tableSymboles.getParent().getName().equals("1")) {
            tableDesSymbolesCour.updateDeplacements(deplacement);
            tabOffsets.add(new Offset(name, tableDesSymbolesCour.getName(), tableDesSymbolesCour.getCurrentDeplacement(),deplacement));
            this.put(name, new Vecteur(name, type, valeurs, pointeur, param, tableDesSymbolesCour.getCurrentDeplacement()));
        }
        else {
            this.ajouterVecteur(tableDesSymbolesCour, tableSymboles.getParent(), name, type, valeurs, pointeur, param, deplacement, tabOffsets);
        }
    }


    public Vecteur getVecteur(TableDesSymboles tableSymboles, String name) throws NonExistantVecteur {
        Vecteur vecteur = ((TableDesVecteurs) tableSymboles.get(TableType.VEC)).get(name);

        if (vecteur != null)
            return vecteur;
        else if (tableSymboles.getParent() == null)
            throw new NonExistantVecteur(name);

        return this.getVecteur(tableSymboles.getParent(), name);
    }

    @Override
    public String toString() {
        StringBuilder stringVecteurs = new StringBuilder("Vecteurs : \n");

        for (HashMap.Entry<String, Vecteur> entry : table.entrySet())
            stringVecteurs.append("\t").append(entry.getValue().toString());

        return stringVecteurs.toString();
    }
}