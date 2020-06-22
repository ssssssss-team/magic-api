package org.ssssssss.script;

import org.ssssssss.script.parsing.Span;
import org.ssssssss.script.parsing.TokenStream;

/**
 * All errors reported by the library go through the static functions of this class.
 */
public class MagicScriptError {

    /**
     * <p>
     * Create an error message based on the provided message and stream, highlighting the line on which the error happened. If the
     * stream has more tokens, the next token will be highlighted. Otherwise the end of the source of the stream will be
     * highlighted.
     * </p>
     *
     * <p>
     * Throws a {@link RuntimeException}
     * </p>
     */
    public static void error(String message, TokenStream stream) {
        if (stream.hasMore()) {
            error(message, stream.consume().getSpan());
        } else {
            error(message, stream.getPrev().getSpan());
        }
    }

    /**
     * Create an error message based on the provided message and location, highlighting the location in the line on which the
     * error happened. Throws a {@link ScriptException}
     **/
    public static void error(String message, Span location, Throwable cause) {

        Span.Line line = location.getLine();
        message = "Error (" + line.getLineNumber() + "): " + message + "\n\n";
        message += line.getText();
        message += "\n";

        int errorStart = location.getStart() - line.getStart();
        int errorEnd = errorStart + location.getText().length() - 1;
        for (int i = 0, n = line.getText().length(); i < n; i++) {
            boolean useTab = line.getText().charAt(i) == '\t';
            message += i >= errorStart && i <= errorEnd ? "^" : useTab ? "\t" : " ";
        }

        if (cause == null) {
            throw new ScriptException(message);
        } else {
            throw new ScriptException(message, cause);
        }
    }

    /**
     * Create an error message based on the provided message and location, highlighting the location in the line on which the
     * error happened. Throws a {@link ScriptException}
     **/
    public static void error(String message, Span location) {
        error(message, location, null);
    }

    public static class ScriptException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String errorMessage;

        public ScriptException(String message) {
            super(message);
            this.errorMessage = message;
        }

        public ScriptException(String message, Throwable cause) {
            super(message, cause);
            this.errorMessage = message;
        }


        @Override
        public String getMessage() {
            StringBuilder builder = new StringBuilder();

            if (getCause() == null || getCause() == this) {
                return super.getMessage();
            }

            builder.append(errorMessage, 0, errorMessage.indexOf('\n'));
            builder.append("\n");

            Throwable cause = getCause();
            while (cause != null && cause != this) {
                if (cause instanceof ScriptException) {
                    ScriptException ex = (ScriptException) cause;
                    if (ex.getCause() == null || ex.getCause() == ex) {
                        builder.append(ex.errorMessage);
                    } else {
                        builder.append(ex.errorMessage, 0, ex.errorMessage.indexOf('\n'));
                    }
                    builder.append("\n");
                }
                cause = cause.getCause();
            }
            return builder.toString();
        }
    }

    public static class StringLiteralException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }
}
