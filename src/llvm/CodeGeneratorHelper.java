/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package llvm;

/**
 *
 * @author Juan Pablo
 */
public class CodeGeneratorHelper {

    private static int nextID = 0;

    private CodeGeneratorHelper(){}

    public static String getNewPointer(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("%%ptro.%s", nextID));
        return ret.toString();
    }

    public static String getNewGlobalPointer(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("@gb.%s", nextID));
        return ret.toString();
    }

    public static String getNewTag(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("tag.%s", nextID));
        return ret.toString();
    }

}