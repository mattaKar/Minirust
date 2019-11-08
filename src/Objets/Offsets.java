package Objets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Offsets {

    public ArrayList<Offset> tableDesOffsets = new ArrayList<>();

    public void add(Offset offset) {
        String id = offset.getTdsId();
        int i=0;
        int len_offsets=this.tableDesOffsets.size();
        while (i<len_offsets && id.length()>=this.tableDesOffsets.get(i).getTdsId().length()) {
         i+=1;
        }
        int j=0;
        while (i<len_offsets && j<id.length()) {
            if (Integer.valueOf(id.charAt(j))>Integer.valueOf(this.tableDesOffsets.get(i).getTdsId().charAt(j))) {
                i+=1;
            } else {
                j+=1;
            }
        }
        this.tableDesOffsets.add(i,offset);
    }

    public void setDeplacementsGlobaux() {
        int deplacement=0;
        for (int i=0; i<this.tableDesOffsets.size(); i++) {
            Offset offset = this.tableDesOffsets.get(i);
            int deplacement_temp=Math.abs(offset.getDeplacement());
            if (i>0) {
                deplacement=this.tableDesOffsets.get(i-1).getGlobDeplacement()+this.tableDesOffsets.get(i-1).getTaille();
            }
            offset.setGlobDeplacement(deplacement);

           /* Offset offset = this.tableDesOffsets.get(i);
            int deplacement = offset.getDeplacement();
            if (deplacement>0) {
                precTaillePositif=deplacement;
                precDeplacementPositif=deplacement+precDeplacementPositif;
                offset.setGlobDeplacement(precDeplacementPositif);
            }
            else if (deplacement<0) {
                precTailleNegatif=deplacement;
                precDeplacementNegatif=deplacement+precDeplacementNegatif;
                offset.setGlobDeplacement(precDeplacementNegatif);
            }
            else {
                precDeplacementPositif=1+precDeplacementPositif;
                offset.setGlobDeplacement(precDeplacementPositif);
            }*/
        }
    }

    public boolean isInOffsets(String name, String tdsId) {
        for (int i=0; i<this.tableDesOffsets.size(); i++) {
            if (this.tableDesOffsets.get(i).getNameVar().equals(name) && (this.tableDesOffsets.get(i).getTdsId().equals(tdsId))) {
                return true;
            }
        }
        return false;
    }
    
    public int getById(String name) {
    	if(name != null) {
	    	for(Offset offset : this.tableDesOffsets)
	    		if(offset.getNameVar().equals(name))
	    			return offset.getDeplacement();
    	}
    	
    	return -1;
    }

    @Override
    public String toString() {
        StringBuilder stringOffsets = new StringBuilder("Offsets : \n");

        for (int i=0; i<this.tableDesOffsets.size(); i++)
            stringOffsets.append("\t").append(this.tableDesOffsets.get(i).toString());

        return stringOffsets.toString();
    }
}
