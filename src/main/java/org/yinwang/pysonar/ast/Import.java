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
            Type mod = Analyzer.self.loadModule(a.name, s);
            if (mod == null) {
                Analyzer.self.putProblem(this, "Cannot load module");
                continue;
            }
            if (a.asname != null) {
                // As-name binding
                // Note: this won't be outputted by JSONDump, because it has same key as original definition.
                // Alternatively, we could treat this as its own definition, but instead, treat this as just another
                // reference to the original definition (all references to this as-name point to the original
                // definition, too)
                s.insert(a.asname.id, a.asname, mod, Binding.Kind.VARIABLE);
            }
            // module reference
            if (mod instanceof ModuleType) {
                Set<Binding> bs = Analyzer.self.moduleTable.lookup(((ModuleType)mod).qname);
                if (bs != null) {
                    // As-name
                    if (a.asname != null) {
                        Analyzer.self.putRef(a.asname, bs);
                    }

                    // Last-name
                    Analyzer.self.putRef(a.name.get(a.name.size()-1), bs);
                    // TODO: do previous names
                }
            }
        }
        return Type.CONT;
    }


    @NotNull
    @Override
    public String toString() {
        return "<Import:" + names + ">";
    }

}
