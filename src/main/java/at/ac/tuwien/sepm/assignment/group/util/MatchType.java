package at.ac.tuwien.sepm.assignment.group.util;

public enum MatchType {
    RANKED2V2("2 vs 2 Ranked", 11),
    RANKED1V1("1 vs 1 Ranked", 10),
    RANKED3V3("3 vs 3 Ranked", 13),
    RANKED3V3SOLO("3 vs 3 Solo Ranked", 12);


    private String label;
    private int id;

    MatchType(String label, int id){
        this.label=label;
        this.id=id;
    }

    public static MatchType getById(int id) {
        for(MatchType e : values()) {
            if(e.id == id) return e;
        }
        return null;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return label;
    }
}




