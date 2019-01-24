package at.ac.tuwien.sepm.assignment.group.replay.dto;

public enum BoostIDs {
    PAD0(0.0f, -4240.0f, 70.0f, 0),
    PAD1(-1792.0f, -4184.0f, 70.0f, 1),
    PAD2(1792.0f, -4184.0f, 70.0f, 2),
    PAD3(-3072.0f, -4096.0f, 73.0f, 3),
    PAD4(3072.0f, -4096.0f, 73.0f, 4),
    PAD5(-940.0f, -3308.0f, 70.0f, 5),
    PAD6(940.0f, -3308.0f, 70.0f, 6),
    PAD7(0.0f, -2816.0f, 70.0f, 7),
    PAD8(-3584.0f, -2484.0f, 70.0f, 8),
    PAD9(3584.0f, -2484.0f, 70.0f, 9),
    PAD10(-1788.0f, -2300.0f, 70.0f, 10),
    PAD11(1788.0f, -2300.0f, 70.0f, 11),
    PAD12(-2048.0f, -1036.0f, 70.0f, 12),
    PAD13(0.0f, -1024.0f, 70.0f, 13),
    PAD14(2048.0f, -1036.0f, 70.0f, 14),
    PAD15(-3584.0f, 0.0f, 73.0f, 15),
    PAD16(-1024.0f, 0.0f, 70.0f, 16),
    PAD17(1024.0f, 0.0f, 70.0f, 17),
    PAD18(3584.0f, 0.0f, 73.0f, 18),
    PAD19(-2048.0f, 1036.0f, 70.0f, 19),
    PAD20(0.0f, 1024.0f, 70.0f, 20),
    PAD21(2048.0f, 1036.0f, 70.0f, 21),
    PAD22(-1788.0f, 2300.0f, 70.0f, 22),
    PAD23(1788.0f, 2300.0f, 70.0f, 23),
    PAD24(-3584.0f, 2484.0f, 70.0f, 24),
    PAD25(3584.0f, 2484.0f, 70.0f, 25),
    PAD26(0.0f, 2816.0f, 70.0f, 26),
    PAD27(-940.0f, 3310.0f, 70.0f, 27),
    PAD28(940.0f, 3308.0f, 70.0f, 28),
    PAD29(-3072.0f, 4096.0f, 73.0f, 29),
    PAD30(3072.0f, 4096.0f, 73.0f, 30),
    PAD31(-1792.0f, 4184.0f, 70.0f, 31),
    PAD32(1792.0f, 4184.0f, 70.0f, 32),
    PAD33(0.0f, 4240.0f, 70.0f, 33);


    private float x;
    private float y;
    private float z;
    private int id;
    private static float tolerance = 500.0f;

    BoostIDs(float x, float y, float z, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public static int getID(float x, float y, float z) {

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
