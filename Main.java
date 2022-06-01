import syntaxtree.*;
import visitor.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

public class
Main {
    public static void main(String[] args) throws Exception {
        for(int i = 0; i < args.length; i++) {
            FileInputStream fis=null;
            PrintWriter fos = null;
            try {
                fis = new FileInputStream(args[i]);
                String outputfile = args[i].replace(".java",".ll");
                fos = new PrintWriter(outputfile);
                System.err.printf("\nParsing file %s\n",args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                System.err.println("Program parsed successfully.");
                DeclVisitor decvis = new DeclVisitor();
                root.accept(decvis, null);
                CodeGenVisitor cgvis = new CodeGenVisitor(fos);
                root.accept(cgvis,null);
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
                if (fos != null) {
                    fos.close();
                    if (fos.checkError()) {
                        System.err.println(("Error when closing print writer"));
                    }
                }
            }
        }
    }
}


class MethInfo{
    String name;
    String type;
    String params;
    public MethInfo (String n, String t, String p){
        name = n;
        type = t;
        params = p;
    }
}

class DeclVisitor extends GJDepthFirst<String,String>{
    static Map<String, String> mparams; //contains keys Class::Methodname and values Type,Type,Type... where Type is the type of the parameter
    static Map<String,Map<String,String>> vardec; //fields and local variables
    static Map<String,Map<String,String>> methdec; //methods
    static Map<String,String> classdec; //classes and parent classes
    static String mainclass; //main class for printing purposes
    static Map<String,Map<String, Integer>> methoffsets; //offsets for methods
    static Map<String,Map<String,Integer>> fieldoffsets; //offsets for fields
    static Map<String,ArrayList<MethInfo>> vtables;
    public DeclVisitor(){
        //map initialization, we use LinkedHashMaps to preserve insertion order
        vardec = new LinkedHashMap<String,Map<String,String>>();
        methdec = new LinkedHashMap<String, Map<String, String>>();
        classdec = new LinkedHashMap<String, String>();
        methoffsets = new LinkedHashMap<String,Map<String,Integer>>();
        fieldoffsets = new LinkedHashMap<String,Map<String,Integer>>();
        mparams = new LinkedHashMap<String, String>();
        vtables = new LinkedHashMap<String,ArrayList<MethInfo>>();
    }
    /**
     * Grammar production:
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"`
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    static public int sizeof(String type){ //returns the size of type for offsets
        if(type.equals("int")){
            return 4;
        }
        else if(type.equals("boolean")){
            return 1;
        }
        else {
            return 8;
        }
    }
    public void redefinition_error(String type, String var, String scope){ //print function for redefinition
        System.out.printf("error: %s %s is already defined in %s%n",type,var,scope);
    }
    public String overriden(String classname, String methodname){ //loops over the inheritance chain to find if the method is to be overriden
        String ext = classname;
        while (ext!=null ){
            if(methdec.containsKey(ext) && methdec.get(ext).containsKey(methodname)){ //if you find declaration in a parent class than you must override it
                return ext;
            }
            ext = classdec.get(ext);
        }
        return null; //no previous declaration
    }
    /**
     * Grammar production:
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String argu) throws Exception {
        String classname =  n.f1.accept(this,argu);
        classdec.put(classname,null);
        mainclass = classname;
        String mainvars = n.f14.accept(this,argu); //get all var declartions in main method, return them in a string like "type name,type name,type name..."
        Map<String,String> vars = new LinkedHashMap<String,String>(); //symbol table for current scope
        vars.put(n.f11.accept(this,argu),"String[]");
        if(!mainvars.isEmpty()) {
            for (String m : mainvars.split(",")) { //string manipulation
                String[] mainvar = m.split(" ");
                if(!vars.containsKey(mainvar[1])) {
                    vars.put(mainvar[1], mainvar[0]);
                }
                else{
                    redefinition_error("variable",mainvar[1],"method " +classname + "::" + "main");
                }
            }
            if(!vars.isEmpty()){
                vardec.put(classname + "::" + "main",vars); //insert symbol table, if it has anything in
            }
        }
        vtables.put(classname,null);
        return null;
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        int off = 0;
        if(classdec.containsKey(classname)){ //handles class redeclaration
            System.out.printf("error: class %s is already defined\n",classname);
            return null;
        }
        else{
            classdec.put(classname,null);
        }
        String fields = n.f3.present()?n.f3.accept(this,classname):",";
        Map<String,String> fields_st = new LinkedHashMap<String, String>();
        Map<String ,Integer> fields_off = new LinkedHashMap<String, Integer>();
        int offset = 0;
        for(String f: fields.split(",")){ //populate symbol table for current scope, similar to previous one
            String[] field = f.split(" ");
            if(!fields_st.containsKey(field[1])){
                fields_st.put(field[1],field[0]);
                fields_off.put(field[1],offset);
                offset+=sizeof(field[0]);
            }
            else {
                redefinition_error("variable",field[1],"class " +classname);
            }
        }
        if(!fields_st.isEmpty()) {
            vardec.put(classname, fields_st);
        }
        if(!fields_off.isEmpty()) {
            fieldoffsets.put(classname, fields_off);
        }
        Map<String,String> methods_st = new LinkedHashMap<String, String>();        String methods = n.f4.present()?n.f4.accept(this,classname):",";
        Map<String ,Integer>  methods_off = new LinkedHashMap<String, Integer>();
        offset = 0;
        ArrayList<MethInfo> vt = new ArrayList<MethInfo>(n.f4.nodes.size());
        for(String m : methods.split(",")){ //populate method symbol table for current scope
            String[] method = m.split(" ");
            if(!methods_st.containsKey(method[1])){
                    methods_st.put(method[1],method[0]);
                    methods_off.put(method[1],offset);
                    offset+=8;
                    vt.add(new MethInfo(classname+"."+method[1],method[0],mparams.get(classname+"::"+method[1])));

            }
            else {
                redefinition_error("method",method[1],"class " + classname);
            }
        }
        if(!methods_st.isEmpty()) {
            methdec.put(classname, methods_st);
        }
        if(!methods_off.isEmpty()){
            methoffsets.put(classname, methods_off);
            Map<String,String> vtable = new LinkedHashMap<>();

        }
        vtables.put(classname,vt);
        return null;
    }



    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String extname = n.f3.accept(this,classname);
        int f_offset;
        int m_offset;
        if(!classdec.containsKey(extname)) { //handle case of no previous definition of parent class
            System.out.printf("class %s must be defined before class %s\n", extname, classname);
            classdec.put(classname,null);
            f_offset = 0;
            m_offset = 0;
        }
        else{
            if(fieldoffsets.containsKey(extname)){
                String[] f_orderedKeys = fieldoffsets.get(extname).keySet().toArray(new String[fieldoffsets.get(extname).size()]); //get the offset of previous parent class, continue from there on
                int f_last = f_orderedKeys.length - 1;
                f_offset = fieldoffsets.get(extname).get(f_orderedKeys[f_last]) + sizeof(vardec.get(extname).get(f_orderedKeys[f_last]));
            }
            else{
                f_offset = 0;
            }
            if(methoffsets.containsKey(extname)) {
                String[] m_orderedKeys = methoffsets.get(extname).keySet().toArray(new String[methoffsets.get(extname).size()]); //get the offset of previous parent class, continue from there on
                int m_last = m_orderedKeys.length - 1;
                m_offset = methoffsets.get(extname).get(m_orderedKeys[m_last]) + 8;
            }
            else{
                m_offset = 0;
            }
            classdec.put(classname, extname);
        }
        String fields = n.f5.present()?n.f5.accept(this,classname):","; //same as classdeclaration
        Map<String,String> fields_st = new LinkedHashMap<String, String>();
        Map<String,Integer> fields_off = new LinkedHashMap<String, Integer>();
        for(String f: fields.split(",")){
            String[] field = f.split(" ");
            if(!fields_st.containsKey(field[1])){
                fields_st.put(field[1],field[0]);
                fields_off.put(field[1],f_offset);
                f_offset+=sizeof(field[0]);
            }
            else {
                redefinition_error("variable",field[1],"class " +classname);
            }
        }
        if(!fields_st.isEmpty()) {
            vardec.put(classname, fields_st);
        }
        if(!fields_off.isEmpty()) {
            fieldoffsets.put(classname, fields_off);
        }
        Map<String,String> methods_st = new LinkedHashMap<String, String>();
        Map<String,Integer> methods_off = new LinkedHashMap<String, Integer>();
        String methods = n.f6.present()?n.f6.accept(this,classname):",";
        ArrayList<MethInfo> extvt = vtables.get(extname);
        ArrayList<MethInfo> vt;
        if(extvt.size() != 0) {
            vt = new ArrayList<MethInfo>(extvt);
        }
        else {
            vt = new ArrayList<MethInfo>(n.f6.nodes.size());
        }
        for(String m : methods.split(",")){
            String[] method = m.split(" ");
            if(!methods_st.containsKey(method[1])){
                methods_st.put(method[1],method[0]);
                String inherited = overriden(extname,method[1]);
                if(inherited == null){
                    methods_off.put(method[1],m_offset);
                    vt.add(new MethInfo(classname+"."+method[1],method[0],mparams.get(classname+"::"+method[1])));
                }
                else{
                    int off = methoffsets.get(inherited).get(method[1]);
                    vt.set(off/8,new MethInfo(classname+"."+method[1],method[0],mparams.get(classname+"::"+method[1])));
                }
                m_offset+=8;
            }
            else {
                redefinition_error("method",method[1],"class "+classname);
            }
        }
        if(!methods_st.isEmpty()) {
            methdec.put(classname, methods_st);
        }
        if(!methods_off.isEmpty()){
            methoffsets.put(classname, methods_off);
        }
        vtables.put(classname,vt);
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : ","; //get methods arguments
        String methodtype = n.f1.accept(this, null);
        String methodname = n.f2.accept(this, null);
        String localvars = n.f7.present() ? n.f7.accept(this,argu+"::"+methodname) : ","; //get methods local vars
        Map<String,String> locvars = new LinkedHashMap<String, String>();
        String[] args = argumentList.split(",");
        StringJoiner jparamtypes = new StringJoiner(",");
        for(String a : args){ //extract only the types of the parameters for mparams symbol table
            String type = a.split(" ")[0];
            jparamtypes.add(type);
        }
        String paramtypes = jparamtypes.toString();
        int i =0;
        for (String arg : argumentList.split(",")){ //populate local vars symbol table, starting with the arguments first
            String[] argument = arg.split(" ");
            if(!locvars.containsKey(argument[1])){
                locvars.put(argument[1],argument[0]);
            }
            else{
                redefinition_error("variable",argument[1],"method " + argu+"::"+methodname);
            }
        }
        for (String l : localvars.split(",")) { //now with the local vars
            String[] localvar = l.split(" ");
            if(!locvars.containsKey(localvar[1])){
                locvars.put(localvar[1],localvar[0]);
            }
            else{
                redefinition_error("variable",localvar[1],"method " + argu+"::"+methodname);
            }
        }
        if(!locvars.isEmpty()){
            vardec.put(argu+"::"+methodname,locvars);
        }
        String ext = classdec.get(argu);
        while(ext!=null) { //check for overriding functions
            if (mparams.containsKey(ext + "::" + methodname)) {
                System.out.println(mparams.get(ext+"::"+methodname));
                String supertype = methdec.get(ext).get(methodname);
                if (!mparams.get(ext + "::" + methodname).equals(paramtypes) || !methodtype.equals(supertype)) {
                    System.out.println(("error: to redefine parent class"+
                            " method both return and parameters types must match"));
                }
                break;
            }
            ext = classdec.get(ext);
        }
        mparams.put(argu+"::"+methodname,paramtypes);
        return methodtype+" "+methodname;
    }

    public String visit(NodeListOptional n, String argu) throws Exception { //this is for returning NodeLists with "," delimiter
        StringJoiner nodes = new StringJoiner(",");
        for (Node node: n.nodes){
            nodes.add(node.accept(this, argu));
        }
        return nodes.toString();
    }
    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1.f0.present()) {
            ret += n.f1.accept(this, null);
        }
        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }
        return ret;
    }
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n,String argu) throws Exception{
        String type = n.f0.accept(this,argu);
        String name = n.f1.f0.tokenImage;
        return type + " " + name;
    }
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n,String argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    public String visit(BooleanArrayType n,String argu) {
        return "boolean[]";
    }
    @Override
    public String visit(IntegerArrayType n,String argu) {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return "int";
    }


    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}



class CodeGenVisitor extends GJDepthFirst<String,String>{

    private  int rcounter;

    private int arraytype_size; //i32->4 bytes + i1*|i32* ->8bytes
    private PrintWriter fos;
    public CodeGenVisitor(PrintWriter f){
        fos = f;
        arraytype_size = 12; //i32->4 bytes + i1*|i32* ->8bytes
    }
    private int getClassSize(String name) {
        int sz = 0;
        while(!DeclVisitor.fieldoffsets.containsKey(name) && name != null){
            name = DeclVisitor.classdec.get(name);
        }
        if(name != null){
            Map <String,Integer> fields = DeclVisitor.fieldoffsets.get(name);
            String[] f_orderedKeys = fields.keySet().toArray(new String[fields.size()]); //get the offset of previous parent class, continue from there on
            sz = fields.get(f_orderedKeys[f_orderedKeys.length - 1]) + DeclVisitor.sizeof(DeclVisitor.vardec.get(name).get(f_orderedKeys[f_orderedKeys.length - 1]));
        }
        return sz + 8;
    }
    public String convertTypes(String type){
        if("int".equals(type)){
            return "i32";
        }
        else if("int[]".equals(type)){
            return "%_IntegerArray*";

        }
        else if("boolean".equals(type)){
            return "i1";
        }
        else if("boolean[]".equals(type)){
            return "%_BooleanArray*";
        }
        else {
            return "i8*";
        }
    }
    public String convertParamTypes(String par){
        StringJoiner params = new StringJoiner(",");
        for (String p : par.split(",")) {
            params.add(convertTypes(p));
        }
        return params.toString();
    }

    public String newReg(String name,boolean var){
        if(name == null){
            return "%_"+rcounter++;
        }
        else if(!var){
            return "%"+name+rcounter++;
        }
        else {
            return "%"+name;
        }
    }

    /**
     * Grammar production:
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String argu) throws Exception {
        String classname = n.f1.accept(this,argu);
        rcounter = 0;
        fos.println("%_BooleanArray = type { i32, i1* }\n" +
                "%_IntegerArray = type { i32, i32* }");
        String contents;
        for(Map.Entry<String, ArrayList<MethInfo>> e : DeclVisitor.vtables.entrySet()) {
            StringJoiner tb = new StringJoiner(",");
            int size;
            if (e.getValue()!=null){
                for (MethInfo mi : e.getValue()) {
                    tb.add(String.format("i8* bitcast (%s (i8*,%s)* @%s to i8*)", convertTypes(mi.type), convertParamTypes(mi.params), mi.name));
                }
                contents  = tb.toString();
                size = e.getValue().size();
            }
            else {
                contents = "";
                size = 0;
            }
            fos.printf("@.%s_vtable = global [%d x i8*] [%s]\n",e.getKey(),size,contents);
        }
        fos.printf("define i32 @main() {\n");
        n.f14.accept(this,classname+"::main");
        n.f15.accept(this,classname+"::main");
        fos.println("  ret i32 0\n}");
        return null;
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String scope) throws Exception {
        String classname = n.f1.accept(this,scope);
        n.f4.accept(this,classname);
        return null;
    }



    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String scope) throws Exception {
        rcounter = 0;
        String classname = scope;
        String methodname = n.f2.accept(this,scope);
        ArrayList<MethInfo> vtable = DeclVisitor.vtables.get(classname);
        String methodtype = convertTypes(DeclVisitor.methdec.get(classname).get(methodname));
        String params = n.f4.accept(this,scope);
        fos.printf("define %s @%s(i8* %%this, %s) {\n",methodtype,classname+"."+methodname,params);
        for(String param : params.split(",")){
            String paramname = param.split(" ")[1];
            String paramtype = param.split(" ")[0];
            String reg1 = newReg(paramname.replace("%.",""),true);
            fos.printf("  %s = alloca %s\n",reg1,paramtype);
            fos.printf("  store %s, %s* %s\n",param,paramtype,reg1);
        }
        n.f7.accept(this,scope+"::"+methodname);
        n.f8.accept(this,scope+"::"+methodname);
        String exp = n.f10.accept(this,scope+"::"+methodname);
        fos.printf("ret %s\n}\n",exp);
        return null;
    }

    public String visit(NodeListOptional n, String argu) throws Exception { //this is for returning NodeLists with "," delimiter
        StringJoiner nodes = new StringJoiner(",");
        for (Node node: n.nodes){
            nodes.add(node.accept(this, argu));
        }
        return nodes.toString();
    }
    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1.f0.present()) {
            ret += n.f1.accept(this, null);
        }
        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }
        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n,String argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        return type + " " + "%." + name;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n,String argu) throws Exception{
        String type = n.f0.accept(this,argu);
        if(DeclVisitor.classdec.containsKey(type)){
            type = "i8*";
        }
        String name = n.f1.f0.tokenImage;
        String reg = newReg(name,true);
        fos.printf("  %s = alloca %s\n",reg,type);
        return type+"* "+reg;
    }
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
//    public String visit(FormalParameter n,String argu) throws Exception{
//        String type = n.f0.accept(this, null);
//        String name = n.f1.accept(this, null);
//        return type + " " + name;
//    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String scope) throws Exception { //same thing as array assignment but without the array error checks
        String name = n.f0.accept(this,scope);
        String exp = n.f2.accept(this,scope); //returns string "type register"
//        String exptype = exp.split(" ")[0];
//        String expname = exp.split(" ")[1];
        String reg = loadVar(scope,name);
        fos.printf("  store %s, %s\n",exp,reg);
//        if(DeclVisitor.vardec.get(scope).containsKey(name)){
//            reg = newReg(name,true);
//            fos.printf("  store %s, %s* %s\n",exp,exptype,reg);
//        }
//        else{
//            int offset = DeclVisitor.
//        }
        return null;
    }

    //Expressions
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String scope) throws Exception{
        String lexp = n.f0.accept(this,scope).split(" ")[1];
        String rexp = n.f2.accept(this,scope).split(" ")[1];
        String reg = newReg(null,false);
        fos.printf("  %s = add i32 %s, %s\n",reg,lexp,rexp);
        return "i32 "+reg;
    }
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String scope) throws Exception{
        String lexp = n.f0.accept(this,scope).split(" ")[1];
        String rexp = n.f2.accept(this,scope).split(" ")[1];
        String reg = newReg(null,false);
        fos.printf("  %s = sub i32 %s, %s\n",reg,lexp,rexp);
        return "i32 "+reg;
    }
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String scope) throws Exception{ //same as plus
        String lexp = n.f0.accept(this,scope).split(" ")[1];
        String rexp = n.f2.accept(this,scope).split(" ")[1];
        String reg = newReg(null,false);
        fos.printf("  %s = mul i32 %s, %s\n",reg,lexp,rexp);
        return "i32 "+reg;
    }
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String scope) throws Exception{
        return null;
    }
    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String scope) throws Exception{
        return null;
    }

    /**
     * Grammar production:
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String scope) throws Exception{
        String exp = n.f2.accept(this,scope).split(" ")[1];
        fos.printf("  call void (i32) @print_int(%s)\n",exp);
        return null;
    }
    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String scope)throws Exception{ //same as &&
       return null;
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(BooleanArrayAllocationExpression n, String scope) throws Exception {
        String exp = n.f3.accept(this,scope);
        String reg1 = newReg(null,false);
        fos.printf("  %s = icmp slt %s, 0\n",reg1,exp);
        String label1 = newReg("arr_alloc",false);
        String label2 = newReg("arr_alloc",false);
        fos.printf("  br i1 %s, label %s, label %s\n",reg1,label1,label2);
        fos.printf("\n%s:\n  call void @throw_oob()\n  br label %s\n",label1,label2);
        String reg2 = newReg(null,false);
        fos.printf("\n%s:\n  %s = call i8* @calloc(i32 1,i32 %d)\n",label2,reg2,arraytype_size);
        String reg3 = newReg(null,false); //register with booleanarray pointer
        fos.printf("  %s = bitcast i8* %s to %%_BooleanArray*\n",reg3,reg2);
        String reg4 = newReg(null,false);
        fos.printf("  %s = getelementptr %%_BooleanArray, %%_BooleanArray* %s, i32 0, i32 0\n",reg4,reg3); // get pointer to array size
        fos.printf("  store %s, i32* %s\n",exp,reg4);
        String reg5 = newReg(null,false);
        fos.printf("  %s = call i8* @calloc(%s, i32 1)\n",reg5,exp);
        String reg6 = newReg(null,false);
        fos.printf("  %s = bitcast i8* %s to i1*\n",reg6,reg5);
        String reg7 = newReg(null,false);
        fos.printf("  %s = getelementptr %%_BooleanArray, %%_BooleanArray* %s, i32 0, i32 1\n",reg7,reg3);
        fos.printf("  store i1* %s, i1** %s\n",reg5,reg6);
        return "%_BooleanArray* "+reg3;
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n,String scope) throws Exception {
        String exp = n.f3.accept(this,scope);
        String reg1 = newReg(null,false);
        fos.printf("  %s = icmp slt %s, 0\n",reg1,exp);
        String label1 = newReg("arr_alloc",false);
        String label2 = newReg("arr_alloc",false);
        fos.printf("  br i1 %s, label %s, label %s\n",reg1,label1,label2);
        fos.printf("\n%s:\n  call void @throw_oob()\n  br label %s\n",label1,label2);
        String reg2 = newReg(null,false);
        fos.printf("\n%s:\n  %s = call i8* @calloc(i32 1,i32 %d)\n",label2,reg2,arraytype_size);
        String reg3 = newReg(null,false); //register with integerarray pointer
        fos.printf("  %s = bitcast i8* %s to %%_IntegerArray*\n",reg3,reg2);
        String reg4 = newReg(null,false);
        fos.printf("  %s = getelementptr %%_IntegerArray, %%_IntegerArray* %s, i32 0, i32 0\n",reg4,reg3); // get pointer to array size
        fos.printf("  store %s, i32* %s",exp,reg4);
        String reg5 = newReg(null,false);
        fos.printf("  %s = call i8* @calloc(%s, i32 4)\n",reg5,exp);
        String reg6 = newReg(null,false);
        fos.printf("  %s = bitcast i8* %s to i32*\n",reg6,reg5);
        String reg7 = newReg(null,false);
        fos.printf("  %s = getelementptr %%_IntegerArray, %%_IntegerArray* %s, i32 0, i32 1\n",reg7,reg3);
        fos.printf("  store i32* %s, i32** %s\n",reg5,reg6);
        return "%_IntegerArray* "+reg3;
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String scope) throws Exception {
        String name = n.f1.accept(this,scope);
        int size = getClassSize(name);
        String classname = scope.split("::")[0];
        ArrayList<MethInfo> vt = DeclVisitor.vtables.get(name);
        String reg1 = newReg(null,false);
        fos.printf("  %s = call i8* @calloc(i32 1, i32 %d)\n",reg1,size);
        String reg2 = newReg(null,false);
        fos.printf("  %s = bitcast i8* %s to i8***\n",reg2,reg1);
        String reg3 = newReg(null,false);
//        System.out.println(name);
        fos.printf("  %s = getelementptr [%d x i8*], [%d x i8*]* @.%s_vtable, i32 0, i32 0\n",reg3,vt.size(),vt.size(),name);
        fos.printf("  store i8** %s, i8*** %s\n",reg3,reg2);
        return String.format("i8* %s",reg1);
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String scope) throws Exception{ //check for array length
        String arr = n.f0.accept(this,scope);
        String arrtype = arr.split(" ")[0];
        String arrname = arr.split(" ")[1];
        fos.printf("  %s = getelementptr %s %s* i32 0\n");
        return null;
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String scope) throws Exception{

        return null;
    }

    /**
     * Grammar production:
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String scope) throws Exception{
        String pexp = n.f0.accept(this,scope);
        if(pexp.split(" ").length == 1){
            String rtype = loadVar(scope,pexp).split(" ")[0];
            String rname = loadVar(scope,pexp).split(" ")[1];
            String reg = newReg(null,false);
            System.out.println(rtype);
            fos.printf("  %s = load %s, %s %s\n",reg,rtype.substring(0,rtype.length()-1),rtype,rname);
            return rtype+" "+reg;
        }
        return pexp;
    }

    private String loadVar(String scope, String pexp) {
        String type;
        type = convertTypes(DeclVisitor.vardec.get(scope).get(pexp));
        if(type == null) {
            String classname = scope.split("::")[0];
            while (classname != null && DeclVisitor.vardec.containsKey(classname)) {
                type = DeclVisitor.vardec.get(classname).get(pexp);
                if (type != null) {
                    type = convertTypes(type);
                    int offset = DeclVisitor.fieldoffsets.get(classname).get(pexp);
                    String reg1 = newReg(null,false);
                    fos.printf("%s = getelementptr i8, i8* %%this, i32 %d\n",reg1,8+offset);
                    String reg2 = newReg(null,false);
                    fos.printf("%s = bitcast i8* %s to %s*",reg2,reg1,type);
                    return type+"* "+reg2;
                }
                classname = DeclVisitor.classdec.get(classname);
            }
        }
        return type + "* "+newReg(pexp,true);
    }

    public String visit(BracketExpression n, String scope) throws Exception{
        return n.f1.accept(this,scope);
    }

    public String visit(IntegerLiteral n, String scope){
        return  "i32 " + n.f0.tokenImage;
    }

    public String visit(TrueLiteral n, String scope){
        return  "i1 1";
    }

    public String visit(FalseLiteral n, String scope){
        return  "i1 0";
    }
    public String visit(BooleanArrayType n,String argu) {
        return "%_BooleanArray*";
    }
    @Override
    public String visit(IntegerArrayType n,String argu) {
        return "%_IntegerArray*";
    }

    public String visit(BooleanType n, String argu) {
        return "i1";
    }


    public String visit(IntegerType n, String argu) {
        return "i32";
    }


    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}