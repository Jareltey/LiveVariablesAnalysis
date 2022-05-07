package hw5;

import common.ErrorMessage;
import common.Utils;
import fj.P;
import soot.Local;
import soot.Unit;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LiveVariablesAnalysis extends BackwardFlowAnalysis<Unit, Sigma> {
    // Holds the set of local variables
    private Set<Local> locals = new HashSet<>();

    // The calling context for the analysis
    // Null if no context (e.g., when only running intraprocedurally)
    private Context ctx;

    // The input sigma for this analysis
    private Sigma sigma_i;

    /**
     * Constructor with no context. This is useful for testing the intraprocedural
     * analysis on its own.
     */
    LiveVariablesAnalysis(UnitGraph graph) {
        // Note the construction of a default Sigma
        this(graph, null, null);
    }

    /**
     * Allows creating an intra analysis with just the context and the input sigma,
     * since the unit graph can be grabbed from the function in the context.
     */
    LiveVariablesAnalysis(Context ctx, Sigma sigma_i) {
        this(new ExceptionalUnitGraph(ctx.fn.getActiveBody()), ctx, sigma_i);
    }

    LiveVariablesAnalysis(UnitGraph graph, Context ctx, Sigma sigma_i) {
        super(graph);
        this.ctx = ctx;
        this.sigma_i = sigma_i;

        // Collect locals
        this.locals.addAll(graph.getBody().getLocals());
    }

    // Runs the analysis
    public void run() {
        this.doAnalysis();
    }

    protected void handle_referenced_var(Sigma sigmaBefore, Local var, Unit u) {
        Utils.reportWarning(u, ErrorMessage.VARIABLE_REFERENCED_WARNING);
        if (!sigmaBefore.live_variables.contains(var)) {
            Utils.reportWarning(u, ErrorMessage.VARIABLE_REFERENCED_ERROR);
        }
    }

    public void reportWarnings() {
        for (Unit u : this.graph) {
            Sigma sigmaBefore = this.getFlowBefore(u);
            Stmt stmt = (Stmt) u;
            if (stmt instanceof AssignStmt) {
                AssignStmt assign_stmt = (AssignStmt) stmt;
                Local var = (Local) assign_stmt.getLeftOp(); // should kill var in getflow Before
                Utils.reportWarning(u, ErrorMessage.VARIABLE_DEFINITION_WARNING);
                if (sigmaBefore.live_variables.contains(var)) {
                    Utils.reportWarning(u, ErrorMessage.VARIABLE_DEFINITION_ERROR);
                }
                soot.Value expr = assign_stmt.getRightOp();
                if (expr instanceof Local) {
                    handle_referenced_var(sigmaBefore, (Local)expr, u);
                } else if (expr instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr)expr;
                    soot.Value op1 = binop.getOp1();
                    soot.Value op2 = binop.getOp2();
                    if (op1 instanceof Local) {
                        handle_referenced_var(sigmaBefore, (Local)op1, u);
                    } else if (op2 instanceof Local) {
                        handle_referenced_var(sigmaBefore, (Local)op2, u);
                    }
                } else if (expr instanceof ArrayRef) {
                    ArrayRef arr_expr = (ArrayRef) expr;
                    soot.Value index = arr_expr.getIndex();
                    soot.Value base = arr_expr.getBase();
                    if (index instanceof Local) {
                        handle_referenced_var(sigmaBefore, (Local)index, u);
                    } else if (base instanceof Local) {
                        handle_referenced_var(sigmaBefore, (Local)base, u);
                    }

                }
            } else if (stmt instanceof ReturnStmt) {
                ReturnStmt ret_stmt = (ReturnStmt)stmt;
                soot.Value ret_var = ret_stmt.getOp();
                if (ret_var instanceof Local) {
                    handle_referenced_var(sigmaBefore, (Local)ret_var, u);
                }
            }
        }
    }

    protected void kill(Sigma res, Local var) {
        res.live_variables.remove(var);
    }

    protected void gen(Sigma res, Local var) {
        res.live_variables.add(var);
    }

    protected void handle_binop(Sigma res, BinopExpr expr) {
        soot.Value op1 = expr.getOp1();
        soot.Value op2 = expr.getOp2();
        if (op1 instanceof Local) {
            gen(res, (Local) op1);
        }
        if (op2 instanceof Local) {
            gen(res, (Local) op2);
        }
    }

    /**
     * Run flow function for this unit
     *
     * @param inValue  The initial Sigma at this point
     * @param unit     The current Unit
     * @param outValue The updated Sigma Before the flow function
     */
    @Override
    protected void flowThrough(Sigma inValue, Unit unit, Sigma outValue) {
        this.copy(inValue, outValue);
        Stmt stmt = (Stmt) unit;
        if (stmt instanceof AssignStmt) {
            AssignStmt assign_stmt = (AssignStmt) stmt;
            Local var = (Local) assign_stmt.getLeftOp();
            kill(outValue, var);
            soot.Value expr = assign_stmt.getRightOp();
            if (expr instanceof Local) {
                gen(outValue, (Local)expr);
            } else if (expr instanceof BinopExpr) {
                handle_binop(outValue, (BinopExpr)expr);
            } else if (expr instanceof NewArrayExpr) {
                NewArrayExpr new_arr_expr = (NewArrayExpr) expr;
                soot.Value size = new_arr_expr.getSize();
                if (size instanceof Local) {
                    gen(outValue, (Local)size);
                }
            } else if (expr instanceof ArrayRef) {
                ArrayRef array_ref = (ArrayRef)expr;
                soot.Value base = array_ref.getBase();
                soot.Value index = array_ref.getIndex();
                if (base instanceof Local) {
                    gen(outValue, (Local)base);
                }
                if (index instanceof Local) {
                    gen(outValue, (Local)index);
                }
            }
        } else if (stmt instanceof IfStmt) {
            soot.Value condition = ((IfStmt)stmt).getCondition();
            ConditionExpr cond_expr = (ConditionExpr)condition;
            soot.Value op1 = cond_expr.getOp1();
            soot.Value op2 = cond_expr.getOp2();
            if (op1 instanceof Local) {
                gen(outValue, (Local) op1);
            }
            if (op2 instanceof Local) {
                gen(outValue, (Local) op2);
            }
        } else if (stmt instanceof GotoStmt) { // don't update sigma_out for GotoStmt
            ;
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt ret_stmt = (ReturnStmt)stmt;
            soot.Value expr = ret_stmt.getOp();
            if (expr instanceof Local) {
                gen(outValue, (Local)expr);
            } else if (expr instanceof BinopExpr) {
                handle_binop(outValue, (BinopExpr)expr);
            }
        }
    }

    /**
     * Initial flow information at the start of a method
     */
    @Override
    protected Sigma entryInitialFlow() {
        if (this.sigma_i != null) {
            return this.sigma_i;
        } else {
            return new Sigma();
        }
    }

    /**
     * Initial flow information at each other program point
     */
    @Override
    protected Sigma newInitialFlow() {
        return new Sigma();
    }

    /**
     * Join at a program point lifted to sets
     */
    @Override
    protected void merge(Sigma in1, Sigma in2, Sigma out) {
        for (Local var : in1.live_variables) {
            out.live_variables.add(var);
        }
        for (Local var: in2.live_variables) {
            out.live_variables.add(var);
        }
    }

    /**
     * Copy for sets
     */
    @Override
    protected void copy(Sigma source, Sigma dest) {
        for (Local var : source.live_variables) {
            dest.live_variables.add(var);
        }
    }
}
