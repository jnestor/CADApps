package pathfinder_demo;

import java.awt.Color;

/**
 * this class generates a sequence of contrasting colors - useful for
 * highlighting different parts of a diagram with different colors
 */
class ColorSequencer {

    /**
     * returns the current color in the sequence
     */
    private static float hue = 0;
    private static float sat = 1;
    private static float brit = 1;

    /**
     * change the color to a contrasting color
     */
    public static Color next() {
        Color c = new Color(Color.HSBtoRGB(hue, sat, brit));
        hue += 0.1;
        if ((int)(hue*10) == 4) {
            hue += 0.1;
        }
        System.out.println(hue);
        if (hue > 0.9) {
            if (sat > 0) {
                sat -= 0.2;
            } else {
                sat = 1;
                brit -= 0.5;
            }
            hue = 0;
        }
        return c;
    }

    public static void reset() {
        hue = 0;
        sat = 1;
        brit = 1;
    }

}
