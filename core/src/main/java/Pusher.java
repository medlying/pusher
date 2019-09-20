/**
 * @author bosong
 * @date 2019-09-11
 *
 */

public interface Pusher {

    /**
     * 发送消息
     * @param pusherMessage
     */
    PusherResult push(PusherMessage pusherMessage);
}
