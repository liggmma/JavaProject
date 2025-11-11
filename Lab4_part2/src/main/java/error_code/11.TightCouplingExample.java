package errorCode;

class Printer {
    void print(String message) {
        System.out.println(message);
    }
}

class Report {
    private Printer printer = new Printer(); // tightly coupled

    void generate() {
        printer.print("Generating report...");
    }
}
