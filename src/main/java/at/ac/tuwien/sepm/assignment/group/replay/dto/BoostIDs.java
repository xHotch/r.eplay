package at.ac.tuwien.sepm.assignment.group.replay.dto;

public enum BoostIDs {
    PAD0(0.0, -4240.0, 70.0, 0),
    PAD1(-1792.0, -4184.0, 70.0, 1),
    PAD2(1792.0, -4184.0, 70.0, 2),
    PAD3(-3072.0, -4096.0, 73.0, 3),
    PAD4(3072.0, -4096.0, 73.0, 4),
    PAD5(-940.0, -3308.0, 70.0, 5),
    PAD6(940.0, -3308.0, 70.0, 6),
    PAD7(0.0, -2816.0, 70.0, 7),
    PAD8(-3584.0, -2484.0, 70.0, 8),
    PAD9(3584.0, -2484.0, 70.0, 9),
    PAD10(-1788.0, -2300.0, 70.0, 10),
    PAD11(1788.0, -2300.0, 70.0, 11),
    PAD12(-2048.0, -1036.0, 70.0, 12),
    PAD13(0.0, -1024.0, 70.0, 13),
    PAD14(2048.0, -1036.0, 70.0, 14),
    PAD15(-3584.0, 0.0, 73.0, 15),
    PAD16(-1024.0, 0.0, 70.0, 16),
    PAD17(1024.0, 0.0, 70.0, 17),
    PAD18(3584.0, 0.0, 73.0, 18),
    PAD19(-2048.0, 1036.0, 70.0, 19),
    PAD20(0.0, 1024.0, 70.0, 20),
    PAD21(2048.0, 1036.0, 70.0, 21),
    PAD22(-1788.0, 2300.0, 70.0, 22),
    PAD23(1788.0, 2300.0, 70.0, 23),
    PAD24(-3584.0, 2484.0, 70.0, 24),
    PAD25(3584.0, 2484.0, 70.0, 25),
    PAD26(0.0, 2816.0, 70.0, 26),
    PAD27(-940.0, 3310.0, 70.0, 27),
    PAD28(940.0, 3308.0, 70.0, 28),
    PAD29(-3072.0, 4096.0, 73.0, 29),
    PAD30(3072.0, 4096.0, 73.0, 30),
    PAD31(-1792.0, 4184.0, 70.0, 31),
    PAD32(1792.0, 4184.0, 70.0, 32),
    PAD33(0.0, 4240.0, 70.0, 33);


    private double x;
    private double y;
    private double z;
    private int id;
    private static double tolerance = 200.0;

    BoostIDs(double x, double y, double z, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public static int getID(double x, double y, double z) {

        for (BoostIDs pad:values()) {
            if((x <= pad.x + tolerance && x > pad.x - tolerance) &&
                (y <= pad.y + tolerance && y > pad.y - tolerance) &&
                (z <= pad.z + tolerance && z > pad.z - tolerance)) {

                return pad.id;
            }
        }
        return -1;
    }
}
