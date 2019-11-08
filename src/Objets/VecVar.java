package Objets;

public abstract class VecVar {
    private String name;
    private String type;
    private boolean pointeur;
    private boolean param;
    protected int deplacement;

    VecVar() {}

    VecVar(String name, String type, boolean pointeur, boolean param) {
        this.name = name;
        this.type = type;
        this.pointeur = pointeur;
        this.param = param;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeplacement(int deplacement) {
        this.deplacement = deplacement;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    boolean isParam() {
        return this.param;
    }

    void setParam() { this.param = true; }

    boolean isPointeur() {
        return this.pointeur;
    }

    void setPointeur(boolean pointeur) { this.pointeur = pointeur; }

    @Override
    public abstract String toString();
}
