package errorCode;
interface Drawable {
    void draw();
}

abstract class Circle implements Drawable {
    // Missing draw() implementation → compile error
}
