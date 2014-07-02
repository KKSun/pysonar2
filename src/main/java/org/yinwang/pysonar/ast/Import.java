package org.yinwang.pysonar.ast;

import org.jetbrains.annotations.NotNull;
import org.yinwang.pysonar.Analyzer;
import org.yinwang.pysonar.Binding;
import org.yinwang.pysonar.State;
import org.yinwang.pysonar.types.Type;
import org.yinwang.pysonar.types.ModuleType;

import java.util.List;
import java.util.Set;


public class Import extends Node {

    public List<Alias> names;


    public Import(List<Alias> names, String file, int start, int end) {
        super(file, start, end);
        this.names = names;
        addChildren(names);
    }


    @NotNull
    @Override
    public Type transform(@NotNull State s) {
        for (Alias a : names) {
            // module and ancestor modules
            for (int i = 1; i <= a.name.size(); i++) {
                List<Name> qname = a.name.subList(0, i);
                if (a.name.size() == qname.size()) { // when we're dealing with the full module name
                    handleModuleName(this, qname, s, a.asname, true);
                } else {
                    handleModuleName(this, qname, s, null, false);
                }
            }
        }
        return Type.CONT;
    }

    public static void handleModuleName(Node node, List<Name> qname, State s, Name asname, boolean must) {
        Type mod = Analyzer.self.loadModule(qname, s);
        if (mod == null) {
            if (must) {
                Analyzer.self.putProblem(node, "Cannot load module");
            }
            return;
        }
        if (asname != null) {
            // As-name binding Note: this won't be outputted by JSONDump, because it has same key as
            // original definition. Alternatively, we could treat this as its own definition, but instead,
            // treat this as just another reference to the original definition (all references to this
            // as-name point to the original definition, too)
            s.insert(asname.id, asname, mod, Binding.Kind.VARIABLE);
        }

        // module reference
        if (mod instanceof ModuleType) {
            Set<Binding> bs = Analyzer.self.moduleTable.lookup(((ModuleType)mod).qname);
            if (bs != null) {
                if (asname != null) {
                    // As-name
                    Analyzer.self.putRef(asname, bs);
                }

                // Name
                Analyzer.self.putRef(qname.get(qname.size()-1), bs);
            }
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "<Import:" + names + ">";
    }

}
