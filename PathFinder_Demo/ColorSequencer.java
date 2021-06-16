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
    public static Color current() {
        return curColor;
    }

    /**
     * change the color to a contrasting color
     */
    public static Color next() {
        Color last = curColor;
        int r = curColor.getRed();
        if (r == 0) {
            r = 1;
        }
        int g = curColor.getGreen();
        if (g == 0) {
            g = 1;
        }
        int b = curColor.getBlue();
        if (b == 0) {
            b = 1;
        }
        // System.out.println("Old color: r=" + r + " g=" + g + " b=" + b);
        int newr = (int) (Math.random() * 43 * b) % 256;
        int newg = r;
        int newb = g;
        if (newr < 100 && newg < 100 && newb < 100) {
            if (Math.random() < 0.5) {
                newg += 100;
            } else {
                newb += 100;
            }
        }
        // System.out.println("New color: r=" + newr + " g=" + newg + " b=" + newb);
        curColor = new Color(newr, newg, newb);
        if(last.equals(Color.orange)||last.equals(Color.red)) next();
        return (last);
    }

    public static Color reset(){
        curColor = new Color(130, 251, 23);
        return curColor;
    }
    
    private static Color curColor = new Color(130, 251, 23);

}
