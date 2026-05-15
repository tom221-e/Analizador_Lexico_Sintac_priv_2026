package ast;

public abstract class Expresion extends Nodo {

    private String ir_ref;

    public Expresion() {
    }
    public String getIr_ref() {
        return ir_ref;
    }

    public void setIr_ref(String ir_ref) {
        this.ir_ref = ir_ref;
    }

}