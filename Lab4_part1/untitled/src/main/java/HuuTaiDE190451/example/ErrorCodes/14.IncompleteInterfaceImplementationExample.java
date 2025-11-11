package HuuTaiDE190451.example.ErrorCodes;

interface Shape {
    void draw();
    void resize();
}

class Square implements Shape {
    public void draw() {
        System.out.println("Drawing square");
    }
    
}
