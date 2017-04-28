
public class FixUnknownOrientation{
    public static void main(String[] args){
        String replaced = "ATCG";
        
        replaced = replaced.replaceAll("(?i)A", "w");
        replaced = replaced.replaceAll("(?i)T", "x");
        replaced = replaced.replaceAll("(?i)C", "y");
        replaced = replaced.replaceAll("(?i)G", "z");
        
        replaced = replaced.replace("w", "T");
        replaced = replaced.replace("x", "A");
        replaced = replaced.replace("y", "G");
        replaced = replaced.replace("z", "C");
        StringBuffer a = new StringBuffer(replaced);       
        System.out.println("After Reversing: "+a.reverse());
    }
}
