/**
 * @author bosong
 * @date 2019-09-12
 */

public interface PusherResult {

    Code code();

    String msg();

    enum Code {
        SUCCESS("SUCCESS"),
        FAIL("FAIL"),
        CONNECTION_ERROR("CONNECTION_ERROR"),
        REQUEST_FAIL("REQUEST_FAIL");

        private String value;

        Code(String value) {
            this.value = value;
        }
    }
}
