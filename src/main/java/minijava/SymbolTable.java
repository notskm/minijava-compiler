package minijava;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;

public class SymbolTable {
    private Map<String, ClassBinding> classes = new HashMap<>();
    private List<String> classesInOrder = new LinkedList<>();

    public boolean addClass(String name) {
        if (classes.containsKey(name)) {
            return false;
        }

        ClassBinding binding = new ClassBinding();
        binding.name = name;
        binding.table = this;
        classes.put(name, binding);
        classesInOrder.add(name);
        return true;
    }

    public ClassBinding getClassBinding(String name) {
        return classes.get(name);
    }

    public Map<String, List<String>> getMethodTables() {
        Map<String, List<String>> tables = new LinkedHashMap<>();

        for (String className : classesInOrder) {
            if (getClassBinding(className).methods.get("main") != null) {
                continue;
            }
            List<String> methodTable = getMethodTable(className);
            tables.put(className, methodTable);
        }

        return tables;
    }

    private List<String> getMethodTable(String className) {
        if (className == "") {
            return new ArrayList<>();
        }

        final ClassBinding binding = getClassBinding(className);

        List<String> baseMethodTable = getMethodTable(binding.baseClass);
        List<String> thisMethodTable = new ArrayList<>();

        for (String methodName : binding.methodsInOrder) {
            final String name = methodName.substring(0, methodName.length() - 2);
            final String derivedMethod = binding.name + "." + name;
            baseMethodTable.replaceAll(str -> str.endsWith("." + name) ? derivedMethod : str);
            if (!baseMethodTable.contains(derivedMethod)) {
                thisMethodTable.add(derivedMethod);
            }
        }

        baseMethodTable.addAll(thisMethodTable);

        return baseMethodTable;
    }

    public class ClassBinding {
        private SymbolTable table;

        private String name;
        private Map<String, String> fields = new HashMap<>();
        private Map<String, Integer> fieldOrder = new HashMap<>();
        private Map<String, MethodBinding> methods = new HashMap<>();
        private List<String> methodsInOrder = new ArrayList<>();
        private String baseClass = "";

        public int getFieldOffset(String field) {
            final Integer fieldIndex = fieldOrder.get(field);
            if (fieldIndex != null) {
                final ClassBinding base = table.getClassBinding(baseClass);
                final int baseSize = base != null ? base.getSizeInBytes() : 0;
                return baseSize + fieldIndex * 4 + 4;
            }

            if (baseClass == "") {
                return -1;
            }

            return table.getClassBinding(baseClass).getFieldOffset(field);
        }

        public Collection<MethodBinding> getMethods() {
            return methods.values();
        }

        public String getName() {
            return name;
        }

        public boolean addField(String name, String type) {
            if (fields.containsKey(name)) {
                return false;
            }

            fields.put(name, type);
            fieldOrder.put(name, fields.size() - 1);
            return true;
        }

        public String getFieldType(String name) {
            return fields.get(name);
        }

        public boolean addMethod(String name) {
            if (methods.containsKey(name)) {
                return false;
            }

            MethodBinding method = new MethodBinding();
            method.classBinding = this;
            method.name = name;
            methods.put(name, method);
            methodsInOrder.add(name);
            return true;
        }

        public MethodBinding getMethod(String name) {
            MethodBinding method = methods.get(name);
            if (method != null) {
                return method;
            } else {
                ClassBinding parent = table.classes.get(baseClass);
                return parent != null ? parent.getMethod(name) : null;
            }
        }

        public int getSizeInBytes() {
            final int thisSize = fields.size();

            ClassBinding baseClassBinding = table.getClassBinding(baseClass);
            if (baseClassBinding != null) {
                return thisSize * 4 + baseClassBinding.getSizeInBytes();
            } else {
                return thisSize * 4;
            }
        }

        public void setBaseClass(String name) {
            baseClass = name;
        }

        public String getBaseClass() {
            return baseClass;
        }

        public String lookup(String name) {
            String type = null;
            type = fields.get(name);

            if (type != null) {
                return type;
            }

            MethodBinding method = methods.get(name);
            if (method != null) {
                type = method.getType();
            }

            if (type != null) {
                return type;
            }

            ClassBinding baseClassBinding = table.getClassBinding(baseClass);
            if (baseClassBinding != null) {
                return baseClassBinding.lookup(name);
            } else {
                return null;
            }
        }
    }

    public class MethodBinding {
        private ClassBinding classBinding;

        private String name;
        private String returnType = "";
        private Map<String, String> parameters = new LinkedHashMap<>();
        private Map<String, String> localVariables = new HashMap<>();

        public boolean addParameter(String name, String type) {
            return add(name, type, parameters);
        }

        public boolean addLocalVariable(String name, String type) {
            return add(name, type, localVariables);
        }

        private boolean add(String name, String type, Map<String, String> map) {
            if (hasVariable(name)) {
                return false;
            }

            map.put(name, type);
            return true;
        }

        public boolean hasVariable(String name) {
            if (localVariables.containsKey(name)) {
                return true;
            }

            if (parameters.containsKey(name)) {
                return true;
            }

            return false;
        }

        public String getVariableType(String name) {
            String type = localVariables.get(name);

            if (type == null) {
                type = parameters.get(name);
            }

            return type;
        }

        public void setReturnType(String type) {
            returnType = type;
        }

        public String getReturnType() {
            return returnType;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            String type = "(";

            Iterator<String> types = parameters.values().iterator();
            if (types.hasNext()) {
                type += types.next();
            }

            while (types.hasNext()) {
                type += ", " + types.next();
            }

            type += ") -> " + getReturnType();

            return type;
        }

        public String lookup(String name) {
            String type = null;
            type = localVariables.get(name);

            if (type != null) {
                return type;
            }

            type = parameters.get(name);

            if (type != null) {
                return type;
            }

            return classBinding.lookup(name);
        }
    }
}
