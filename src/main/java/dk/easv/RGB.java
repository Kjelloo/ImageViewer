/**
 * @author kjell
 */

package dk.easv;

public class RGB {
    private int red;
    private int green;
    private int blue;
    private int mixed;

    public RGB(int red, int green, int blue, int mixed) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.mixed = mixed;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getMixed() {
        return mixed;
    }

    public void setMixed(int mixed) {
        this.mixed = mixed;
    }

    @Override
    public String toString() {
        return "red = " + red + ", green = " + green + ", blue = " + blue + ", mixed = " + mixed;
    }
}
