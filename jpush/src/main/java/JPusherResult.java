/**
 * @author bosong
 * @date 2019-09-12
 */

public class JPusherResult implements PusherResult {

    private Code code;

    private String msg;

    JPusherResult(Code code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Code code() {
        return code;
    }

    @Override
    public String msg() {
        return msg;
    }
}
