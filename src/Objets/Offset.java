package Objets;

public class Offset {

    private  String nameVar;
    private String tabId;
    private int deplacement;
    private int glob_deplacement;
    private int taille;

    public Offset(String nameVar, String tabId, int deplacement, int taille) {
        this.nameVar = nameVar;
        this.tabId = tabId;
        this.deplacement = deplacement;
        this.glob_deplacement=0;
        this.taille=taille;
    }

    public int getDeplacement() {
        return deplacement;
    }

    public int getTaille() {
        return this.taille;
    }

    public int getGlobDeplacement() {
        return glob_deplacement;
    }

    public void setGlobDeplacement(int glob_deplacement) {
        this.glob_deplacement=glob_deplacement;
    }

    public String getNameVar() {
        return nameVar;
    }

    public String getTdsId() {
        return tabId;
    }

    public String toString() {
        return "Variable : " + this.nameVar + " | tabId : " + this.tabId + " | deplacement : " + this.deplacement + " | deplacement_global : " + this.glob_deplacement + "\n";
    }
}
