package Objets;

import Exceptions.InvalidArgumentsNumber;

public class Fonction {
    private String name;
    private String returnType;
    private Arguments args;
    private int deplacement;

    Fonction(String name, String returnType, Arguments args, int deplacement) {
        this.name = name;
        this.returnType = returnType;
        this.args = args;
        this.deplacement = deplacement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        if (returnType != null && (returnType.substring(0, 1).equals("&") || returnType.substring(0, 1).equals("*")))
            return returnType.substring(2, returnType.length());

        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public Arguments getArgs() {
        return args;
    }

    public void setArgs(Arguments args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "\tFonction : " + this.name + " | retour : " + this.returnType + " | Arguments : " + this.args + " | Deplacement : " + this.deplacement + "\n";
    }

    public void validNumberArgs(Fonction fonc, int num) throws InvalidArgumentsNumber {
        if (fonc.getArgs().getNames().size() != num)
            throw new InvalidArgumentsNumber(name, fonc.getArgs().getNames().size());
    }
}