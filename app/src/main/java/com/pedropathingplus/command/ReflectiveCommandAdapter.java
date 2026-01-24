package com.pedropathingplus.command;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * Wraps an arbitrary object and attempts to use it as a Command by reflectively calling
 * standard lifecycle methods: initialize, execute, isFinished, end, getRequirements.
 */
public class ReflectiveCommandAdapter implements Command {
    private final Object target;

    private Method initializeMethod;
    private Method executeMethod;
    private Method isFinishedMethod;
    private Method endMethod; // end(boolean)
    private Method endNoArgMethod; // end()
    private Method getRequirementsMethod;

    public ReflectiveCommandAdapter(Object target) {
        if (target == null) throw new IllegalArgumentException("Target object cannot be null");
        this.target = target;

        Class<?> clazz = target.getClass();

        try { initializeMethod = clazz.getMethod("initialize"); } catch (NoSuchMethodException ignored) {}
        try { executeMethod = clazz.getMethod("execute"); } catch (NoSuchMethodException ignored) {}
        try { isFinishedMethod = clazz.getMethod("isFinished"); } catch (NoSuchMethodException ignored) {}
        try { endMethod = clazz.getMethod("end", boolean.class); } catch (NoSuchMethodException ignored) {}
        try { endNoArgMethod = clazz.getMethod("end"); } catch (NoSuchMethodException ignored) {}
        try { getRequirementsMethod = clazz.getMethod("getRequirements"); } catch (NoSuchMethodException ignored) {}
    }

    @Override
    public void initialize() {
        if (initializeMethod != null) {
            try {
                initializeMethod.invoke(target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void execute() {
        if (executeMethod != null) {
            try {
                executeMethod.invoke(target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isFinished() {
        if (isFinishedMethod != null) {
            try {
                return (boolean) isFinishedMethod.invoke(target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        try {
            if (endMethod != null) {
                endMethod.invoke(target, interrupted);
            } else if (endNoArgMethod != null) {
                endNoArgMethod.invoke(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Object> getRequirements() {
        if (getRequirementsMethod != null) {
            try {
                Object result = getRequirementsMethod.invoke(target);
                if (result instanceof Set) {
                    return (Set<Object>) result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Adapter(" + target.getClass().getSimpleName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReflectiveCommandAdapter that = (ReflectiveCommandAdapter) o;
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
