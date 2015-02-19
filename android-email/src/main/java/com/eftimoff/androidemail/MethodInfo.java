package com.eftimoff.androidemail;

import com.eftimoff.androidemail.annotations.Bcc;
import com.eftimoff.androidemail.annotations.Cc;
import com.eftimoff.androidemail.annotations.Email;
import com.eftimoff.androidemail.annotations.Param;
import com.eftimoff.androidemail.annotations.Sender;
import com.eftimoff.androidemail.annotations.Subject;
import com.eftimoff.androidemail.annotations.To;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodInfo {

    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    private static final Pattern PARAM_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");


    private final Method method;
    private ExecutionType executionType;
    private Set<String> pathParameters = new HashSet<String>();
    private Annotation[] requestParamAnnotations;
    private Email emailAnnotation;
    private Subject subjectAnnotation;
    private Sender senderAnnotation;
    private To toAnnotation;
    private Cc ccAnnotation;
    private Bcc bccAnnotation;

    enum ExecutionType {
        ASYNC
    }


    public MethodInfo(final Method method) {
        this.method = method;
        parseResponseType();
        parseAnnotations();
        parseParameters();
    }

    private void parseResponseType() {
        final Type returnType = method.getGenericReturnType();

        // Asynchronous methods should have a Callback type as the last argument.
        Class<?> lastArgClass = null;
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length > 0) {
            Type typeToCheck = parameterTypes[parameterTypes.length - 1];
            if (typeToCheck instanceof ParameterizedType) {
                typeToCheck = ((ParameterizedType) typeToCheck).getRawType();
            }
            if (typeToCheck instanceof Class) {
                lastArgClass = (Class<?>) typeToCheck;
            }
        }

        boolean hasReturnType = returnType != void.class;
        boolean hasCallback = lastArgClass != null && Callback.class.isAssignableFrom(lastArgClass);
        // Check for invalid configurations.
        if (hasReturnType) {
            throw methodError("Must have return type void.");
        }
        if (!hasCallback) {
            throw methodError("Must have Callback as last argument.");
        }

        setExecutionType(ExecutionType.ASYNC);
    }

    private void parseAnnotations() {
        final Annotation[] annotations = method.getAnnotations();
        for (final Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == Email.class) {
                final Email emailAnnotation = (Email) annotation;
                final String annotationValue = emailAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.emailAnnotation = emailAnnotation;
            } else if (annotationType == Subject.class) {
                final Subject subjectAnnotation = (Subject) annotation;
                final String annotationValue = subjectAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.subjectAnnotation = subjectAnnotation;
            } else if (annotationType == Sender.class) {
                final Sender senderAnnotation = (Sender) annotation;
                final String annotationValue = senderAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.senderAnnotation = senderAnnotation;
            } else if (annotationType == To.class) {
                final To toAnnotation = (To) annotation;
                final String annotationValue = toAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.toAnnotation = toAnnotation;
            } else if (annotationType == Cc.class) {
                final Cc ccAnnotation = (Cc) annotation;
                final String annotationValue = ccAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.ccAnnotation = ccAnnotation;
            } else if (annotationType == Bcc.class) {
                final Bcc bccAnnotation = (Bcc) annotation;
                final String annotationValue = bccAnnotation.value();
                parseAnnotationValue(annotationValue);
                this.bccAnnotation = bccAnnotation;
            }
        }
    }

    private void parseAnnotationValue(final String annotationValue) {
        if (annotationValue == null || annotationValue.length() == 0) {
            throw methodError("Email value must not be empty : \"%s\".", annotationValue);
        }

        parsePathParameters(annotationValue);
    }

    private void parseParameters() {
        final Type[] methodParameterTypes = method.getGenericParameterTypes();

        final Annotation[][] methodParameterAnnotationArrays = method.getParameterAnnotations();
        int count = methodParameterAnnotationArrays.length;
        if (executionType == ExecutionType.ASYNC) {
            count -= 1; // Callback is last argument when not a synchronous method.
        }

        final Annotation[] requestParamAnnotations = new Annotation[count];


        for (int i = 0; i < count; i++) {
            final Type methodParameterType = methodParameterTypes[i];
            final Annotation[] methodParameterAnnotations = methodParameterAnnotationArrays[i];
            for (Annotation methodParameterAnnotation : methodParameterAnnotations) {
                final Class<? extends Annotation> methodAnnotationType = methodParameterAnnotation.annotationType();
                if (methodAnnotationType == Param.class) {
                    final String annotationValue = ((Param) methodParameterAnnotation).value();
                    validateParamName(i, annotationValue);
                }
                requestParamAnnotations[i] = methodParameterAnnotation;
            }
            if (requestParamAnnotations[i] == null) {
                throw parameterError(i, "All parameters must have @Param annotation.");
            }
        }
        this.requestParamAnnotations = requestParamAnnotations;
    }

    private void validateParamName(final int index, final String name) {
        if (!PARAM_NAME_REGEX.matcher(name).matches()) {
            throw parameterError(index, "@Param parameter name must match %s. Found: %s",
                    PARAM_REGEX.pattern(), name);
        }
        // Verify URL replacement name is actually present in the URL path.
        if (!pathParameters.contains(name)) {
            throw parameterError(index, "Email \"%s\" does not contain \"{%s}\".", emailAnnotation.value(), name);
        }
    }


    /**
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    private void parsePathParameters(final String path) {
        final Matcher m = PARAM_REGEX.matcher(path);
        while (m.find()) {
            pathParameters.add(m.group(1));
        }
    }

    private RuntimeException methodError(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        return new IllegalArgumentException(
                method.getDeclaringClass().getSimpleName() + "." + method.getName() + ": " + message);
    }

    private RuntimeException parameterError(int index, String message, Object... args) {
        return methodError(message + " (parameter #" + (index + 1) + ")", args);
    }


    public ExecutionType getExecutionType() {
        return executionType;
    }

    public void setExecutionType(ExecutionType executionType) {
        this.executionType = executionType;
    }

    public Email getEmailAnnotation() {
        return emailAnnotation;
    }

    public Subject getSubjectAnnotation() {
        return subjectAnnotation;
    }

    public Sender getSenderAnnotation() {
        return senderAnnotation;
    }

    public To getToAnnotation() {
        return toAnnotation;
    }

    public Cc getCcAnnotation() {
        return ccAnnotation;
    }

    public Bcc getBccAnnotation() {
        return bccAnnotation;
    }

    public Set<String> getPathParameters() {
        return pathParameters;
    }


    public Method getMethod() {
        return method;
    }


    public Annotation[] getRequestParamAnnotations() {
        return requestParamAnnotations;
    }


    @Override
    public String toString() {
        return "MethodInfo{" +
                "\nmethod=" + method +
                ", \nexecutionType=" + executionType +
                ", \npathParameters=" + pathParameters +
                ", \nrequestParamAnnotations=" + Arrays.toString(requestParamAnnotations) +
                ", \nemailAnnotation=" + emailAnnotation +
                ", \nsubjectAnnotation=" + subjectAnnotation +
                ", \nsenderAnnotation=" + senderAnnotation +
                '}';
    }
}
