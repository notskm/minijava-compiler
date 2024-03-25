package minijava;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, ClassBinding> classes = new HashMap<>();

    public boolean addClass(String name) {
        if (classes.containsKey(name)) {
            return false;
        }

        ClassBinding binding = new ClassBinding();
        binding.name = name;
        binding.table = this;
        classes.put(name, binding);
        return true;
    }

    public ClassBinding getClassBinding(String name) {
        return classes.get(name);
    }

    public class ClassBinding {
        private SymbolTable table;

        private String name;
        private Map<String, String> fields = new HashMap<>();
        private Map<String, MethodBinding> methods = new HashMap<>();
        private String baseClass = "";

        public String getName() {
            return name;
        }

        public boolean addField(String name, String type) {
            if (fields.containsKey(name)) {
                return false;
            }

            fields.put(name, type);
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

        private boolean hasVariable(String name) {
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
