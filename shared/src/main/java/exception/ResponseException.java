package exception;

public class ResponseException extends Exception {

    public enum Code {
        ClientError,
        ServerError
    }

    private final Code code;

    public ResponseException(Code code, String message) {

        super(message);
        this.code = code;
    }


    public Code getCode() {
        return code;
    }




    public static Code fromHttpStatusCode(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return Code.ClientError;
        }

        return Code.ServerError;
    }
}
